import re

with open("app/src/main/java/com/example/ui/MainViewModel.kt", "r") as f:
    text = f.read()

bad_block = """                    obj.put("weightEnrolment", b.weightEnrolment)
                    obj.put("weight12Weeks", b.weight12Weeks)
                    obj.put("heightEnrolment", b.heightEnrolment)
                    obj.put("height12Weeks", b.height12Weeks)
                    obj.put("programmeOutcome", b.programmeOutcome)
                    obj.put("thrReceivedTreatment", b.thrReceivedTreatment)
                    obj.put("thrConsumedDays7d", b.thrConsumedDays7d)
                    obj.put("dietaryScore24h", b.dietaryScore24h)"""

good_block = """                    obj.put("programmeOutcome", b.programmeOutcome)
                    obj.put("thrReceivedTreatment", b.thrReceivedTreatment)
                    obj.put("thrConsumedDays7d", b.thrConsumedDays7d)"""

text = text.replace(bad_block, good_block)

with open("app/src/main/java/com/example/ui/MainViewModel.kt", "w") as f:
    f.write(text)
