# satviz

satviz is an application that visualises reentrant incremental satisfiability (SAT) solvers and proofs. More generally, it provides abstractions and a compact protocol for sending [CNF clauses](https://en.wikipedia.org/wiki/Conjunctive_normal_form) from a *source* (such as a [DRAT](https://github.com/marijnheule/drat-trim) proof or a [CDCL](https://en.wikipedia.org/wiki/Conflict-driven_clause_learning) solver) to a *consumer*, which processes clauses in some way. This repository specifically provides a *consumer* visualising clauses using a *Variable Interaction Graph* and a *heatmap*.

The visualisation part currently only supports *Linux*.

![ortholatin-7.cnf](img/ortholatin-7.gif)

## Prerequisites

To work on the project and to build it, you'll need to install a few dependencies:

- `gcc` and `make` - those should be preinstalled on most Linux distributions.
- `cmake` - to configure the C++ project builds
- `libtheora-dev` - for video encoding
- `libsfml-dev` - for rendering
- `libgtest-dev` - to run the test suite
- a JDK for Java SE 17

On Ubuntu, you can install most of these using `sudo apt install build-essential cmake libtheora-dev libsfml-dev libgtest-dev`.

For the JDK, we recommend [*sdkman!*](https://sdkman.io): `sdk install java 17.0.2-open`

## Installation
Clone this repository and run the following commands:
```
git submodule init
git submodule add
```

To install the app on your system, run `sudo ./gradlew install`. 

To only get a zipped distribution and not install it directly, use `./gradlew satvizDist` instead.
You will find the visualisation app in `satviz-consumer/build/satviz.zip` and the producer app in `satviz-producer/build/satviz-producer.zip`. Both contain a script to run them in `bin/`.

For testing purposes, you can also run `./gradlew installTestBuild`, which will put the consumer application in `test-run/satviz`.

