# import matplotlib as mpl
# mpl.rcParams['font.family'] = ['GillSans']

import matplotlib.pyplot as plt
import seaborn as sns

import re



sns.set_style("white")
sns.set_context("poster")

time_col = "time"
num_col = "# SSHOMs"
timestamp_pattern = "^TIME\s+(\d+)\|(.+)"
path = "data/benchmarks/allTriangle"

START_TIME = 0.1
# END_TIME = 1e5
# END_TIME = 45000
END_TIME = 10000

naive_times = [START_TIME]
naive_nums = [0]

count = 0;
with open(path+"/naive.txt") as f:
    for l in f:
        match_obj = re.match(timestamp_pattern, l)
        if(match_obj):
            time = int(match_obj.group(1))/1000
            if (time <= END_TIME):
                count += 1
                naive_times.append(time)
                naive_nums.append(count)
naive_times.append(END_TIME)
naive_nums.append(naive_nums[-1])


varex_times = [START_TIME]
varex_nums = [0]

count = 0;
with open(path+"/varex.txt") as f:
    for l in f:
        match_obj = re.match(timestamp_pattern, l)
        if(match_obj):
            time = int(match_obj.group(1))/1000
            if (time <= END_TIME):
                count += 1
                varex_times.append(time)
                varex_nums.append(count)
# varex_times.append(END_TIME)
# varex_nums.append(varex_nums[-1])

ga_times = [START_TIME]
ga_nums = [0]

count = 0;
with open(path+"/ga.txt") as f:
    for l in f:
        match_obj = re.match(timestamp_pattern, l)
        if(match_obj):
            time = int(match_obj.group(1))/1000
            if (time <= END_TIME):
                count += 1
                ga_times.append(time)
                ga_nums.append(count)
# ga_times.append(END_TIME)
# ga_nums.append(ga_nums[-1])

# import matplotlib.font_manager as fm
# prop = fm.FontProperties(fname='/home/serena/reuse/gillsans.ttf')



# plt.figure(figsize=(9,6))
plt.figure(figsize=(8, 8))
plt.plot(naive_times, naive_nums, '^-', label="Exhaustive Search", markevery=list(range(len(naive_nums)-1)))
plt.plot(ga_times, ga_nums, 'X-', label="Genetic Algorithm")#, markevery=list(range(len(ga_nums)-1)))
plt.plot(varex_times, varex_nums, '.-', label="Varex") #, markevery=list(range(len(varex_nums)-1)))
# plt.semilogx()
plt.xlim(xmin=START_TIME)
plt.ylim(ymin=0)
sns.despine()
plt.hlines(965, START_TIME, END_TIME,linestyles='dashed', label="all SSHOMs")
plt.legend(loc="lower right", fontsize='medium', edgecolor='k')
# plt.title("SSHOMs found over time in Triangle")
plt.xlabel("Time (s)")
plt.ylabel("SSHOMs Found")
plt.savefig('/home/serena/reuse/splashfigs/lin-cutoff-square.png', format='png', dpi=400)
# plt.show()
