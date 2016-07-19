#!/bin/bash
# Generate --stop options for traingen given input data.
# Input data format: lines with whitespace-separated
# pairs of HH:MM and number of passengers entering.

readonly PROGNAME=$(basename "$0")

printUsage() {
    cat <<EOF
usage: $PROGNAME < time-and-enter.txt
EOF
}

main() {
    if [[ $# != 0 ]]; then
        printUsage
        exit 1
    fi

    cat \
    | awk '{ split($1, a, ":"); s = a[1]*3600+a[2]*60; print s, $2 }' \
    | awk 'NR==1{ t=$1 }; { print ($1-t), $2 }' \
    | awk '{ print "--stop=" $1 ",top," $2 }' \
    | awk -v ab=1 '{ if(ab) print $0 " \\"; else print }'
    # ab = append backslash
}

main "$@"
