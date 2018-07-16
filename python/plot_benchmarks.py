
import matplotlib.pyplot as plt
import seaborn as sns

import re

time_col = "time"
num_col = "# SSHOMs"
timestamp_pattern = "^TIME\s+(\d+)\|(.+)"

START_TIME = 0.1

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
print(varex_times, varex_nums)

plt.plot(naive_times, naive_nums, '.-', label="Brute Force")
plt.plot(varex_times, varex_nums, '.-', label="Varex")
plt.semilogx()
plt.hlines(38, START_TIME, 5e3, label="all SSHOMs")
plt.legend(loc="lower right", frameon=True, fontsize='medium', edgecolor='k')
plt.title("Time to Find Strongly Subsuming Higher Order Mutants (SSHOMs)")
plt.xlabel("Time (s)")
plt.ylabel("Number of SSHOMs found")
plt.show()
