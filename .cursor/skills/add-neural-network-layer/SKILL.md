---
name: add-neural-network-layer
description: Change network topology or add a layer capability without breaking the weight serialisation contract.
---

# Add or Change a Neural Network Layer

Use this skill when changing `Layer`, `Neuron`, `NeuralNetwork`, `LayeredNetwork`, the topology, or
the activation used by birds.

## 1. Understand the serialisation contract

The chromosome is the network's flat weight vector. Its order is a **public contract**:

```
for each layer (input→hidden, hidden→output):
  for each neuron in that layer:
    bias
    inputWeight[0] … inputWeight[inputSize-1]
```

`NeuralNetwork#weights()` and `NeuralNetwork#fromWeights(topology, weights)` must agree exactly.
Changing the order invalidates every saved chromosome, breaks the GA's crossover (which blends
vectors index-by-index) and breaks golden tests.

**Rule:** if you must change the layout, bump a version marker on the persisted format and document
the migration in `TO-DO.md`. Never change it silently.

## 2. Check the Rust reference

- `libs/neural-network/src/layer.rs` — `Layer::random`, `Layer::from_weights`, `propagate`.
- `libs/neural-network/src/neuron.rs` — bias-first consumption from the weight iterator, ReLU.
- `libs/neural-network/src/lib.rs` — `Network::random`, `from_weights`, `propagate`, `weights`.
- `libs/neural-network/src/layer_topology.rs` — topology validation.

Rust asserts `layers.len() > 1`, `!weights.is_empty()`, equal input/weight lengths, and rejects
leftover weights with `panic!("got too many weights")`. Java must throw
`IllegalArgumentException` with equivalent messages.

## 3. Implement

- A layer holds `List<Neuron>`; a neuron holds `float bias` and `float[] weights`.
- Both clone their arrays at the boundary (SpotBugs `EI_EXPOSE_REP`).
- `Layer#propagate` validates input length once, then loops; do not validate per neuron.
- `NeuralNetwork#propagate` validates against the first layer's input width and iterates layers.
- Topology validation lives in `matchesTopology` and in `fromWeights`; write both directions
  (too few weights, too many weights).
- Activation stays ReLU in the production path. `ActivationFunction` is an extension point; adding a
  call to it inside `Neuron` changes every trained result, so treat it as a behaviour change.

## 4. Test

Extend `LayerTest`, `NeuralNetworkTest` or `ActivationFunctionTest`:

- `fromWeights(network.weights())` round-trips for a seeded random network (topology `[3,2,1]`).
- Weight order: build a layer with known weights, assert bias-first ordering.
- `propagate` equals manual `bias + Σ(input × weight)` then `max(0, …)`.
- Wrong input length, too few weights, too many weights → `IllegalArgumentException` with a
  message naming the sizes.
- ReLU boundary: negative input → `0`, zero → `0`, positive → unchanged.

## 5. Wire the topology

Topology comes from `Config#getBrainTopology()` → `[eyeCells, brainNeurons, 2]`. If you add a layer:

1. Add the parameter to `Config` with a Rust-reference table entry.
2. Update `getBrainTopology()`.
3. Verify `BirdBrain#processInputs` still reads exactly `response[0]` (speed) and `response[1]`
   (rotation); a new output changes the brain contract and needs a documented decision.

## 6. Verify

```bash
mvn -B -ntp spotless:apply
mvn -B -ntp -pl neural-network test
mvn -B -ntp verify
```

## Checklist

- [ ] Weight order unchanged (or explicitly migrated and documented)
- [ ] Defensive copies on every array boundary
- [ ] Input/weight length validation with clear messages
- [ ] Round-trip, ordering, propagation and error tests added
- [ ] Topology documented in `Config` and in `shorelark-simulation` rule table
- [ ] `spotless:apply` + `verify` green
