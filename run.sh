#!/bin/bash

echo "Running order matching engine..."

# Compile if needed
if [ ! -f "target/matching-engine-1.0.0.jar" ]; then
    echo "Compiling..."
    cd src && find . -name "*.java" | xargs javac -d ../target/classes
    cd .. && mkdir -p target
    jar cfe target/matching-engine-1.0.0.jar orderbook.Main -C target/classes .
fi

# Run with JFR profiling
java -XX:+FlightRecorder \
     -XX:+UnlockExperimentalVMOptions \
     -Xmx512m -Xms256m \
     -jar target/matching-engine-1.0.0.jar