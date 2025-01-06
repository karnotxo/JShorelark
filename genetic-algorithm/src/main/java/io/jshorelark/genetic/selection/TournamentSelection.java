/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.genetic.selection;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.jshorelark.genetic.Individual;

/**
 * Implements tournament selection. Randomly selects a group of individuals and chooses the one with
 * highest fitness. Tournament size controls selection pressure - larger tournaments favor fitter
 * individuals more strongly. This class is equivalent to the TournamentSelection struct in the Rust
 * implementation.
 *
 * @author Jose
 * @version $Id: $Id
 */
public final class TournamentSelection implements SelectionMethod {

  private final int tournamentSize;

  /**
   * Creates a new tournament selection with the specified tournament size.
   *
   * @param tournamentSize the number of individuals to compete in each tournament
   * @throws java.lang.IllegalArgumentException if tournamentSize is less than 1
   */
  public TournamentSelection(int tournamentSize) {
    if (tournamentSize < 1) {
      throw new IllegalArgumentException("Tournament size must be at least 1");
    }
    this.tournamentSize = tournamentSize;
  }

  /** {@inheritDoc} */
  @Override
  public Individual select(Random random, List<? extends Individual> population) {
    if (population == null || population.isEmpty()) {
      throw new IllegalArgumentException("Population cannot be null or empty");
    }

    // Use actual tournament size or population size, whichever is smaller
    int actualTournamentSize = Math.min(tournamentSize, population.size());

    // Select random participants
    List<Individual> tournament = new ArrayList<>(actualTournamentSize);
    for (int i = 0; i < actualTournamentSize; i++) {
      int index = random.nextInt(population.size());
      tournament.add(population.get(index));
    }

    // Find the winner (individual with highest fitness)
    return tournament.stream()
        .max((a, b) -> Float.compare(a.getFitness(), b.getFitness()))
        .orElseThrow(); // Can't be empty as we just added items
  }
}
