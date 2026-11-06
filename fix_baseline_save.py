import re

with open("app/src/main/java/com/example/ui/BaselineScreen.kt", "r") as f:
    text = f.read()

# Fix the enabled check
text = text.replace('dynamicAnswers["study_id"].isNullOrBlank().not() && dynamicAnswers["household_id"].isNullOrBlank().not()', 
                    'dynamicAnswers["study_id"].isNullOrBlank().not()')

# Fix household ID assignment
text = text.replace('val householdId = dynamicAnswers["household_id"] ?: ""', 
                    'val studyId = dynamicAnswers["study_id"] ?: ""\n                            val householdId = dynamicAnswers["household_id"]?.takeIf { it.isNotBlank() } ?: studyId')
                    
text = text.replace('val studyId = dynamicAnswers["study_id"] ?: ""\n                            val csamId',
                    'val csamId')

with open("app/src/main/java/com/example/ui/BaselineScreen.kt", "w") as f:
    f.write(text)
