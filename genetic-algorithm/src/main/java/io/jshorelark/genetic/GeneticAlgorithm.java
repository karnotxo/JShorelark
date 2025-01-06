/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.genetic;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.jshorelark.genetic.crossover.CrossoverMethod;
import io.jshorelark.genetic.mutation.MutationMethod;
import io.jshorelark.genetic.selection.SelectionMethod;
import io.jshorelark.genetic.statistics.Statistics;

import lombok.RequiredArgsConstructor;

/**
 * Genetic algorithm implementation.
 *
 * @author Jose
 * @version $Id: $Id
 */
@RequiredArgsConstructor
public class GeneticAlgorithm<I extends Individual> {
  /** Selection method. */
  private final SelectionMethod selectionMethod;

  /** Crossover method. */
  private final CrossoverMethod crossoverMethod;

  /** Mutation method. */
  private final MutationMethod mutationMethod;

  /** Factory for creating new individuals. */
  private final Individual.Factory<I> factory;

  /**
   * Creates a new genetic algorithm.
   *
   * @param selectionMethod selection method
   * @param crossoverMethod crossover method
   * @param mutationMethod mutation method
   * @param factory factory for creating new individuals
   * @return new genetic algorithm
   * @param <I> a I class
   */
  public static <I extends Individual> GeneticAlgorithm<I> create(
      SelectionMethod selectionMethod,
      CrossoverMethod crossoverMethod,
      MutationMethod mutationMethod,
      Individual.Factory<I> factory) {
    return new GeneticAlgorithm<>(selectionMethod, crossoverMethod, mutationMethod, factory);
  }

  /**
   * Evolves a population of individuals.
   *
   * @param random random number generator
   * @param population current population
   * @return pair of evolved population and statistics
   */
  @SuppressWarnings("unchecked")
  public Pair<List<I>, Statistics> evolve(Random random, List<I> population) {
    if (population.isEmpty()) {
      throw new IllegalArgumentException("Population cannot be empty");
    }

    var newPopulation = new ArrayList<I>();

    // Create new individuals
    for (int i = 0; i < population.size(); i++) {
      // Select parents
      var parentA = selectionMethod.select(random, population).getChromosome();
      var parentB = selectionMethod.select(random, population).getChromosome();

      // Create child through crossover and mutation
      var child = crossoverMethod.crossover(random, parentA, parentB);
      var mutated = child.mutate(mutationMethod, random);

      // Create new individual using the factory
      newPopulation.add(factory.create(mutated));
    }

    return new Pair<>(newPopulation, Statistics.of(population));
  }
}
