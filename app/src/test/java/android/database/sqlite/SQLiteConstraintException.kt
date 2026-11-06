package android.database.sqlite

open class SQLiteException : RuntimeException {
    constructor() : super()
    constructor(error: String?) : super(error)
    constructor(error: String?, cause: Throwable?) : super(error, cause)
}

class SQLiteConstraintException : SQLiteException {
    constructor() : super()
    constructor(error: String?) : super(error)
}
