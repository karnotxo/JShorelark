# AGENTS.md - JShorelark

Guide for agents and developers working in this repository. Read this file and `.cursor/rules/`
before any structural change.

## Scope of agent work (hard rules)

- Work on the **local copy** of this project only. Do **not** clone to the cloud, launch cloud
  agents, open PRs, or push unless the user explicitly asks for that specific action.
- Do **not** `git commit`, `git push`, or use `gh` unless the user explicitly requests it.
- Do **not** invent secrets, rewrite credentials, or print values from config / properties files.
  Prefer placeholders when discussing config.
- Prefer editing the existing multi-module tree; do not mass-reorganise packages without need.
- Talk to the user in **Spanish**. Code, identifiers, Javadoc, commit messages and technical docs:
  **English**.

## What this project is

`JShorelark` is a **faithful Java 21 / JavaFX port** of
[Shorelark](https://codeberg.org/pwy/shorelark) (originally
[patryk27/shorelark](https://github.com/patryk27/shorelark)): a bird evolution simulator that
demonstrates a genetic algorithm driving a population of feed-forward neural networks.

Birds live in a unit-square toroidal world, see food through a segmented field-of-view "eye", and
steer with the output of their own neural network. Every `sim_generation_length` steps the
population is scored by food eaten (satiation), then replaced by the offspring of
fitness-proportionate selection, uniform crossover and Gaussian mutation.

Reference material:

- Rust sources (ground truth for behaviour): `libs/genetic-algorithm`, `libs/neural-network`,
  `libs/simulation`, `libs/optimizer`, `libs/simulation-wasm`, `www/`.
- Blog series: `https://pwy.io/posts/learning-to-fly-pt1/` … `pt4`.

**The Rust implementation is the specification.** A Java behaviour that differs from Rust is a bug
unless a deliberate, documented decision says otherwise (`TO-DO.md` records those decisions).

## Modules

| Module             | Role                                                                    | Purity                          |
|--------------------|-------------------------------------------------------------------------|---------------------------------|
| `config-resources` | Shared Checkstyle/PMD/Log4j2 resources, imported as a dependency         | resources only                  |
| `genetic-algorithm`| Chromosome, selection, crossover, mutation, statistics                   | pure Java, no UI                |
| `neural-network`   | Layer/Neuron/Network, topology, weight serialisation                     | pure Java, no UI                |
| `simulation`       | World, Animal/Bird, Eye, Brain, Food, collision + generation stepping     | pure Java, no JavaFX / no I/O   |
| `optimizer`        | Headless CLI: grid-search configs, JSONL logs, analysis (PicoCLI)         | adapter (files, console)        |
| `simulation-ui`    | JavaFX application, Canvas renderer, controls, charts, audio              | adapter (JavaFX)                |

Dependency direction is inward only:

```
simulation-ui -> optimizer -> simulation -> neural-network -> genetic-algorithm
      |                              |
      +------------------------------+
                 (read-only views)
```

Domain modules (`genetic-algorithm`, `neural-network`, `simulation`) must **never** import
`javafx.*`, `java.io` file APIs, or any UI type. `simulation-ui` owns every pixel and every
keyboard shortcut; it observes domain state and never mutates it directly.

Entry points:

- Desktop UI: `io.jshorelark.simulation.ui.SimulationApplication`
- Optimizer CLI: `io.jshorelark.optimizer.Main` (`simulate`, `analyze`)

## Stack (facts from the POMs)

- **JDK 21** (`maven.compiler.release=21`), no preview features.
- **Maven** multi-module; groupId `io.jshorelark`; version `1.0-SNAPSHOT`.
- **JavaFX 21 LTS** (`javafx-controls`, `javafx-media`).
- **Jackson** for JSON / JSONL persistence.
- **PicoCLI** for the optimizer CLI.
- **Lombok** + **RecordBuilder** to remove boilerplate.
- **SLF4J** + **Log4j2** for logging.
- **Tests**: JUnit 6 (`junit-bom`), AssertJ, Mockito.
- **Quality**: Spotless (`google-java-format`), Checkstyle (`google_checks.xml`), SpotBugs,
  PMD/CPD, JaCoCo.

Every version is a property in the **root** `pom.xml`. Modules never hardcode a version that the
parent already manages.

## Conventions for agents

- Read `.cursor/rules/` first. The rules are the long form of this section.
- Prefer `.cursor/skills/` for repeatable workflows: genetic operators, neural-network layers,
  simulation metrics, TDD tests, optimizer benchmarks.
- Randomness always arrives through an injected `Random` (or `SplittableRandom`); never call
  `Math.random()`, never create `new Random()` inside domain logic.
- One source of truth per rule: collision distance lives in `Config`, generation length lives in
  `Config`, statistics live in one calculator.
- Do not add Maven dependencies without justification (need, maintenance, licence, footprint).
- User-facing strings belong in the UI layer and must stay easy to externalise.

## Useful Maven commands

Run from the repo root:

```bash
mvn -B -ntp spotless:apply          # format (google-java-format)
mvn -B -ntp -DskipTests package     # fast compile/package
mvn -B -ntp test                    # unit tests
mvn -B -ntp verify                  # full gate: Spotless, Checkstyle, SpotBugs, PMD, JaCoCo
mvn -B -ntp -pl simulation test     # module-scoped
mvn -B -ntp javadoc:javadoc
mvn -B -ntp -pl simulation-ui javafx:run
```

Read the reports, not only the exit code: `target/checkstyle-result.xml`, `target/pmd.xml`,
`target/spotbugsXml.xml`, `target/site/jacoco/index.html`.

## Definition of done (per change)

- Builds with JDK 21 on the user's machine (Windows primary; Linux/CI secondary).
- `mvn -B -ntp spotless:apply` run; Spotless clean for every touched file.
- `mvn -B -ntp verify` green, with Checkstyle/SpotBugs/PMD/JaCoCo reports actually inspected.
- Tests for non-trivial logic (JUnit 6 + AssertJ); no coverage regression below the module gate.
- Behaviour changes cite the Rust counterpart (`file::function`) or record the decision in `TO-DO.md`.
- No `TODO` without an owner; no commented-out code.
- Docs updated when behaviour, modules or the build change.
- No commit/push unless requested.

## Engineering directives (non-negotiable)

The long form lives in `.cursor/rules/shorelark-engineering.mdc`. Summary:

1. **SOLID, KISS, DRY** — one responsibility per type; extend with sealed subtypes or strategy
   implementations instead of adding `if` branches; exactly one source of truth per rule.
2. **Newest stable technology** — JDK 21 final features only; dependencies pinned in the root
   `pom.xml`; verify an API against the version on the classpath before using it.
3. **Hexagonal architecture** — the domain is pure Java behind small ports
   (`Function<…>`, `Random`, listeners); `optimizer` and `simulation-ui` are adapters that own I/O,
   JavaFX and the clock.
4. **Static analysis counts** — Spotless, Checkstyle, SpotBugs, PMD/CPD and JaCoCo are wired gates;
   fix the defect, never the message, and never lower a gate to get green.
5. **Google style, faithfully** — `google-java-format` is the single source of truth; Javadoc on
   public domain APIs; comments explain *why*, never narrate code.
