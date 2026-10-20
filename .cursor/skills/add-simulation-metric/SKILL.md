---
name: add-simulation-metric
description: Add a measured value (statistic, counter, chart series) to the simulation and surface it in the UI or optimizer.
---

# Add a Simulation Metric

Use this skill when you need to measure something new: a per-bird counter, a per-generation
statistic, a per-run optimizer field.

## 1. Decide the layer

| Kind of metric | Where it belongs | Why |
|---|---|---|
| Per-bird state (e.g. food eaten, distance flown) | `simulation.bird.Bird` field + accessor | Domain state |
| Per-generation aggregate (min/max/avg/median of anything) | `genetic.statistics.Statistics` or a new domain record | One source of truth, reusable by UI and optimizer |
| Per-run run metadata (generation index, iteration, config) | `optimizer.Optimization*` value type | Adapter/log concern |
| Display-only formatting (pixels, labels, "12.3") | `simulation-ui` | Presentation |

**Never** compute a domain metric in the UI or in `EvolutionManager`. If the chart needs it, the
domain returns it.

## 2. Model it

- Prefer an immutable record: `public record GenerationSnapshot(int generation, float avgFitness, int totalFoodEaten) {}`.
- Add the aggregate to the existing statistics type if it belongs to the same "one value per
  generation" group; add a sibling record if it describes a different concern (do not grow a
  god-record).
- Keep the unit in the name (`…Steps`, `…FoodItems`, `…Millis`) and document it in Javadoc.
- If the metric needs to be persisted (optimizer JSONL), pin its JSON shape with `@JsonProperty` and
  treat it as a contract (see `OptimizationStatistics`).

## 3. Accumulate it correctly

- Per-step counters increment inside the domain step that owns the event, next to the effect they
  measure (satiation increments where food is consumed).
- Per-generation aggregates are computed once, from the **population that was just evaluated**, at
  the same point where `Statistics.of(population)` runs.
- Reset counters at exactly the same place the domain resets `age`/`generation` (the evolution
  step). A metric that outlives its generation is a bug waiting to happen.
- Keep the hot path allocation-free: no streams per bird per tick.

## 4. Expose it

- **Domain**: a plain accessor on the owning type (`int getFoodEaten()` / record accessor).
- **UI**: `EvolutionManager` delegates; the chart or a label formats it.
- **Optimizer**: add the field to `OptimizationStatistics` (and `fromGaStats`), then to the JSON
  mapping. Update `AnalyzeCommand`'s CSV header and row in the same change so the analysis stays
  usable.
- **i18n/format**: format at the edge (`String.format("%.2f", …)` or `formatter`), never store
  formatted strings in the domain.

## 5. Test

- Domain: accumulate over a controlled scenario (a bird placed on a food item eats exactly once per
  step it overlaps, food respawns elsewhere).
- Boundary: zero population / zero food still produces a well-defined metric (0 or documented
  `Double.NaN`—pick one and test it).
- Statistics: hand-computed expected values for a small population, including the even-size median.
- Optimizer: extend the existing round-trip test that serialises an `OptimizationLog` and reads it
  back; assert the new field survives.
- Do not test the UI formatting; extract the formatting into a pure helper if it is non-trivial and
  test that.

## 6. Update documentation

- `TO-DO.md` — tick or add the entry.
- `README.md` / `src/docs/asciidoc/index.adoc` — only if the metric is user-visible in the walkthrough.
- `.cursor/rules/shorelark-simulation.mdc` — if the metric encodes a rule (a new cliff, a new
  trigger).

## 7. Verify

```bash
mvn -B -ntp spotless:apply
mvn -B -ntp -pl simulation,optimizer,simulation-ui test
mvn -B -ntp verify
```

## Checklist

- [ ] Metric computed in exactly one layer
- [ ] Unit and meaning documented
- [ ] Reset at the same point as the generation reset
- [ ] JSON/CSV shape updated together if persisted
- [ ] Immutable snapshot type, no mutable aggregates crossing layers
- [ ] Domain + round-trip tests added
- [ ] `spotless:apply` + `verify` green
