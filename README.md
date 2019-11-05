Overview

This is the main project repo for investigating the use of variational execution in finding strongly subsuming higher order mutants (SSHOMs).

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