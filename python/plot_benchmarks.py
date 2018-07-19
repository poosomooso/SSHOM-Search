
import matplotlib.pyplot as plt
import seaborn as sns

import re

time_col = "time"
num_col = "# SSHOMs"
timestamp_pattern = "^TIME\s+(\d+)\|(.+)"

START_TIME = 0.1
END_TIME = 5e3

naive_times = [START_TIME]
naive_nums = [0]

count = 0;
with open("data/benchmarks/naive.txt") as f:
    for l in f:
        match_obj = re.match(timestamp_pattern, l)
        if(match_obj):
            time = int(match_obj.group(1))/1000
            count += 1
            naive_times.append(time)
            naive_nums.append(count)
naive_times.append(END_TIME)
naive_nums.append(naive_nums[-1])


varex_times = [START_TIME]
varex_nums = [0]

count = 0;
with open("data/benchmarks/varex.txt") as f:
    for l in f:
        match_obj = re.match(timestamp_pattern, l)
        if(match_obj):
            time = int(match_obj.group(1))/1000
            count += 1
            varex_times.append(time)
            varex_nums.append(count)
varex_times.append(END_TIME)
varex_nums.append(varex_nums[-1])

ga_times = [START_TIME]
ga_nums = [0]

count = 0;
with open("data/benchmarks/ga.txt") as f:
    for l in f:
        match_obj = re.match(timestamp_pattern, l)
        if(match_obj):
            time = int(match_obj.group(1))/1000
            count += 1
            ga_times.append(time)
            ga_nums.append(count)
ga_times.append(END_TIME)
ga_nums.append(ga_nums[-1])

plt.plot(naive_times, naive_nums, '.-', label="Brute Force", markevery=list(range(len(naive_nums)-1)))
plt.plot(varex_times, varex_nums, '.-', label="Varex", markevery=list(range(len(varex_nums)-1)))
plt.plot(ga_times, ga_nums, '.-', label="Genetic Algorithm", markevery=list(range(len(ga_nums)-1)))
plt.semilogx()
plt.hlines(38, START_TIME, END_TIME, label="all SSHOMs")
plt.legend(loc="lower right", frameon=True, fontsize='medium', edgecolor='k')
plt.title("Time to Find Strongly Subsuming Higher Order Mutants (SSHOMs)")
plt.xlabel("Time (s)")
plt.ylabel("Number of SSHOMs found")
plt.show()
