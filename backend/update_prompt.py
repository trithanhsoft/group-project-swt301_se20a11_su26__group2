import re

file_path = "/Users/ngocthanh/.gemini/antigravity/brain/723673b7-2fbd-4273-8288-04dddc984a85/Prompt_for_AI_Doc_Generation.md"

with open(file_path, "r") as f:
    content = f.read()

# Replace PART 1
old_part1 = """## PART 1: UNDERSTANDING THE INPUT MARKDOWN STRUCTURE

The Markdown input has two clearly separated sections:

### SECTION 1: TEST DATA SPECIFICATION
- Contains **10 modules**: Auth, User, Cart, Category, Progress, Course, Instructor, Payment, Problem, Contest.
- Each module contains one or more **entities** (e.g., `### Entity 1 — InvalidatedToken`).
- Each entity has a table with **5 columns**: `No. | Field Name | Data Type | Mock Data | Constraints & Description`.
- Total: **41 entities** across all modules.

### SECTION 2: TEST CASE SPECIFICATION"""

new_part1 = """## PART 1: UNDERSTANDING THE INPUT MARKDOWN STRUCTURE

The Markdown input has two clearly separated sections:

### SECTION 1: TEST DATA SPECIFICATION
- Structurally organized into **7 main sections** (Heading 2, e.g., `## 2. Module 1: User & Authentication Test Data`).
- Inside each main section, there are **subsections** (Heading 3, e.g., `### 2.1 Roles and Accounts Matrix`).
- Inside each subsection, there are **entities** (Heading 4, e.g., `#### Entity 1 — `Role``).
- There is a custom entity `#### Entity 6 — `InvalidInputCases`` under `### 2.2 Invalid Registration & Login Inputs`.
- Each entity contains a table with **5 columns**: `No. | Field Name | Data Type | Mock Data | Constraints & Description`.
- Total: **42 entities** across the entire document.

### SECTION 2: TEST CASE SPECIFICATION"""

content = content.replace(old_part1, new_part1)

# Replace PART 2 section 2.3
old_part2_3 = """### 2.3 Document Body — Per Module & Entity

For each **Module**:
- **Module Heading** (Heading 1 style): e.g., `Module: Auth`
  - Font: Arial, 16pt, Bold, color: `#1F3864`.

For each **Entity** within a module:
- **Entity Sub-heading** (Heading 2 style): e.g., `Entity 1 — InvalidatedToken`
  - Font: Arial, 13pt, Bold, color: `#2E74B5`.
- A brief one-line note: *"Table X: Mock data specification for the `[EntityName]` database table."* — 10pt, Italic, gray.
- A table with these exact columns: `No. | Field Name | Data Type | Mock Data | Constraints & Description`"""

new_part2_3 = """### 2.3 Document Body — Hierarchical Structure & Headings

When writing the Word document, apply the following heading levels and font styles:
- **Main Document Section** (Heading 1): e.g., `SECTION 1: TEST DATA SPECIFICATION`
  - Font: Arial, 18pt, Bold, color: `#1F3864`, center-aligned.
- **Module Heading** (Heading 2): e.g., `2. Module 1: User & Authentication Test Data`
  - Font: Arial, 14pt, Bold, color: `#1F3864` (dark navy).
- **Sub-module Heading** (Heading 3): e.g., `2.1 Roles and Accounts Matrix`
  - Font: Arial, 12pt, Bold, color: `#2E74B5` (medium blue).
- **Entity Heading** (Heading 4): e.g., `Entity 1 — Role`
  - Font: Arial, 11pt, Bold, color: `#4F81BD` (steel blue).
  - Add a brief one-line note below the header: *"Table X: Mock data specification for the `[EntityName]` database table."* — 10pt, Italic, gray.
  - Follow immediately with the entity's data table."""

content = content.replace(old_part2_3, new_part2_3)

# Replace PART 5 Parsing Rules
old_part5 = """1. **Table row detection**: A line starting with `|` and containing at least 3 `|` characters is a table row.
2. **Separator row detection**: A line matching `|---|` pattern is a separator — SKIP this row, do not write it as data.
3. **Module detection**: A line starting with `## Module:` marks the start of a new module. Extract the module name from this line.
4. **Entity detection** (Section 1 only): A line starting with `### Entity` marks a new entity. Extract entity number and name (e.g., `Entity 1 — InvalidatedToken`)."""

new_part5 = """1. **Table row detection**: A line starting with `|` and containing at least 3 `|` characters is a table row.
2. **Separator row detection**: A line matching `|---|` pattern is a separator — SKIP this row, do not write it as data.
3. **Heading detection (Section 1)**:
   - Heading 2: Matches `## [Number]. [Name]` (e.g., `## 2. Module 1: ...`).
   - Heading 3: Matches `### [Number].[Number] [Name]` (e.g., `### 2.1 Roles...`).
   - Heading 4: Matches `#### Entity [Number] — [Name]` (e.g., `#### Entity 1 — `Role``).
4. **Entity detection (Section 1 only)**: A line starting with `#### Entity` marks a new entity. Extract entity number and name (e.g., `Entity 1 — Role`)."""

content = content.replace(old_part5, new_part5)

# Also update totals in SUMMARY sheet description
content = content.replace("Total Test Cases | 129", "Total Test Cases | 129\n| Total Entities | 42 |")

with open(file_path, "w") as f:
    f.write(content)

print("Updated prompt instructions successfully")
