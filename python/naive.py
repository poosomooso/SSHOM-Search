import benchmark_util as bk
import re

# real jank, but I don't want to implementregex for this
special_terms = set(["start homs", "order 2 done", "order 3 done", "order 4 done", "order 5 done"])

for i in range(2,100):
    special_terms.add("order {} done".format(i))

def get_sshom_times(fname, end_time):
    naive_times = []

    with open(fname) as f:
        for l in f:
            match_obj = re.match(bk.TIMESTAMP_PATTERN, l)
            if match_obj:
                if match_obj.group(2) not in special_terms :
                    time = int(match_obj.group(1))/1000
                    if time <= end_time:
                        naive_times.append(time)

    return naive_times

def avg_times(times1, times2, times3):
    return bk.avg_times_3(times1, times2, times3)

def get_naive_avg(path, start_time, end_time):
    run1 = get_sshom_times(path+bk.BRUTE1, end_time)
    run2 = get_sshom_times(path+bk.BRUTE2, end_time)
    run3 = get_sshom_times(path+bk.BRUTE3, end_time)

    times, avg = avg_times(run1, run2, run3)
    times.insert(0, start_time)
    avg.insert(0, 0)
    times.append(end_time)
    avg.append(avg[-1])
    return times, avg

def triangle_get_naive_avg(path, start_time, end_time):
    run1 = get_sshom_times(path+bk.BRUTE1, end_time)
    run1.insert(0, start_time)
    avg = list(range(len(run1)))
    avg.append(avg[-1])
    run1.append(end_time)
    return run1, avg