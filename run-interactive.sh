#!/bin/bash
# Run Interactive CLI

echo "Running Interactive CLI..."
./gradlew clean build
./gradlew run -PmainClass=org.example.examples.InteractiveCliKt
