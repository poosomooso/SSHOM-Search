TIMESTAMP_PATTERN = "^TIME\s+(\d+)\|(.+)"

CLI = 0
VALIDATOR = 1
MONOPOLY = 2
CHESS = 3
TRIANGLE = 4

BRUTE1 = "-naive1.txt"
BRUTE2 = "-naive2.txt"
BRUTE3 = "-naive3.txt"
GA1 = "-ga1.txt"
GA2 = "-ga2.txt"
GA3 = "-ga3.txt"
VAREX1 = "-varex1.txt"
VAREX2 = "-varex2.txt"
VAREX3 = "-varex3.txt"
SMART1 = "-smart1.txt"
SMART2 = "-smart1.txt"
SMART3 = "-smart1.txt"

CLI_PATH = "data/benchmarks/issta-submission/cli/cli"
VALIDATOR_PATH = "data/benchmarks/ase-submission/validator/validator"
MONOPOLY_PATH = "data/benchmarks/issta-submission/monopoly/monopoly"
CHESS_PATH = "data/benchmarks/issta-submission/chess/chess"
TRIANGLE_PATH = "data/benchmarks/ase-submission/allTriangle/triangle"

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
