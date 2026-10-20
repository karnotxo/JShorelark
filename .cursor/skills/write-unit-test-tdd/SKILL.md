---
name: write-unit-test-tdd
description: Write JUnit 6 + AssertJ tests first, port Rust behaviour tests, and keep the suite deterministic and fast.
---

# Write Unit Tests (TDD)

Use this skill when adding tests for new logic, when porting a Rust test, or when a bug needs a
regression test before the fix.

## 1. Write the failing test first

1. Name the behaviour, not the method: `returnsZeroWhenAllInputsAreNegative`,
   `evolvesPopulationOfSameSize`.
2. Put it in the matching test class (`BirdTest`, `NeuralNetworkTest`, `StatisticsTest`, …) under
   `src/test/java` with the same package.
3. Run it and confirm it fails for the **right** reason (assertion, not a compile error you did not
   expect):

```bash
mvn -B -ntp -pl <module> test -Dtest=<Class>#<method>
```

## 2. Port Rust tests when they exist

Rust tests are the fidelity contract. For each ported test:

- Keep the Rust test name and structure (`mod propagate { #[test] fn restricts_output … }` →
  `@Nested class Propagate { @Test void restrictsOutput() … }`).
- Keep the golden values and add a comment naming the Rust source
  (`// libs/neural-network/src/neuron.rs, test restrict_output`).
- Replace Rust's seeded RNG with `new Random(seed)` and document the seed choice.
- Replace `approx::assert_relative_eq!` with `.isCloseTo(expected, within(tolerance))`.

Priority order: eye vision grids, neuron/network propagation and weight round-trip, crossover
provenance, mutation boundaries, roulette-wheel distribution, statistics median (even/odd),
genetic algorithm golden population.

## 3. Follow the house style

```java
@DisplayName("Statistics")
class StatisticsTest {

  @Nested
  @DisplayName("of(population)")
  class Of {

    @Test
    @DisplayName("averages the two middle values for an even population")
    void averagesMiddleValuesForEvenPopulation() {
      var population = List.of(
          TestIndividual.withFitness(10f),
          TestIndividual.withFitness(20f),
          TestIndividual.withFitness(30f),
          TestIndividual.withFitness(40f));

      var statistics = Statistics.of(population);

      assertThat(statistics.getMedianFitness()).isCloseTo(25f, within(1e-6f));
      assertThat(statistics.getMinFitness()).isCloseTo(10f, within(1e-6f));
      assertThat(statistics.getMaxFitness()).isCloseTo(40f, within(1e-6f));
      assertThat(statistics.getAvgFitness()).isCloseTo(25f, within(1e-6f));
    }
  }
}
```

Rules:

- `@DisplayName` on the class and on non-obvious tests; `@Nested` for scenario groups.
- AssertJ only; one static import (`assertThat`).
- Fixtures are private static factory methods at the bottom of the class.
- No shared mutable state, no execution-order dependencies.
- Seeded `Random` for anything stochastic.
- `@TempDir` for files, `@Timeout` for loops.

## 4. Cover the failure paths

For every new validation, add the matching test:

```java
assertThatThrownBy(() -> NeuralNetwork.fromWeights(new int[] {3, 2}, new float[] {1f}))
    .isInstanceOf(IllegalArgumentException.class)
    .hasMessageContaining("weights");
```

Boundary values are tests too: empty list, single element, exactly the threshold, one past it.

## 5. Keep the suite fast

- Default `mvn test` for the reactor should stay well under a minute. Use tiny configurations
  (`simGenerationLength = 50`, `worldAnimals = 5`) for simulation tests.
- Tag genuinely slow tests with `@Tag("slow")`.
- Never `Thread.sleep`; never start a JavaFX toolkit; never touch the network.
- Mockito only for adapters (files, clocks, external processes).

## 6. Verify and measure

```bash
mvn -B -ntp spotless:apply
mvn -B -ntp -pl <module> test
mvn -B -ntp verify
```

Open `target/site/jacoco/index.html` for the module and check the new code is covered, including
branches. If coverage drops, add tests — do not lower the gate.

## Checklist

- [ ] Test failed first, for the expected reason
- [ ] Behavioural name + `@DisplayName` where helpful
- [ ] Rust counterpart named when ported
- [ ] Seeded randomness, `within(...)` for floats
- [ ] Failure path covered
- [ ] Suite still fast; no sleeps, no toolkit, no network
- [ ] `spotless:apply` + `verify` green, coverage read
