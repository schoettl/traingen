#!/bin/bash
# Generate --stop options for traingen given input data.
# Input data format: lines with whitespace-separated
# pairs of HH:MM and number of passengers entering.

readonly PROGNAME=$(basename "$0")

printUsage() {
    cat <<EOF
usage: $PROGNAME [ -b ] < time-and-enter.txt
  -b   append a backslash after each --stop option
EOF
}

main() {
    if (( $# > 1 )); then
        printUsage
        exit 1
    fi
    declare appendBackslash=0
    if (( $# == 1 )); then
        case $1 in
            -b)
                appendBackslash=1
                ;;
            *)
                printUsage
                exit 1
                ;;
        esac
    fi

    # could also be an option. halves the count so that we have counts for one wagon
    declare countFactor=0.5

    cat \
    | awk '{ split($1, a, ":"); s = a[1]*3600+a[2]*60; print s, $2 }' \
    | awk 'NR==1{ t=$1 }; { print ($1-t), $2 }' \
    | awk -v f="$countFactor" '{ print $1, $2*f }' \
    | awk '{ printf "--stop=%s,top,%d\n", $1, $2 }' \
    | awk -v ab="$appendBackslash" '{ if(ab) print $0 " \\"; else print }'
}

main "$@"
