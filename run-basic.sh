#!/bin/bash
# Run Basic Example

echo "Running Basic MCP Example..."
./gradlew clean build
./gradlew run -PmainClass=org.example.examples.BasicExampleKt
