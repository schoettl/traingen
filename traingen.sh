#!/bin/bash
# Wrapper script for Traingen (Java program, class TraingenApp)

# To compile the jar with dependencies:
# mvn clean compile assembly:single

readonly PROGDIR=$(dirname "$(readlink -m "$0")")

readonly TRAINGEN_VERSION=0.0.1-SNAPSHOT

main() {
    java -jar "$PROGDIR"/traingen/target/traingen-${TRAINGEN_VERSION}-jar-with-dependencies.jar "$@"
}

main "$@"
