---
name: benchmark-optimizer
description: Run, extend or profile the headless optimizer grid search and analyse its JSONL results.
---

# Benchmark the Optimizer

Use this skill to measure evolutionary performance across configurations, to reproduce a result, or
to profile the simulation hot path.

## 1. Know the shape of a run

- `SimulateCommand` builds the full grid: `brainNeurons × eyeFovRange × eyeFovAngle × eyeCells ×
  gaMutChance × gaMutCoeff` (4 × 4 × 4 × 5 × 4 × 5 combinations), each repeated `SIM_ITERATIONS`
  (15) times, and trains `SIM_GENERATIONS` (30) generations per run.
- Each generation is one `train` call, which steps the simulation until `sim_generation_length`
  ticks are consumed (2500 steps by default). That is why a full grid takes hours.
- Output is **JSONL**, one `OptimizationLog` per line:
  `{"cfg":{…},"ctxt":{"generation":…,"iteration":…},"stats":{…}}`.

## 2. Run a cheap experiment first

Always validate the pipeline with a reduced run before starting the full grid:

```bash
mvn -B -ntp -q -DskipTests package
mvn -B -ntp -pl optimizer exec:java -Dexec.args="simulate -o target/opt-small.jsonl"
```

- Use `testMode` (a couple of configurations, three generations) or a temporary reduced
  `simGenerationLength` (e.g. 200) on a branch. Do not edit the constants and forget to revert
  them: they are production defaults that define the published comparison.
- Confirm the file grows and lines parse (`analyze` on a truncated file must not crash).

## 3. Run the real grid

```bash
mvn -B -ntp -pl optimizer exec:java -Dexec.args="simulate -o target/opt.jsonl"
```

- Keep the machine otherwise idle; the run is CPU-bound and parallel.
- Watch the progress log line (`Progress: done / total steps | ETA`). If the ETA grows instead of
  shrinking, you are likely thrashing memory — check heap (`-Xmx`) before blaming the code.
- Do not run two grids concurrently: they compete for cores and make timings meaningless.

## 4. Analyse

```bash
mvn -B -ntp -pl optimizer exec:java -Dexec.args="analyze -i target/opt.jsonl"
```

The command prints CSV (`brain_neurons,eye_fov_range,…,min_fitness,…,median_fitness`) averaged per
configuration. Feed it to a spreadsheet; do not eyeball raw JSONL.

Typical reading:

- Compare `avg_fitness` across `brain_neurons` to see whether a bigger brain pays for itself.
- Compare `eye_cells` / `eye_fov_angle` to see how much vision detail matters.
- `ga_mut_chance` above ~0.1 usually destroys learned weights; low chance + moderate coefficient
  learns slowly but steadily.
- A flat `max_fitness` across generations with a rising `avg_fitness` means the population is
  converging on a generalist; a rising `max` only means one lucky lineage.

## 5. Profile before optimising

- Coarse: run one generation with a JFR recording
  (`-XX:StartFlightRecording=duration=60s,filename=sim.jfr`) and inspect hot methods.
- Expected hot spots: `Neuron#propagate` (per neuron per step), `BirdEye#processVision` (per food per
  bird per step), `Vector2D` allocations.
- Optimise only measured costs, and keep the Rust-equivalent semantics. A faster-but-different
  simulation invalidates every historical comparison.

## 6. Extend the grid safely

- Add a dimension in `SimulateCommand#generateConfigurations` and in `OptimizationConfig`
  together; use a property-style value object rather than positional indices where possible.
- Keep the JSON field names stable (`a`, `b`, `c`, `d` for statistics, `c`–`h` for config) or
  version the format; `AnalyzeCommand` and any external spreadsheet depend on them.
- Record any new combination in `TO-DO.md` and in the formula above.

## 7. Verify

```bash
mvn -B -ntp spotless:apply
mvn -B -ntp -pl optimizer test
mvn -B -ntp verify
```

`SimulateCommandTest` must stay under its `@Timeout`; if the default makes it slow, use test mode
rather than raising the timeout.

## Checklist

- [ ] Cheap run validated the pipeline
- [ ] Production constants unchanged (or the change is deliberate and documented)
- [ ] JSONL parsed by `analyze` end to end
- [ ] Hot path profiled before any optimisation
- [ ] JSON/CSV schema kept stable
- [ ] `spotless:apply` + `verify` green
