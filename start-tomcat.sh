#!/bin/bash
# Start script for ExamRank (Apache Tomcat + Web Application)

set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$DIR"

# Load local environment variables if .env exists
if [ -f ".env" ]; then
    set -a
    source .env 2>/dev/null || true
    set +a
fi

echo "=========================================================="
echo "Starting ExamRank Web Application on Apache Tomcat..."
echo "=========================================================="

# 0. Ensure Tomcat server runtime exists
if [ ! -d "server/apache-tomcat-10.1.25" ]; then
    echo "Tomcat runtime not found in server/. Downloading Apache Tomcat 10.1.25..."
    mkdir -p server
    curl -sL https://archive.apache.org/dist/tomcat/tomcat-10/v10.1.25/bin/apache-tomcat-10.1.25.tar.gz | tar -xz -C server/
fi

# 1. Compile project and prepare webapp classes
./compile.sh

# 2. Deploy webapp to Tomcat ROOT
mkdir -p server/apache-tomcat-10.1.25/webapps/ROOT
rm -rf server/apache-tomcat-10.1.25/webapps/ROOT/*
cp -r webapp/* server/apache-tomcat-10.1.25/webapps/ROOT/

# 3. Ensure Tomcat scripts have execution permissions
chmod +x server/apache-tomcat-10.1.25/bin/*.sh

# 4. Check if Tomcat is already running, if so stop it first
if pgrep -f "apache-tomcat-10.1.25" > /dev/null; then
    echo "Tomcat is currently running. Stopping existing instance..."
    ./server/apache-tomcat-10.1.25/bin/catalina.sh stop 10 -force || true
    sleep 2
fi

# 5. Start Tomcat
./server/apache-tomcat-10.1.25/bin/catalina.sh start

echo "=========================================================="
echo "ExamRank Web Application successfully launched!"
echo "URL: http://localhost:8080/"
echo "Credentials:"
echo "  - Admin:   admin   / admin123"
echo "  - Faculty: faculty / faculty123"
echo "=========================================================="

