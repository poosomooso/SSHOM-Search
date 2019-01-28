import matplotlib.pyplot as plt
import seaborn as sns

import re

import benchmark_util as bk
import naive
import varex
import ga


sns.set_style("white")
sns.set_context("talk")

time_col = "time"
num_col = "# SSHOMs"


START_TIME = 1
END_TIME = 86400 # 24 hours

path = bk.MONOPOLY

naive_times, naive_avg  = naive.get_naive_avg(path, START_TIME, END_TIME)
ga_times, ga_avg        = ga.get_ga_avg(path, START_TIME, END_TIME)
varex_times, varex_avg  = varex.get_varex_avg(path, START_TIME, END_TIME)


plt.figure(figsize=(12, 9))
plt.plot(naive_times, naive_avg, ':', label="Brute Force")
plt.plot(ga_times, ga_avg, '--', label="Genetic Algorithm")#, markevery=list(range(len(ga_nums)-1)))
plt.plot(varex_times, varex_avg, '-', label="Variational Execution") #, markevery=list(range(len(varex_nums)-1)))
plt.semilogx()
plt.xlim(xmin=START_TIME)
plt.ylim(ymin=0)
sns.despine()
plt.hlines(bk.MONOPOLY_ALL, START_TIME, END_TIME,linestyles='dotted', label="all SSHOMs")
plt.legend(loc="upper left", fontsize='medium')
# plt.title("SSHOMs found over time in Triangle")
plt.xlabel("Time (s)")
plt.ylabel("SSHOMs Found")
# plt.savefig('/home/serena/reuse/splashfigs/lin-cutoff-square.png', format='png', dpi=400)
plt.show()
