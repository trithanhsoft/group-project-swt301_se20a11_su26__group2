import re

file_path = "/Users/ngocthanh/.gemini/antigravity/brain/723673b7-2fbd-4273-8288-04dddc984a85/QA_Specification.md"

with open(file_path, "r") as f:
    content = f.read()

# Separate Section 1 and Section 2
parts = re.split(r'# SECTION 2: TEST CASE SPECIFICATION', content)
sec1 = parts[0]
sec2 = parts[1]

# Parse entities from Section 1
# Each entity looks like:
# ### Entity X — `Name`
# | No. | ...
# |---|...
# and some lines of table...
entity_blocks = re.split(r'### Entity \d+ — `([^`]+)`', sec1)

header_info = entity_blocks[0] # Anything before Entity 1

# Extract entities as key-value (name -> block content)
entities = {}
for i in range(1, len(entity_blocks), 2):
    entity_name = entity_blocks[i]
    block_content = entity_blocks[i+1].strip()
    
    # We want to keep the markdown table
    entities[entity_name] = block_content

print("Parsed entities:", list(entities.keys()))
print("Total parsed:", len(entities))
