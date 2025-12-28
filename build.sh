#!/bin/bash

echo "Building order matching engine for deployment..."

# Compile Java files
cd src && find . -name "*.java" | xargs javac -cp "../lib/*" -d ../target/classes
cd ..

# Create target directory
mkdir -p target

# Download Jackson if not present
if [ ! -f "lib/jackson-databind-2.15.2.jar" ]; then
    mkdir -p lib
    echo "Downloading Jackson dependencies..."
    curl -L -o lib/jackson-core-2.15.2.jar https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-core/2.15.2/jackson-core-2.15.2.jar
    curl -L -o lib/jackson-databind-2.15.2.jar https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-databind/2.15.2/jackson-databind-2.15.2.jar
    curl -L -o lib/jackson-annotations-2.15.2.jar https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-annotations/2.15.2/jackson-annotations-2.15.2.jar
fi

# Create JAR with dependencies
jar cfe target/matching-engine-1.0.0.jar orderbook.WebServer -C target/classes . -C lib .

echo "Build complete. JAR: target/matching-engine-1.0.0.jar"