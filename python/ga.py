import benchmark_util as bk
import re

# real jank, but I don't want to implementregex for this
has_seen_prefix = "genetic algorithm has seen"
special_terms = set(["Generated FOMs", "Generated HOMs", has_seen_prefix])

for i in range(100):
    special_terms.add("GENERATION "+str(i))

sum_100k_candidates = 0
candidates_100k = 0
avg_at_100k = 0


def get_sshom_times(fname, end_time):
    ga_times = []

    with open(fname) as f:
        for l in f:
            match_obj = re.match(bk.TIMESTAMP_PATTERN, l)
            if match_obj:
                time = int(match_obj.group(1))/1000
                substr = match_obj.group(2)[:len(has_seen_prefix)]
                if substr not in special_terms :
                    if time <= end_time:
                        ga_times.append(time)
                if substr == has_seen_prefix:
                    global sum_100k_candidates
                    sum_100k_candidates += time


    return ga_times

def avg_times(times1, times2, times3):
    return bk.avg_times_3(times1, times2, times3)

def get_ga_avg(path, start_time, end_time):
    run1 = get_sshom_times(path+bk.GA1, end_time)
    run2 = get_sshom_times(path+bk.GA2, end_time)
    run3 = get_sshom_times(path+bk.GA3, end_time)

    

    times, avg = avg_times(run1, run2, run3)
    times.insert(0, start_time)
    avg.insert(0, 0)
    times.append(end_time)
    avg.append(avg[-1])

    global candidates_100k
    global avg_at_100k
    candidates_100k = sum_100k_candidates/3.0
    i = 0
    while i < len(times) and times[i] < candidates_100k:
        i += 1
    avg_at_100k = (avg[i-1] + avg[i]) / 2.0

    return times, avg