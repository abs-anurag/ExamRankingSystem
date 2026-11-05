#!/bin/bash
# Execution script for Online Examination Ranking System

set -e

# Load local environment variables if .env exists
if [ -f ".env" ]; then
    set -a
    source .env 2>/dev/null || true
    set +a
fi

if [ ! -d "bin" ] || [ ! -f "bin/Main.class" ]; then
    echo "Compiled files not found. Compiling first..."
    ./compile.sh
fi

java -cp "lib/mysql-connector-j-8.2.0.jar:bin" Main

