#!/bin/bash
# Generate ET423 with stops defined in a given file (see stop-option-gen.sh).

readonly PROGNAME=$(basename "$0")
readonly PROGDIR=$(dirname "$(readlink -m "$0")")

printUsage() {
    cat <<EOF
usage: $PROGNAME <times-counts-file> <additional-traingen-options>
EOF
}

main() {
    if [[ $# == 0 ]]; then
        printUsage
        exit 1
    fi

    declare timesCountsFile=$1
    shift

    declare stopOptions otherOptions
    stopOptions=$("$PROGDIR"/stop-option-gen.sh < "$timesCountsFile")

    otherOptions="--number-entrance-areas=12 \
--compartment-targets \
--door-source-distance=0.5 \
--block-ends --block-exits \
--random-seed=0 \
--train-geometry=org.vadere.state.scenario.Et423Geometry \
--output-file=~/vadere/Software/VadereGui/scenarios/traingen-output-file.json"

    # shellcheck disable=SC2086
    "$PROGDIR"/traingen.sh $otherOptions $stopOptions "$@"
}

main "$@"
