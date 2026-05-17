SIT707 Pass Task 9.1P — Test Driven Development & Continuous Integration

## Overview

This project implements the **Task Submission Inbox** feature for the OnTrack platform using a strict **Test-Driven Development (TDD)** strategy.

## Function Description

The `TaskInboxService` allows students to:
- Submit a task for review
- View their personal task inbox (list of submitted tasks)
- Track the status of each submission (`PENDING` → `UNDER_REVIEW` → `COMPLETE` / `NEED_RESUBMISSION`)
- View tutor feedback on each submitted task

## Project Structure

```
ontrack-task-inbox/
├── src/
│   ├── main/java/com/ontrack/
│   │   ├── TaskSubmission.java        # Domain model
│   │   └── TaskInboxService.java      # Business logic
│   └── test/java/com/ontrack/
│       └── TaskInboxServiceTest.java  # JUnit 5 tests
├── .github/workflows/
│   └── ci.yml                         # GitHub Actions CI pipeline
├── README.md
└── pom.xml
```

## Running Tests Locally

**Prerequisites:** Java 11+, Maven 3.8+

```bash
# Run all tests
mvn test
```

## CI Pipeline

This project uses **GitHub Actions** for CI. On every push to `main`:
1. Checkout the code
2. Set up JDK 11
3. Build the project with Maven
4. Run all unit tests
5. Upload test reports as build artifacts

## TDD Strategy

Tests were written **before** implementation following the red-green-refactor cycle:
1. **Red** — Write a failing test for the desired behaviour
2. **Green** — Write the minimum code to make the test pass
3. **Refactor** — Clean up code without breaking tests
