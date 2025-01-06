/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.genetic.selection;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import io.jshorelark.genetic.Individual;
import io.jshorelark.genetic.TestIndividual;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TournamentSelectionTest {
  @Test
  void select() {
    var rng = new Random(42);
    var selection = new TournamentSelection(2);

    var population =
        List.of(
            TestIndividual.withFitness(2.0f),
            TestIndividual.withFitness(1.0f),
            TestIndividual.withFitness(4.0f),
            TestIndividual.withFitness(3.0f));

    Map<Float, Integer> histogram =
        IntStream.range(0, 1000)
            .<Individual>mapToObj(i -> selection.select(rng, population))
            .collect(
                Collectors.groupingBy(
                    Individual::getFitness,
                    Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));

    assertEquals(4, histogram.size());
    assertTrue(histogram.get(1.0f) < histogram.get(2.0f));
    assertTrue(histogram.get(2.0f) < histogram.get(3.0f));
    assertTrue(histogram.get(3.0f) < histogram.get(4.0f));
  }
}
