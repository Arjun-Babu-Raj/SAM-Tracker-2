package org.json

import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import okio.Buffer

class JSONObject {
    private val map = LinkedHashMap<String, Any?>()

    constructor()

    constructor(copyFrom: Map<*, *>) {
        for ((k, v) in copyFrom) {
            if (k != null) {
                map[k.toString()] = wrap(v)
            }
        }
    }

    constructor(json: String) {
        val buffer = Buffer().writeUtf8(json)
        val reader = JsonReader.of(buffer)
        reader.isLenient = true
        val parsed = readValue(reader)
        if (parsed is JSONObject) {
            map.putAll(parsed.map)
        }
    }

    fun put(name: String, value: Any?): JSONObject {
        if (value == null) {
            map.remove(name)
        } else {
            map[name] = wrap(value)
        }
        return this
    }

    fun put(name: String, value: Int): JSONObject = put(name, value as Any)
    fun put(name: String, value: Long): JSONObject = put(name, value as Any)
    fun put(name: String, value: Double): JSONObject = put(name, value as Any)
    fun put(name: String, value: Boolean): JSONObject = put(name, value as Any)

    fun has(name: String): Boolean = map.containsKey(name)

    fun isNull(name: String): Boolean = !map.containsKey(name) || map[name] == null || map[name] === NULL

    fun get(name: String): Any = map[name] ?: throw RuntimeException("No value for $name")

    fun opt(name: String): Any? = map[name]

    fun getString(name: String): String {
        val v = map[name]
        if (v == null || v === NULL) throw RuntimeException("No value for $name")
        return v.toString()
    }

    fun optString(name: String): String = optString(name, "")

    fun optString(name: String, fallback: String): String {
        val v = map[name]
        return if (v == null || v === NULL) fallback else v.toString()
    }

    fun getInt(name: String): Int = optInt(name, 0)

    fun optInt(name: String, fallback: Int = 0): Int {
        val v = map[name] ?: return fallback
        return when (v) {
            is Number -> v.toInt()
            is String -> v.toIntOrNull() ?: fallback
            else -> fallback
        }
    }

    fun getDouble(name: String): Double = optDouble(name, Double.NaN)

    fun optDouble(name: String, fallback: Double = Double.NaN): Double {
        val v = map[name] ?: return fallback
        return when (v) {
            is Number -> v.toDouble()
            is String -> v.toDoubleOrNull() ?: fallback
            else -> fallback
        }
    }

    fun getJSONArray(name: String): JSONArray =
        optJSONArray(name) ?: throw RuntimeException("No JSONArray for $name")

    fun optJSONArray(name: String): JSONArray? = map[name] as? JSONArray

    fun getJSONObject(name: String): JSONObject =
        optJSONObject(name) ?: throw RuntimeException("No JSONObject for $name")

    fun optJSONObject(name: String): JSONObject? = map[name] as? JSONObject

    fun keys(): Iterator<String> = map.keys.iterator()

    fun length(): Int = map.size

    override fun toString(): String = toString(0)

    fun toString(indentSpaces: Int): String {
        val buffer = Buffer()
        val writer = JsonWriter.of(buffer)
        if (indentSpaces > 0) {
            writer.indent = " ".repeat(indentSpaces)
        }
        writeValue(writer, this)
        return buffer.readUtf8()
    }

    internal fun entries(): Set<Map.Entry<String, Any?>> = map.entries

    companion object {
        @JvmField
        val NULL: Any = Any()

        internal fun wrap(v: Any?): Any? = when (v) {
            null -> NULL
            is JSONObject, is JSONArray, is String, is Number, is Boolean -> v
            is Map<*, *> -> JSONObject(v)
            is Collection<*> -> JSONArray(v)
            else -> v.toString()
        }

        internal fun readValue(reader: JsonReader): Any? {
            return when (reader.peek()) {
                JsonReader.Token.BEGIN_OBJECT -> {
                    val obj = JSONObject()
                    reader.beginObject()
                    while (reader.hasNext()) {
                        val name = reader.nextName()
                        obj.map[name] = readValue(reader)
                    }
                    reader.endObject()
                    obj
                }
                JsonReader.Token.BEGIN_ARRAY -> {
                    val arr = JSONArray()
                    reader.beginArray()
                    while (reader.hasNext()) {
                        arr.put(readValue(reader))
                    }
                    reader.endArray()
                    arr
                }
                JsonReader.Token.STRING -> reader.nextString()
                JsonReader.Token.NUMBER -> {
                    val str = reader.nextString()
                    if (str.contains('.') || str.contains('e') || str.contains('E')) {
                        str.toDouble()
                    } else {
                        str.toLongOrNull()?.let { if (it in Int.MIN_VALUE..Int.MAX_VALUE) it.toInt() else it } ?: str.toDouble()
                    }
                }
                JsonReader.Token.BOOLEAN -> reader.nextBoolean()
                JsonReader.Token.NULL -> {
                    reader.nextNull<Any>()
                    NULL
                }
                else -> null
            }
        }

        internal fun writeValue(writer: JsonWriter, value: Any?) {
            when (value) {
                null, NULL -> writer.nullValue()
                is JSONObject -> {
                    writer.beginObject()
                    for ((k, v) in value.entries()) {
                        writer.name(k)
                        writeValue(writer, v)
                    }
                    writer.endObject()
                }
                is JSONArray -> {
                    writer.beginArray()
                    for (i in 0 until value.length()) {
                        writeValue(writer, value.opt(i))
                    }
                    writer.endArray()
                }
                is String -> writer.value(value)
                is Int -> writer.value(value.toLong())
                is Long -> writer.value(value)
                is Double -> writer.value(value)
                is Float -> writer.value(value.toDouble())
                is Number -> writer.value(value)
                is Boolean -> writer.value(value)
                else -> writer.value(value.toString())
            }
        }
    }
}

class JSONArray {
    private val list = ArrayList<Any?>()

    constructor()

    constructor(copyFrom: Collection<*>) {
        for (item in copyFrom) {
            list.add(JSONObject.wrap(item))
        }
    }

    constructor(json: String) {
        val buffer = Buffer().writeUtf8(json)
        val reader = JsonReader.of(buffer)
        reader.isLenient = true
        val parsed = JSONObject.readValue(reader)
        if (parsed is JSONArray) {
            list.addAll(parsed.list)
        }
    }

    fun length(): Int = list.size

    fun put(value: Any?): JSONArray {
        list.add(JSONObject.wrap(value))
        return this
    }

    fun opt(index: Int): Any? = list.getOrNull(index)

    fun getJSONObject(index: Int): JSONObject =
        list.getOrNull(index) as? JSONObject ?: throw RuntimeException("Not a JSONObject at $index")

    override fun toString(): String {
        val buffer = Buffer()
        val writer = JsonWriter.of(buffer)
        JSONObject.writeValue(writer, this)
        return buffer.readUtf8()
    }
}
