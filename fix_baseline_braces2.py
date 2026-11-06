with open("app/src/main/java/com/example/ui/BaselineScreen.kt", "r") as f:
    text = f.read()

import re
text = re.sub(r'        \}\n    \}\n    \}\n    if \(formState is FormState.Success\)', '        }\n    }\n    if (formState is FormState.Success)', text)

with open("app/src/main/java/com/example/ui/BaselineScreen.kt", "w") as f:
    f.write(text)
