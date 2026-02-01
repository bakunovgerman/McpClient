#!/bin/bash
# Run Tool Call Example

echo "Running Tool Call Example..."
./gradlew clean build
./gradlew run -PmainClass=org.example.examples.ToolCallExampleKt
