#!/bin/bash
# Compilation script for Online Examination Ranking System (Console + Web Application)

set -e

echo "=========================================================="
echo "Compiling Online Examination Ranking System..."
echo "=========================================================="

mkdir -p bin webapp/WEB-INF/classes webapp/WEB-INF/lib

# Ensure runtime dependencies are present in webapp/WEB-INF/lib
cp lib/mysql-connector-j-8.2.0.jar webapp/WEB-INF/lib/
cp lib/gson-2.13.2.jar webapp/WEB-INF/lib/

# Compile all source files into bin/ (for console / test suite)
javac -cp "lib/*:src" -d bin $(find src -name "*.java")

# Compile all source files into webapp/WEB-INF/classes/ (for Tomcat Webapp)
javac -cp "lib/*:src" -d webapp/WEB-INF/classes $(find src -name "*.java")

# Compile test suite
javac -cp "lib/*:bin:src" -d bin $(find test -name "*.java")

echo "[SUCCESS] Compilation completed successfully! Classes generated in bin/ and webapp/WEB-INF/classes/"
