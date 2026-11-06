import re

def process_file(filename):
    with open(filename, 'r', encoding='utf-8') as f:
        content = f.read()

    # Find the FormField sealed class definition and add Subheading
    content = content.replace(
        "class Dropdown(id: String, labelEn: String, labelHi: String, val optionsEn: List<String>, val optionsHi: List<String>) : FormField(id, labelEn, labelHi)",
        "class Dropdown(id: String, labelEn: String, labelHi: String, val optionsEn: List<String>, val optionsHi: List<String>) : FormField(id, labelEn, labelHi)\n    class Subheading(labelEn: String, labelHi: String) : FormField(\"\", labelEn, labelHi)"
    )

    # We want to find patterns like:
    # FormField.Text("id", "HeadingEn\nQuestionEn", "HeadingHi\nQuestionHi", ... )
    # FormField.Dropdown("id", "HeadingEn\nQuestionEn", "HeadingHi\nQuestionHi", ... )

    # A regex to match FormField.xxx( ... )
    # Let's just process line by line or use a regex
    
    new_content = []
    lines = content.split('\n')
    for line in lines:
        if 'FormField.' in line and r'\n' in line:
            # Check if it has a newline pattern in the strings
            # For simplicity, we can regex capture:
            # ("[^"]*\\n[^"]*", "[^"]*\\n[^"]*"
            # It's better to do it carefully
            match_text = re.search(r'FormField\.(Text|Dropdown)\("([^"]+)", "([^"]+)\\n([^"]+)", "([^"]+)\\n([^"]+)"', line)
            if match_text:
                field_type = match_text.group(1)
                field_id = match_text.group(2)
                sub_en = match_text.group(3)
                q_en = match_text.group(4)
                sub_hi = match_text.group(5)
                q_hi = match_text.group(6)
                
                # Replace the match with Subheading and the original field
                indent = line[:line.find('FormField')]
                subheading_line = f'{indent}FormField.Subheading("{sub_en}", "{sub_hi}"),'
                
                # We need to preserve the rest of the line (like true, or listOf(...))
                # Let's rebuild the field line by replacing the strings
                rest_of_line = line[match_text.end():]
                field_line = f'{indent}FormField.{field_type}("{field_id}", "{q_en}", "{q_hi}"{rest_of_line}'
                
                new_content.append(subheading_line)
                new_content.append(field_line)
            else:
                new_content.append(line)
        else:
            new_content.append(line)

    with open(filename, 'w', encoding='utf-8') as f:
        f.write('\n'.join(new_content))

process_file('app/src/main/java/com/example/ui/QuestionBank.kt')
