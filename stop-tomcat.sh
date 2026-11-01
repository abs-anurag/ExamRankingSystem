#!/bin/bash
# Stop script for Apache Tomcat

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$DIR"

echo "Stopping Apache Tomcat..."
if [ -f "server/apache-tomcat-10.1.25/bin/catalina.sh" ]; then
    ./server/apache-tomcat-10.1.25/bin/catalina.sh stop 10 -force || true
fi

echo "Apache Tomcat stopped."

