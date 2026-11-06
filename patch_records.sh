sed -i 's/items(filteredChildren.size) { index ->/items(filteredChildren, key = { it.studyId }) { child ->/' app/src/main/java/com/example/ui/RecordsScreen.kt
sed -i 's/val child = filteredChildren\[index\]//' app/src/main/java/com/example/ui/RecordsScreen.kt
