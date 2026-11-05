# ==============================================================
# ExamRank - Dockerfile for Apache Tomcat 10.1 on Railway (JDK 21)
# ==============================================================
FROM tomcat:10.1-jdk21-temurin

WORKDIR /app

# Copy libraries, source code, and web application assets
COPY lib/ ./lib/
COPY src/ ./src/
COPY webapp/ ./webapp/

# Prepare webapp directory, copy runtime libraries, and compile all Java servlets
RUN mkdir -p webapp/WEB-INF/classes webapp/WEB-INF/lib && \
    cp lib/mysql-connector-j-8.2.0.jar webapp/WEB-INF/lib/ && \
    cp lib/gson-2.13.2.jar webapp/WEB-INF/lib/ && \
    find src -name "*.java" > /tmp/sources.txt && \
    javac -cp "lib/*:src" -d webapp/WEB-INF/classes @/tmp/sources.txt && \
    rm -f /tmp/sources.txt

# Deploy web application to Tomcat ROOT context
RUN rm -rf /usr/local/tomcat/webapps/ROOT && \
    mkdir -p /usr/local/tomcat/webapps/ROOT && \
    cp -r webapp/* /usr/local/tomcat/webapps/ROOT/

# Default port for local testing; Railway dynamically overrides $PORT
ENV PORT=8080
EXPOSE 8080

# Create entrypoint script to dynamically configure Tomcat for Railway:
# 1. Binds Tomcat HTTP connector to address 0.0.0.0 (required by Railway to accept external traffic)
# 2. Replaces connector port with Railway's dynamically assigned $PORT
# 3. Uses 'exec' so catalina.sh receives OS signals directly as PID 1
RUN { \
    echo '#!/bin/sh'; \
    echo 'set -e'; \
    echo 'PORT="${PORT:-8080}"'; \
    echo "echo \"[Railway] Configuring Tomcat HTTP connector on 0.0.0.0:\${PORT}...\""; \
    echo 'sed -i -E "s/(<Connector[[:space:]]+)(address=\"[^\"]*\"[[:space:]]+)?port=\"[0-9]+\"/\\1port=\"${PORT}\" address=\"0.0.0.0\"/g" /usr/local/tomcat/conf/server.xml'; \
    echo 'exec catalina.sh run'; \
} > /usr/local/bin/docker-entrypoint.sh && chmod +x /usr/local/bin/docker-entrypoint.sh

ENTRYPOINT ["/usr/local/bin/docker-entrypoint.sh"]
