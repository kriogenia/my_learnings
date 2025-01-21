# Genetic Algorithm for Hub Location Problem

The hub location problem was first introduced by O'Kelly (1987). It stems from real world industries, like postal
deliveries or passenger transports, where different places are nodes in an interconnected graph and all possible
transits should be optimized defining some of them as hubs. These centrals hubs would serve as switching points for
flows between the nodes, optimizing the edges connecting two hubs. Each node would be assigned to a hub and the transit
from a node *i* to a node *j* would be routed first to the hub assigned to *i* (*k*) and then to *j* via its own 
assigned hub, *l*. This structure creates a network where the positioning of hubs significantly impacts transportation
costs. The hub location model seeks to minimize these costs by choosing optimal hub placements.

This project creates a Genetic Algorithm implementation solving a version of this problem presented in "*Efficient 
algorithms for the uncapacitated single allocation p-hub median problem*" of A.T. Ernst & M. Krishnamoorthy.

The article of this project can be found in the repository: [Practical study of basic Genetic Algorithm over the
p-Hub Median Problem](./Practical_study_of_basic_Genetic_Algorithm_over_the_p-Hub_Median_Problem.pdf).

> This project uses `just` (an alternative of `make`) to ease the commands to execute the project. If you don't want to 
> install this, check the [Justfile](./Justfile) for the commands executed by the recipe.

## Generating problems

The problems of the project are generated using the phub problems compiled in the 
[OR-library](https://people.brunel.ac.uk/~mastjjb/jeb/orlib/phubinfo.html) of
[J.E. Beasley](http://people.brunel.ac.uk/~mastjjb/jeb/jeb.html). Different subproblems  can be generated from the
Australian Post data with the `generate.c` file given in the library. To ease this just run the following recipe
specifying the number of nodes and hubs. The generated subproblem will be placed in the resources folder where the
project will be able to pick it.

```sh
just subproblem 10 3
```

The repository already contains all the subproblems with solutions given in the OR-library, so you won't neeed to this
step unless you want to try other combinations.

## Running the algorithm

To execute the algorithm a single time until the stop criterion is met you can just run the recipe with N and P.

```sh
just run "10 3"
```

It is also possible to specify a seed, a maximum number of evaluations and the population size.

```sh
just run "10 3 --seed 123 --limit 10000 --population 15"
```

## Benchmarking

There's two benchmark classes in this project. The first will execute the algorithm an exact number of times.

```shell
just max_evals | tee -a data_/benchmark_max_evals.csv
```

While the second one will execute the algorithm multiple times with different parameters until the best solution is
found.

```shell
just optimal | tee -a data_/benchmark_optimal.csv
```
