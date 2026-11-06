import json

# Try to find the Room generated schema
import os
for root, dirs, files in os.walk("app/schemas"):
    for file in files:
        if file.endswith(".json"):
            print(os.path.join(root, file))
