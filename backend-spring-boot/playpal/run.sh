#!/usr/bin/env bash

# Resolve the absolute path of the script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Define local JDK path
LOCAL_JDK="$SCRIPT_DIR/.jdk/jdk-21.0.11+10"

if [ -d "$LOCAL_JDK" ]; then
    echo "Using local JDK 21: $LOCAL_JDK"
    export JAVA_HOME="$LOCAL_JDK"
    export PATH="$JAVA_HOME/bin:$PATH"
else
    echo "WARNING: Local JDK 21 not found at $LOCAL_JDK. Using default system Java."
fi

# Ensure MongoDB is running natively
echo "Ensuring MongoDB is running..."
if ! pgrep -x "mongod" > /dev/null; then
    echo "MongoDB is not running. Starting local mongod..."
    mkdir -p "$SCRIPT_DIR/.mongo_data"
    mongod --dbpath "$SCRIPT_DIR/.mongo_data" --port 27017 --fork --logpath "$SCRIPT_DIR/.mongo_data/mongod.log"
else
    echo "MongoDB is already running."
fi

# Seed the database using mongosh if available
if command -v mongosh > /dev/null; then
    echo "Seeding the database with init.js..."
    mongosh localhost:27017/TEST "$SCRIPT_DIR/../mongo-init/init.js" > /dev/null 2>&1
    echo "Seeding completed (or already existed)."
fi

# Start Spring Boot application
echo "Starting Spring Boot application via Maven Wrapper..."
cd "$SCRIPT_DIR"
./mvnw spring-boot:run
