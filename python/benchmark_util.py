TIMESTAMP_PATTERN = "^TIME\s+(\d+)\|(.+)"

BRUTE1 = "-brute.txt"
BRUTE2 = "-brute2.txt"
BRUTE3 = "-brute3.txt"
GA1 = "-ga.txt"
GA2 = "-ga2.txt"
GA3 = "-ga3.txt"
VAREX1 = "-varex.txt"
VAREX2 = "-varex2.txt"
VAREX3 = "-varex3.txt"

CLI = "data/benchmarks/cli/cli"
VALIDATOR = "data/benchmarks/validator/validator"
MONOPOLY = "data/benchmarks/monopoly/monopoly"
CHESS = "data/benchmarks/chess/chess"
TRIANGLE = "data/benchmarks/allTriangle/triangle"

CLI_ALL = 376
MONOPOLY_ALL = 818
VALIDATOR_ALL = 100000
TRIANGLE_ALL = 965

def at_index(arr, i):
    if (i >= len(arr)):
        return -1
    return arr[i]

def avg_times_3(times1, times2, times3):
    i1 = 0
    i2 = 0
    i3 = 0
    total_ct = 0
    total_times = []
    avg = []

    while (at_index(times1, i1) >= 0) or (at_index(times2, i2) >= 0) or (at_index(times3, i3) >= 0):
        compare_times = {}
        t1 = at_index(times1, i1)
        t2 = at_index(times2, i2)
        t3 = at_index(times3, i3)
        if t1 >= 0:
            compare_times[t1] = 1
        if t2 >= 0:
            compare_times[t2] = 2
        if t3 >= 0:
            compare_times[t3] = 3

        smallest = sorted(compare_times.keys())[0]
        if compare_times[smallest] == 1:
            i1+=1
            total_times.append(t1)
        if compare_times[smallest] == 2:
            i2+=1
            total_times.append(t2)
        if compare_times[smallest] == 3:
            i3+=1
            total_times.append(t3)
        total_ct+=1
        avg.append(total_ct/3.0)
    return total_times, avg
