#!/bin/bash
# Run Batch Operations Example

echo "Running Batch Operations Example..."
./gradlew clean build
./gradlew run -PmainClass=org.example.examples.BatchOperationsExampleKt
