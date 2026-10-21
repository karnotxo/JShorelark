# TO-DO - JShorelark

Backlog and fidelity record against the Rust implementation
([pwy/shorelark](https://codeberg.org/pwy/shorelark)) and the
[Learning to Fly](https://pwy.io/posts/learning-to-fly-pt1/) series (parts 1-4).

The Rust crates in `libs/*` are the specification. Every deviation listed here is either a fixed
bug (kept for the record) or a deliberate decision with its rationale. Anything not listed is
intended to match Rust behaviour exactly.

## Status

| Area | State |
|---|---|
| Build (`mvn -B -ntp verify`) | Green, 7/7 modules |
| `genetic-algorithm` | Faithful |
| `neural-network` | Faithful |
| `simulation` | Faithful (vision grid test suite ported) |
| `optimizer` | Faithful |
| `simulation-ui` | Adapter; intentionally beyond Rust (see below) |

## Fixed fidelity bugs

### 1. Vision angle was mirrored (`BirdEye`)

**Symptom.** Birds perceived left and right swapped, so the network learned a mirrored steering
policy; the population still improved, which is why this survived review.

**Root cause.** The port used `atan2(vec.x, vec.y)`. Rust calls
`na::Rotation2::rotation_between(&na::Vector2::y(), &vec).angle()`, and nalgebra defines
`rotation_between` as `atan2(y.perp(vec), y.dot(vec))`. For 2D vectors the cross product
`y.perp(vec)` is `-vec.x`, so the reference formula is:

```
atan2(-vec.x, vec.y)
```

A unit test could not have caught this alone: every symmetric scenario looks correct under a
mirror. The reference `eye.rs` test suite does catch it, because its ASCII vision grids are
asymmetric.

**Fix.** `BirdEye#processVision` now uses `atan2(-offset.x(), offset.y())`, and the whole
`eye.rs` test suite was ported as `BirdEyeTest.ReferenceGrids` (49 parameterised cases covering
field-of-view range, field-of-view angle, bird position and rotation). The wrong formula was also
present in `.cursor/rules/`, which is how it survived; both rules files now document the correct
formula and cite the failing test.

### 2. Rotation was stored in `[0, 2π)` instead of `[-π, π]`

**Symptom.** Food exactly on the rear boundary was binned differently from Rust, which shifts the
learned policy at the edge of the field of view.

**Root cause.** `Bird` normalised the heading with a modulo into `[0, 2π)`. Rust stores a
`Rotation2` and reports `angle()`, i.e. `atan2(sin, cos)`, which is `[-π, π]`. Two further details
matter and are now reproduced:

- `Rotation2::angle()` returns `atan2(sin r, cos r)`, not the `f32` the caller passed in. The round
trip maps `3π/2` to `-π/2`.
- `na::wrap` shifts by whole periods only while the value is **strictly** outside the bounds, so
`+π` stays `+π`. A modulo formulation silently maps it to `-π`.

**Fix.** `Bird#normalizeAngle` wraps into `[-π, π]` with strict comparisons, and `BirdEye` reads the
heading through `atan2(sin, cos)` before subtracting it.

### 3. Eaten food was looked up by identity

**Symptom.** With two food items at the same position, the wrong item could be respawned.

**Root cause.** `World#respawnFood(consumed, replacement)` used `indexOf`, and `Food` equality is
positional.

**Fix.** `World#replaceFood(index, replacement)` addresses the slot directly, matching Rust's
`food.position = rng.gen()`, where the loop index is the identity. `Simulation` iterates by index
and calls it before the listener fires, so the event still reports the item the bird actually ate.

### 4. `Food` carried a `consumed` flag

Rust's `Food` is a bare `Point2`; consumption is a repositioning, not a state bit. The unused flag
was removed and `Food` became a record with `position()`.

## Deliberate deviations from Rust

| Deviation | Rationale |
|---|---|
| `SinglePointCrossover`, `TournamentSelection`, `RandomResetMutation` exist but are not wired | Rust ships only uniform crossover, roulette selection and Gaussian mutation. These are extra strategies behind the same interfaces, kept for experimentation; `Config` and the simulation still use the Rust trio. |
| `Simulation` exposes listeners (`CollisionListener`) | Rust's WASM bridge reads state from JavaScript each frame. A listener is the Java-idiomatic equivalent and keeps the domain free of UI types. |
| `Statistics` is computed per generation and returned, not held in a mutable field | Same values, but the caller owns the history (the UI chart, the optimizer log). |
| UI adds sound, splash screen, charts, theming, hover inspector, speed control | The Rust web UI has no such features. They live entirely in `simulation-ui` and never touch domain rules. |
| Optimizer writes JSONL through a single writer thread | Rust uses the same single-writer design; Java expresses it with `JsonlLogWriter` instead of a channel + thread in the command itself. |

## Fidelity risks still open

- [x] **Golden evolution test.** `GoldenEvolutionTest` runs 15 generations from a fixed seed and
  asserts the exact min/max/avg/median of each one, a digest of every final bird (which also covers
  the bias-first weight order) and that the median climbs. The values are a regression lock
  produced by this implementation, not an oracle; the Rust-recorded fixture below is still the
  remaining gap.
- [x] **`f32` vs `f64` in the optimizer analysis.** Investigated and left in `f32` on purpose.
  Rust's `AnalyzeCmd` accumulates into `OptStatistics`, whose fields are `f32`, so a `double`
  accumulator would print different digits than the reference. Measured over a full grid point
  (450 samples), the mean drifts about 1e-6 relative — two orders of magnitude below the 7
  significant digits the CSV prints. `FitnessAccumulatorTest` records that reasoning and pins the
  printed output, so the choice is a decision rather than an oversight.
- [ ] **Thread-local randomness in the optimizer.** `SimulateCommand` uses `ThreadLocalRandom`, so a
  grid search is not reproducible. Rust seeds per configuration. Add an optional `--seed` that
  derives a per-configuration `Random` from it.
- [ ] **No cross-check against a recorded Rust run.** The single strongest remaining validation
  would be a small set of configurations whose per-generation fitness is recorded from the Rust
  optimizer and asserted here.

## Engineering debt

- [x] `SimulationControls.SimulationApplicationWrapper` duplicated the application's public surface
  just to hide it. `SimulationCommands` now names the contract the panel uses and
  `SimulationApplication` implements it, so the wrapper is gone and the panel is substitutable.
- [x] UI strings were inline English literals. Chrome, tooltips, readouts, chart labels and the
  About dialog now live in `messages.properties` behind `Messages`, with `MessagesTest` failing on a
  key that has no value. The embedded terminal's welcome text and its command error messages are
  still inline: they are a command reference rather than an interface, so they move when the
  terminal itself gets a help system.
- [x] `SimulationView` mixes layout, drawing and hit-testing. It is coherent today, but splitting
  the bird inspector out would keep the paint path minimal.
- [ ] `mockito` warns about self-attaching its agent on JDK 21. Add it as a
  `-javaagent` in the Surefire configuration before it becomes an error.
- [ ] `rust-ref/` holds the reference Rust sources read during the audit and is committed, but it
  is not part of the build and nothing links to it. Either cite it from the docs as the pinned
  reference (with the upstream commit it was taken from) or drop it; silently drifting copies of
  the specification are worse than none.

## CI

- [x] `.github/workflows/ci.yml` previously targeted JDK 17 and invoked things that do not exist in
  the POM (a `benchmark` profile, a `performance-tests` profile, JMH, JMeter, SonarCloud, Snyk,
  AsciiDoc generation), so it could never pass. It is now a single `mvn -B -ntp verify` job on
  JDK 21 across Linux and Windows, uploading the quality reports only when a gate fails.
- [ ] Decide whether dependency and vulnerability scanning come back. If they do, they need real
  configuration (Dependabot or Renovate rather than a `versions:` goal that only prints), not the
  placeholder steps the old workflow carried.

## Future ideas (from the blog series)

- **Part 1-2.** The series explains ReLU vs `tanh`. `ActivationFunction` is already a `sealed`
interface, so adding `TANH` is a one-line, one-test change if a comparison is ever wanted.
- **Part 3.** Backpropagation is deliberately absent: Shorelark evolves weights instead of training
them. Worth a README note so readers do not expect a training loop.
- **Part 4.** The web UI draws food as circles and birds as triangles; the JavaFX renderer does the
same and adds a vision overlay, which is the most useful debugging view the Rust original lacks.
- **Beyond the series.** Elitism (carrying the best bird over unchanged) would speed up early
learning, but it is a behavioural deviation and must not be enabled in the faithful default
configuration.
