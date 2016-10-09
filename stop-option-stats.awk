BEGIN { FS = "," }
/^--stop=/ {
    s += $3
    split($1, a, "=")
    if (a[2] > t) t = a[2]
}
END {
    print "passenger count total", s
    print "time of last stop", t
}
