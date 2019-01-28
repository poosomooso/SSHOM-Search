import benchmark_util as bk
import re

# real jank, but I don't want to implementregex for this
special_terms = set(["load f(t)", "generate FOMs", "create SSHOM expression"])

varex_done = "create features"

def get_sshom_times(fname, end_time):
    varex_times = []

    with open(fname) as f:
        for l in f:
            match_obj = re.match(bk.TIMESTAMP_PATTERN, l)
            if match_obj:
                time = int(match_obj.group(1))/1000
                if match_obj.group(2) == varex_done:
                    varex_times.append(time)
                elif match_obj.group(2) not in special_terms:
                    if time <= end_time:
                        varex_times.append(time)

    return varex_times

def avg_times(times1, times2, times3):
    if len(times1) == 1: # only one thing -> only have varex times to report
        return [((times1[0] + times2[0] + times3[0]) / 3.0)], [bk.VALIDATOR_ALL]
    return bk.avg_times_3(times1, times2, times3)

def get_varex_avg(path, start_time, end_time):
    run1 = get_sshom_times(path+bk.VAREX1, end_time)
    run2 = get_sshom_times(path+bk.VAREX2, end_time)
    run3 = get_sshom_times(path+bk.VAREX3, end_time)

    times, avg = avg_times(run1, run2, run3)
    if len(times) == 1:
        times.insert(0, times[0]-1)
        avg.insert(0, 0)
    times.insert(0, start_time)
    avg.insert(0, 0)
    return times, avg