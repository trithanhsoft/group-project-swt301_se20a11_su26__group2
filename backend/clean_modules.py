import re

file_path = "/Users/ngocthanh/.gemini/antigravity/brain/723673b7-2fbd-4273-8288-04dddc984a85/QA_Specification.md"

with open(file_path, "r") as f:
    content = f.read()

# Let's clean up all headers like "## Module: User" or similar that are in Section 1 (before Section 2)
parts = re.split(r'# SECTION 2: TEST CASE SPECIFICATION', content)
sec1 = parts[0]
sec2 = parts[1]

# Remove any line that starts with "## Module: " in Section 1
lines = sec1.split("\n")
cleaned_lines = []
for line in lines:
    if line.strip().startswith("## Module:"):
        continue
    cleaned_lines.append(line)

new_sec1 = "\n".join(cleaned_lines)
new_content = new_sec1 + "# SECTION 2: TEST CASE SPECIFICATION" + sec2

with open(file_path, "w") as f:
    f.write(new_content)

print("Removed inline module headers from Section 1")
