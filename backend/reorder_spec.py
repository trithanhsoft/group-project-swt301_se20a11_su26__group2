import re

file_path = "/Users/ngocthanh/.gemini/antigravity/brain/723673b7-2fbd-4273-8288-04dddc984a85/QA_Specification.md"

with open(file_path, "r") as f:
    content = f.read()

# Separate Section 1 and Section 2
parts = re.split(r'# SECTION 2: TEST CASE SPECIFICATION', content)
sec1 = parts[0]
sec2 = parts[1]

# Extract entities
entity_blocks = re.split(r'### Entity \d+ — `([^`]+)`', sec1)
entities = {}
for i in range(1, len(entity_blocks), 2):
    entity_name = entity_blocks[i]
    block_content = entity_blocks[i+1].strip()
    entities[entity_name] = block_content

# Define groups
group_mapping = {
    "2.1": ["Role", "User", "UserOauthAccount", "InvalidatedToken", "UserDailyActivity"],
    "3.1": ["Category", "Course", "Chapter", "Lesson", "CompletedLessonsCount", "LessonProgress", "CourseReview", "Enrollment", "LessonComment", "Cart", "CartItem"],
    "3.2": ["Quiz", "QuizQuestion", "QuizOption", "QuizAttempt", "QuizAttemptAnswer"],
    "4.1": ["Problem", "ProblemVersion", "ProblemTestcase", "ProblemComment", "ProblemTag"],
    "4.2": ["ProblemSubmission"],
    "5.1": ["Contest", "ContestParticipant", "ContestProblem", "ContestProblemAttempt", "ContestRanking"],
    "6.1": ["Wallet", "WalletTransaction", "PayoutRequest"],
    "6.2": ["Order", "OrderItem", "PaymentTransaction"],
    "7.1": ["InstructorApplication", "Instructor"],
    "7.2": ["CourseModerationReport"]
}

# Construct Section 1
new_sec1 = """# QA Specification Document
**Project:** SWP391 – AI-Assisted Coding Audit Platform  
**Prepared By:** QA/QC Engineering Team  
**Last Updated:** 2026-07-15  
**Version:** 2.0 (Corrected & Completed)

---

# SECTION 1: TEST DATA SPECIFICATION
*(This raw data will be used to generate the official Test Data Word document)*

This section defines the mock/test data required in the database, extracted directly from the JPA Entity source code. Data types, constraints, and mock values reflect the actual persistence layer.

---

## 1. Introduction
This document defines the structured test data specifications and test case specifications for the SWP391 AI-Assisted Coding Audit Platform. Section 1 serves as the reference database mock values to establish pre-conditions, and Section 2 maps the functional API testing scenarios.

## 2. Module 1: User & Authentication Test Data

### 2.1 Roles and Accounts Matrix
This section defines the system roles, active user profiles, daily activities, and blacklisted/invalidated tokens required to test access control and authentication scenarios.
"""

entity_counter = 1

# Helper to output entities
def append_entities(keys):
    global entity_counter
    res = ""
    for k in keys:
        if k in entities:
            # We strip any trailing markdown line separators
            content = entities[k]
            # Replace the old end table separator if any
            content = re.sub(r'\n---$', '', content)
            res += f"\n#### Entity {entity_counter} — `{k}`\n"
            res += content + "\n"
            entity_counter += 1
    return res

# 2.1
new_sec1 += append_entities(group_mapping["2.1"])

# 2.2
new_sec1 += """
### 2.2 Invalid Registration & Login Inputs
This table defines invalid data payloads used to verify request validation, boundary checks, and system error responses during login and registration.

#### Entity 6 — `InvalidInputCases`
| No. | Field Name | Data Type | Mock Data | Constraints & Description |
|---|---|---|---|---|
| 1 | shortUsername | String | `"usr"` | Username length must be >= 5 characters. |
| 2 | weakPassword | String | `"123"` | Password must be >= 8 characters and contain uppercase, lowercase, numbers, and symbols. |
| 3 | invalidEmail | String | `"invalid-email.com"` | Email must follow the standard RFC 5322 format. |
| 4 | lockedUser | String | `"locked_user"` | Account status is `LOCKED`; login attempts must be rejected. |
| 5 | expiredToken | String | `"expired-jti-token-xyz"` | JWT ID exists in the invalidated tokens list; must return HTTP 401. |
"""
entity_counter += 1

# 3. Module 2
new_sec1 += """
## 3. Module 2: Course & Quiz Test Data

### 3.1 Course Information Data
This section contains categories, course drafts, published course details, curriculum structures (chapters and lessons), enrollment records, comments, and shopping cart states.
"""
new_sec1 += append_entities(group_mapping["3.1"])

new_sec1 += """
### 3.2 Quiz & Question Data
This section covers quizzes associated with lessons, multiple-choice questions, shuffled options, and user attempt responses with score tracking.
"""
new_sec1 += append_entities(group_mapping["3.2"])

# 4. Module 3
new_sec1 += """
## 4. Module 3: Coding Playground & Online Judge Test Data

### 4.1 Problem Constraints & Test Cases
This section details coding problem definitions, version histories, test cases with expected output, comments, and problem category tags.
"""
new_sec1 += append_entities(group_mapping["4.1"])

new_sec1 += """
### 4.2 Sample Source Code Payloads for Submission Grading
This section contains code submission records sent to the Judge0 sandbox, storing source code, execution limits, memory footprint, and OJ verdicts.
"""
new_sec1 += append_entities(group_mapping["4.2"])

# 5. Module 4
new_sec1 += """
## 5. Module 4: Competitive Programming Contest Test Data

### 5.1 Contest Schedule & Participant Setup
This section details the coding contest setup, list of problems assigned to contests, participant registrations, ICPC scoring rules, penalty statistics, and leaderboards.
"""
new_sec1 += append_entities(group_mapping["5.1"])

# 6. Module 5
new_sec1 += """
## 6. Module 5: E-Wallet & PayOS Webhook Integration Test Data

### 6.1 Payment Deposits Data
This section contains user wallets, balance logs, transaction histories (credits/debits), and bank payout/withdrawal requests.
"""
new_sec1 += append_entities(group_mapping["6.1"])

new_sec1 += """
### 6.2 Mock PayOS Webhook Payload Cases
This section describes order receipts, individual order line items, and transaction reference codes matched with PayOS payment gateway statuses.
"""
new_sec1 += append_entities(group_mapping["6.2"])

# 7. Module 6
new_sec1 += """
## 7. Module 6: Administration & Review Applications Test Data

### 7.1 Instructor Application Data
This section defines instructor candidate resumes (CVs), AI evaluation scores, recommendations, and approved instructor records.
"""
new_sec1 += append_entities(group_mapping["7.1"])

new_sec1 += """
### 7.2 Course Moderation Logs
This section contains AI moderation reports, validation checks, and text/video transcription scans flag logs for administrative review.
"""
new_sec1 += append_entities(group_mapping["7.2"])

# Rejoin and write
new_content = new_sec1 + "\n\n# SECTION 2: TEST CASE SPECIFICATION\n" + sec2

with open(file_path, "w") as f:
    f.write(new_content)

print("Reordered Section 1 successfully. Total entities:", entity_counter - 1)
