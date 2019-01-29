import matplotlib.pyplot as plt
import seaborn as sns
from matplotlib import rc

import re

import benchmark_util as bk
import naive
import varex
import ga


sns.set_style("white")
sns.set_context("poster")
rc('text', usetex=True)
rc('font',**{'family':'serif'})


time_col = "time"
num_col = "# SSHOMs"


START_TIME = 1
END_TIME = 86400 # 24 hours

lwidth = 5.0

# path = bk.TRIANGLE
# path = bk.MONOPOLY
# path = bk.CLI
path = bk.VALIDATOR

# all_muts = bk.TRIANGLE_ALL
# all_muts = bk.MONOPOLY_ALL
# all_muts = bk.CLI_ALL
all_muts = bk.VALIDATOR_ALL

naive_times, naive_avg  = naive.get_naive_avg(path, START_TIME, END_TIME)
ga_times, ga_avg        = ga.get_ga_avg(path, START_TIME, END_TIME)
varex_times, varex_avg  = varex.get_varex_avg(path, START_TIME, END_TIME)

# naive_times, naive_avg  = naive.triangle_get_naive_avg(path, START_TIME, END_TIME)
# ga_times, ga_avg        = ga.triangle_get_ga_avg(path, START_TIME, END_TIME)
# varex_times, varex_avg  = varex.triangle_get_varex_avg(path, START_TIME, END_TIME)


plt.figure(figsize=(10, 6))
plt.title("Validator", fontsize=36)


plt.plot(naive_times, naive_avg, ':', label="Brute Force", linewidth=lwidth)

plt.plot(ga_times, ga_avg, '--', label="Genetic Algorithm")#, markevery=list(range(len(ga_nums)-1)))
plt.plot([ga.candidates_100k], [ga.avg_at_100k], marker='o', markersize=10, color="green", linewidth=lwidth)
print(ga_avg[-1])

plt.plot(varex_times, varex_avg, '-', label="Variational Execution", linewidth=lwidth) #, markevery=list(range(len(varex_nums)-1)))

fig = plt.gcf()
ax = plt.gca()
fig.tight_layout()

plt.semilogx()
plt.xlim(xmin=START_TIME)
plt.ylim(ymin=0, ymax = 1200)

sns.despine()
# plt.hlines(all_muts, START_TIME, END_TIME,linestyles='dotted', label="all SSHOMs")
# plt.legend(loc="upper left", fontsize='small')
plt.xlabel("Time (s)")
plt.ylabel("SSHOMs Found")
plt.savefig('/home/serena/reuse/paper_figs/validator.pdf', format='pdf', dpi=400)
# plt.show()
