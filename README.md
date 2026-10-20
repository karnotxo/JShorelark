# JShorelark 🦅

A Java 21 / JavaFX port of [Shorelark](https://codeberg.org/pwy/shorelark), the bird evolution
simulator by [Patryk Wychowaniec](https://github.com/patryk27): populations of birds learn to find
food, not because anyone programmed them to, but because a genetic algorithm evolves the weights of
each bird's neural network.

The Rust implementation is the specification for this port. Where behaviour could be ambiguous, the
Java code follows the Rust source, and the differences that remain are deliberate and listed in
[`TO-DO.md`](TO-DO.md).

## What actually happens

There is no backpropagation and no training set. Each bird is born with a random brain, and the
simulation is a loop over three facts:

1. **The eye** splits a 225° field of view into 9 cells. A visible food item adds
   `(range − distance) / range` to exactly one cell, so nearer food produces a stronger signal.
2. **The brain** is a small feed-forward network (9 inputs → 9 hidden neurons → 2 outputs) with
   ReLU activations. Its two outputs are a speed delta and a rotation delta, each clamped to
   `[-1, 1]` and then to the acceleration limits from `Config`.
3. **The world** is the unit square with wrapping edges. Birds move along their heading; a bird
   within `foodSize` of an item eats it and gains one unit of satiation.

Every `simGenerationLength` (2500) steps the population is replaced. Fitness is satiation — how much
food a bird ate, nothing else. Parents are drawn by fitness-proportionate (roulette) selection, their
weight vectors are crossed over uniformly, and the result is mutated.

Watch the median fitness rise over generations: that curve *is* the learning.

## Modules

| Module | Role |
|---|---|
| `genetic-algorithm` | Chromosome, selection, crossover, mutation, statistics |
| `neural-network` | Layers, neurons, activation, weight serialisation |
| `simulation` | Config, World, Bird, BirdEye, BirdBrain, Food, Simulation |
| `optimizer` | Headless CLI: parameter grid search, JSONL logs, CSV analysis |
| `simulation-ui` | JavaFX application, Canvas renderer, charts, controls, audio |
| `config-resources` | Shared Checkstyle / PMD / Log4j2 resources |

`genetic-algorithm`, `neural-network` and `simulation` are pure Java: no JavaFX, no file I/O, no
`System.out`, no threads. Randomness and time arrive through parameters. The two adapters own
everything else.

## Requirements

- **JDK 21** (the build enforces `[21,)`)
- **Maven 3.9+**

JavaFX 21 is pulled from Maven, so no separate SDK installation is needed.

## Build and run

```bash
mvn -B -ntp package            # compile and package
mvn -B -ntp test               # unit tests
mvn -B -ntp verify             # full gate: Spotless, Checkstyle, SpotBugs, PMD/CPD, JaCoCo
mvn -B -ntp spotless:apply     # format with google-java-format
```

Launch the desktop simulation:

```bash
mvn -B -ntp -pl simulation-ui javafx:run
```

### Keyboard shortcuts

| Key | Action |
|---|---|
| `Space` | Play / pause |
| `R` | Reset the simulation |
| `F` | Add 10 food items |
| `M` | Mute / unmute |
| `Ctrl` + `+` / `Ctrl` + `-` | Faster / slower |

The speed slider runs the simulation from 0.1× to 5× real time, and hovering a bird shows its
position, heading, fitness and what it currently sees.

## Optimizer (headless)

The optimizer runs many configurations in parallel and records what each one achieves, which is how
the default parameters were chosen.

```bash
# Grid-search parameters; one JSONL line per configuration per generation.
mvn -B -ntp -pl optimizer exec:java -Dexec.args="simulate -o results.jsonl"

# Reduced grid, useful as a smoke test.
mvn -B -ntp -pl optimizer exec:java -Dexec.args="simulate -o results.jsonl --test"

# Average each configuration and print CSV to stdout.
mvn -B -ntp -pl optimizer exec:java -Dexec.args="analyze -i results.jsonl"
```

`simulate` sweeps the same grid as the Rust optimizer: hidden neurons `{2, 3, 5, 10}`, vision range
`{0.1, 0.25, 0.33, 0.5}`, field-of-view angle `{1.0, 2.0, 3.14, 6.0}`, cells `{2, 3, 6, 9, 12}`,
mutation chance `{0.001, 0.01, 0.1, 0.5}` and coefficient `{0.01, 0.1, 0.3, 0.5, 1.0}` — 6400 points,
repeated 15 times each, 30 generations per run. That is a lot of CPU, which is why `--test` exists.

`analyze` reports `min`, `max`, `avg` and `median` fitness per configuration, sorted so two runs over
the same input produce byte-identical CSV. Rows are shuffled before being spread over the cores, so
an interrupted run still leaves a representative sample.

## Quality gates

Nothing here is advisory; a violation fails the build.

- **Spotless** with `google-java-format` (2 spaces, 100 columns, explicit imports)
- **Checkstyle** with `google_checks.xml`, promoted to `error`
- **SpotBugs** at `effort=Max`, `threshold=Low`
- **PMD** with `pmd-ruleset.xml`, plus CPD duplication checks
- **JaCoCo** with an 80 % line-coverage floor on the domain modules

Current line coverage: `genetic-algorithm` 94.5 %, `neural-network` 95.9 %, `simulation` 95.7 %,
`optimizer` 86.3 %. `simulation-ui` is exercised by a smoke test rather than pixel assertions, so it
carries no coverage gate.

## Documentation

- [`ROADMAP.md`](ROADMAP.md) / [`ROADMAP.mmd`](ROADMAP.mmd) — milestones and the diagram
- [`TO-DO.md`](TO-DO.md) — fidelity record, fixed bugs, deliberate deviations, open work
- [`AGENTS.md`](AGENTS.md) — the engineering rules this codebase is held to

## Relationship to the original

This is an independent port, not a fork. The interesting parts — the genetic algorithm, the network,
the vision geometry, the step order — are ports of the Rust code, and the eye in particular is
validated against the reference test suite. The Rust project additionally ships a WebAssembly build
and a browser UI; this port targets the desktop JVM and adds a few things the original does not
have, such as a vision overlay, charts and sound.

## References

Patryk Wychowaniec's *Learning to Fly* series explains the design from first principles:

1. [The domain](https://pwy.io/posts/learning-to-fly-pt1/) — what is simulated and why
2. [The neural network](https://pwy.io/posts/learning-to-fly-pt2/) — neurons, weights, ReLU
3. [The genetic algorithm](https://pwy.io/posts/learning-to-fly-pt3/) — fitness, selection, crossover
4. [The user interface](https://pwy.io/posts/learning-to-fly-pt4/) — wiring it all together

## License

Released under the same terms as the original Shorelark project.
