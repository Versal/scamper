unset key
set title "Fast Test"
set ylabel "Requests per second"
set style fill solid border -1
set xtic rotate by -45
set terminal postscript
set output 'fast-test.ps'
plot "fast-test.dat" using 2:xtic(1) with histogram
