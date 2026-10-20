---
name: implement-genetic-operator
description: Add or change a genetic algorithm operator (selection, crossover, mutation) with tests and golden values.
---

# Implement a Genetic Operator

Use this skill when adding a `SelectionMethod`, `CrossoverMethod` or `MutationMethod`, or when
changing the behaviour of an existing one.

## 1. Confirm the contract

Read the port it implements:

- `genetic-algorithm/src/main/java/io/jshorelark/genetic/selection/SelectionMethod.java`
- `genetic-algorithm/src/main/java/io/jshorelark/genetic/crossover/CrossoverMethod.java`
- `genetic-algorithm/src/main/java/io/jshorelark/genetic/mutation/MutationMethod.java`

Read the Rust counterpart in `libs/genetic-algorithm/src/**` and write down, in the class Javadoc,
the exact rule you are reproducing (probability, range, clamping, floor value).

## 2. Decide whether it is a port or an extension

- **Port** (Rust has it): reproduce it exactly, including edge cases and the validation assertion.
  Keep the Rust name (`GaussianMutation`, `UniformCrossover`, `RouletteWheelSelection`).
- **Extension** (Java-only idea): name it descriptively, keep it off the production path, and add a
  line to `TO-DO.md` recording that it is not part of the Rust reference.

## 3. Implement

Rules that always apply:

- The method takes `Random` as a parameter; never store a `Random` field or create one.
- Never mutate inputs. Chromosomes are copied/cloned at the boundary.
- Validate arguments eagerly: equal parent lengths for crossover, `0 ≤ chance ≤ 1` for mutation,
  non-empty population for selection. Throw `IllegalArgumentException` with a message that names
  the offending value.
- Keep the operator allocation-free in the inner loop: one result array, one pass.
- No logging inside the per-gene loop; no `System.out`.
- Javadoc: what the operator does, the probability semantics, the parameter meaning, and the Rust
  counterpart if it exists.

```java
/**
 * Selects parents proportionally to their fitness.
 *
 * <p>Mirrors Rust's {@code RouletteWheelSelection}: each individual contributes
 * {@code max(fitness, 0.00001)} so that an all-zero population stays selectable.
 */
public final class RouletteWheelSelection implements SelectionMethod {
  private static final float MINIMUM_FITNESS = 0.00001f;

  @Override
  public Individual select(Random random, List<? extends Individual> population) { … }
}
```

## 4. Test it

Create or extend the matching test class under `src/test/java/...`:

- **Determinism test**: same seed → same output, asserted exactly.
- **Boundary tests**: `chance = 0` never mutates; `chance = 1` always mutates; equal-length parents;
  single-gene chromosome; empty population throws.
- **Distribution test** (selection only): 1000 draws from a known population, assert the histogram
  within a tolerance, mirroring Rust's `RouletteWheelSelection` test.
- **Provenance test** (crossover only): every child gene equals the gene at the same index of one of
  the two parents.
- **Alternative-operator test**: if you also add a Java-only operator, cover it independently; do
  not reuse the production golden values.

Use AssertJ and a seeded `Random`. For float comparisons use `isCloseTo(expected, within(1e-6f))`.

## 5. Wire it (only if the default changes)

- Production default is defined where the algorithm is constructed (`OptimizingSimulation`,
  `Simulation#evolve`). Changing the default is a **behaviour change**: update the golden tests, the
  rule documentation and `TO-DO.md`.
- Never add an `if (type == ...)` branch inside `GeneticAlgorithm`; the strategy interface exists
  for exactly that reason.

## 6. Verify

```bash
mvn -B -ntp spotless:apply
mvn -B -ntp -pl genetic-algorithm test
mvn -B -ntp verify
```

Read `target/site/jacoco/index.html` for the module: the operator must be covered, including the
validation branch.

## Checklist

- [ ] Contract and Rust counterpart documented in Javadoc
- [ ] `Random` injected, no hidden randomness
- [ ] Inputs not mutated; defensive copies where arrays cross the boundary
- [ ] Eager validation with meaningful messages
- [ ] Tests: determinism, boundaries, distribution/provenance
- [ ] Golden values re-verified if the default changed
- [ ] `spotless:apply` + `verify` green, reports read
