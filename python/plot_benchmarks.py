import matplotlib.pyplot as plt
import seaborn as sns
from matplotlib import rc

import re

import benchmark_util as bk
import naive
import varex
import ga

###############
## please set
## parameters
###############

program = bk.VALIDATOR
output_fname = '/home/serena/reuse/paper_figs/ase/validator.pdf'
output_format = 'pdf'
linewidth = 5.0
# ymax = 1000
ymax = bk.VALIDATOR_ALL + 100

sns.set_style("white")
sns.set_context("poster")
rc('text', usetex=True)
rc('font',**{'family':'serif'})



###############
## script
###############

time_col = "time"
num_col = "# SSHOMs"


START_TIME = 1
END_TIME = 86400 # 24 hours

if program == bk.TRIANGLE:
    path = bk.TRIANGLE_PATH
    all_muts = bk.TRIANGLE_ALL
    title = "Triangle"
elif program == bk.CLI:
    path = bk.CLI_PATH
    all_muts = bk.CLI_ALL
    title = "CLI"
elif program == bk.MONOPOLY:
    path = bk.MONOPOLY_PATH
    all_muts = bk.MONOPOLY_ALL
    title = "Monopoly"
elif program == bk.VALIDATOR:
    path = bk.VALIDATOR_PATH
    all_muts = bk.VALIDATOR_ALL
    title = "Validator"
elif program == bk.CHESS:
    path = bk.CHESS_PATH
    all_muts = bk.CHESS_ALL
    title = "Chess"

naive_times, naive_avg  = naive.get_naive_avg(path, START_TIME, END_TIME)
ga_times, ga_avg        = ga.get_ga_avg(path, START_TIME, END_TIME)
varex_times, varex_avg  = varex.get_varex_avg(path, START_TIME, END_TIME)

# naive_times, naive_avg  = naive.triangle_get_naive_avg(path, START_TIME, END_TIME)
# ga_times, ga_avg        = ga.triangle_get_ga_avg(path, START_TIME, END_TIME)
# varex_times, varex_avg  = varex.triangle_get_varex_avg(path, START_TIME, END_TIME)


plt.figure(figsize=(10, 6))
plt.title(title, fontsize=36)


plt.plot(naive_times, naive_avg, ':', label="Brute Force", linewidth=linewidth)

plt.plot(ga_times, ga_avg, '--', label="Genetic Algorithm")#, markevery=list(range(len(ga_nums)-1)))
print(ga_avg[-1])

plt.plot(varex_times, varex_avg, '-', label="Variational Execution", linewidth=linewidth) #, markevery=list(range(len(varex_nums)-1)))

plt.plot([ga.candidates_100k], [ga.avg_at_100k], marker='o', markersize=10, color="green", linewidth=linewidth, label="100k Genetic Algorithm Candidates", linestyle = 'None')

fig = plt.gcf()
ax = plt.gca()
fig.tight_layout()

plt.semilogx()
plt.xlim(xmin=START_TIME)
plt.ylim(ymin=0, ymax=ymax)

sns.despine()
plt.hlines(all_muts, START_TIME, END_TIME,linestyles='dotted', label="all SSHOMs")
plt.legend(loc="upper left", fontsize='large',numpoints=1)
plt.xlabel("Time (s)")
plt.ylabel("SSHOMs Found")
plt.savefig(output_fname, format=output_format, dpi=400)
# plt.show()
