#!/bin/bash

echo "Building order matching engine for deployment..."

# Compile Java files
cd src && find . -name "*.java" | xargs javac -d ../target/classes
cd ..

# Create target directory
mkdir -p target

# Create JAR without dependencies (simpler for Docker)
jar cfe target/matching-engine-1.0.0.jar orderbook.WebServer -C target/classes .

echo "Build complete. JAR: target/matching-engine-1.0.0.jar"