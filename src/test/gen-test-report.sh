#!/bin/bash

set -e

if [ -z "$1" ]; then
  echo "Usage: $0 <input.json>"
  exit 1
fi

INPUT_FILE=$1
OUTPUT_FILE="test-results.md"
# Use GITHUB_SERVER_URL and GITHUB_REPOSITORY if available, otherwise use a default
REPO_URL="${GITHUB_SERVER_URL:-https://github.com}/${GITHUB_REPOSITORY:-chr15m/eucalypt}"

# Extract summary data
PASSED_FILES=$(jq '[.testResults[] | select(.status == "passed")] | length' "$INPUT_FILE")
TOTAL_FILES=$(jq '.testResults | length' "$INPUT_FILE")
PASSED_SUITES=$(jq '.numPassedTestSuites' "$INPUT_FILE")
TOTAL_SUITES=$(jq '.numTotalTestSuites' "$INPUT_FILE")
PASSED_TESTS=$(jq '.numPassedTests' "$INPUT_FILE")
TOTAL_TESTS=$(jq '.numTotalTests' "$INPUT_FILE")
START_TIME=$(jq '.startTime' "$INPUT_FILE")
# Find the latest endTime across all test suites
END_TIME=$(jq '[.testResults[].endTime] | max' "$INPUT_FILE")
# Duration in seconds, formatted to 2 decimal places
DURATION=$(echo "scale=2; ($END_TIME - $START_TIME) / 1000" | bc)

# Start markdown file
echo "# Test Results" > "$OUTPUT_FILE"
echo "" >> "$OUTPUT_FILE"
echo "[Tests source code]($REPO_URL/blob/main/src/test/src)." >> "$OUTPUT_FILE"
echo "" >> "$OUTPUT_FILE"
echo '```' >> "$OUTPUT_FILE"

# Process and add test results, grouped by file
jq -r '
  .testResults[] |
  select(.assertionResults | length > 0) |
  " " +
  (if .status == "passed" then "✅" else "❌" end) +
  " " +
  (.name | sub(".*/src/"; "src/")) +
  " (" +
  (.assertionResults | length | tostring) +
  (if (.assertionResults | length) == 1 then " test" else " tests" end) +
  ") " +
  ((.endTime - .startTime) | round | tostring) +
  "ms"
' "$INPUT_FILE" >> "$OUTPUT_FILE"

echo '```' >> "$OUTPUT_FILE"
echo "" >> "$OUTPUT_FILE"
echo "## Summary" >> "$OUTPUT_FILE"
echo "" >> "$OUTPUT_FILE"

# Add summary
echo "- **Test Files**: $PASSED_FILES passed ($TOTAL_FILES)" >> "$OUTPUT_FILE"
echo "- **Test Suites**: $PASSED_SUITES passed ($TOTAL_SUITES)" >> "$OUTPUT_FILE"
echo "- **Tests**: $PASSED_TESTS passed ($TOTAL_TESTS)" >> "$OUTPUT_FILE"
echo "- **Duration**: ${DURATION}s" >> "$OUTPUT_FILE"
