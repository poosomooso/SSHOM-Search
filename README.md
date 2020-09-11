# Efficiently Finding Higher-Order Mutants

This repository contains (or links to) the source code and auxiliary material for the following publication:

>Chu-Pan Wong, Jens Meinicke, Leo Chen, João P. Diniz, Christian Kästner,
>and Eduardo Figueiredo. 2020. Efficiently Finding Higher-Order Mutants. In
>Proceedings of the 28th ACM Joint European Software Engineering Conference
>and Symposium on the Foundations of Software Engineering (ESEC/FSE ’20),
>November 8–13, 2020, Virtual Event, USA.ACM, New York, NY, USA, 13 pages.
>https://doi.org/10.1145/3368089.3409713

To reproduce the results presented in our paper, please refer to our [Figshare repo](https://doi.org/10.1184/R1/12081858).

# Source Code

This repository contains the source code for all the four SSHOM search strategies discussed in the paper:

* <img src="https://render.githubusercontent.com/render/math?math=search_{var}">: our systematic search based on variational execution 
* <img src="https://render.githubusercontent.com/render/math?math=search_{gen}">: the state-of-the-art genetic search 
* <img src="https://render.githubusercontent.com/render/math?math=search_{bf}">: the baseline brute-force search
* <img src="https://render.githubusercontent.com/render/math?math=search_{pri}">: our new heuristic search that prioritizes likely SSHOMs based on observed characteristics

The <img src="https://render.githubusercontent.com/render/math?math=search_{var}"> strategy uses an existing implementation of variational execution called [VarexC](https://github.com/chupanw/vbc.git). Since VarexC is under active development, we provide a snapshot of the version we used together with the experiment setup in the [Figshare repo](https://doi.org/10.1184/R1/12081858).

The customized symbolic execution engine discussed in the Appendix is available in [another Github repo](https://github.com/chupanw/symex-triangle.git).

# Environment

This project requires Java 8 and can be imported into Intellij or Eclipse.

# Subject programs

The jars for the programs we analyzed can be found in the module `code-ut`. The source code (with the generated mutants) can be found in other repos [linked](somewhere).

The program we used for mutation generation can be found [here](link repo).

Some of the package names in the subject programs needed to change for use with VarexC (to avoid duplicate package names). Those jars can be found in the same location in the `varexc` branch.

# Run search strategies

## Brute force and genetic algorithm

In the `varex-hom-finder` module, run the main class `benchmarks.RunBenchmarks`.
It takes two arguments: the program to run (ex. triangle, chess, validator) and the search strategy (ga or naive).

## Varex

In the varexc branch, use the same process as above, except the search strategy is `varex`.

## Heuristics search

# Complete list of SSHOMs
