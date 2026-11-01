#!/bin/bash
# Test execution script for Online Examination Ranking System
# Runs both Core ADSA Unit Tests and Live Web Integration Tests

set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$DIR"

echo "=========================================================="
echo "Compiling Project and Test Suites..."
echo "=========================================================="
./compile.sh

echo ""
echo "=========================================================="
echo "1. Running Core ADSA & JDBC Unit Test Suite"
echo "=========================================================="
java -cp "lib/*:bin" test.SystemTestSuite

echo ""
echo "=========================================================="
echo "2. Running Web API & Servlet Integration Test Suite"
echo "=========================================================="

# Ensure Tomcat is running for web test
if ! curl -s http://localhost:8080/ > /dev/null; then
    echo "Starting local Tomcat server for Web tests..."
    ./start-tomcat.sh
    sleep 2
fi

java -cp "lib/*:bin" test.WebIntegrationTestSuite

echo ""
echo "[ALL TESTS COMPLETED SUCCESSFULLY!]"
