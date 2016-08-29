#!/bin/bash
# Wrapper script for Traingen (Java program, class TraingenApp)

readonly PROGDIR=$(dirname "$(readlink -m "$0")")

readonly TRAINGEN_VERSION=0.0.1-SNAPSHOT

main() {
    # Classpath according to maven defaults
    #java -classpath "$PROGDIR"/traingen/target/classes/:"$PROGDIR"/../Software/VadereUtils/target/classes/:"$PROGDIR"/../Software/VadereState/target/classes/ edu.hm.cs.vadere.seating.traingen.TraingenApp "$@"
    java -jar "$PROGDIR"/traingen/target/traingen-$TRAINGEN_VERSION.jar "$@"
}

main "$@"
