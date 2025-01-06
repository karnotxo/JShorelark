/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.genetic;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import io.jshorelark.genetic.mutation.MutationMethod;

/**
 * A chromosome containing genes.
 *
 * @author Jose
 * @version $Id: $Id
 */
public class Chromosome {
  /** The genes in this chromosome. */
  private final float[] genes;

  /** Creates a new chromosome with the given genes. */
  private Chromosome(float[] genes) {
    this.genes = genes;
  }

  /**
   * Creates a new chromosome from a stream of genes.
   *
   * @param genes a {@link java.util.stream.Stream} object
   * @return a {@link io.jshorelark.genetic.Chromosome} object
   */
  public static Chromosome fromStream(Stream<Float> genes) {
    final var list = genes.mapToDouble(Float::doubleValue).toArray();
    float[] result = new float[list.length];
    int i = 0;
    for (final var f : list) {
      result[i++] = (float) f;
    }
    return new Chromosome(result);
  }

  /**
   * Creates a chromosome from the given genes.
   *
   * @param genes a float
   * @return a {@link io.jshorelark.genetic.Chromosome} object
   */
  public static Chromosome of(float... genes) {
    return new Chromosome(genes);
  }

  /**
   * Returns an immutable list of genes.
   *
   * @return an array of {@link float} objects
   */
  public float[] getGenes() {
    return Arrays.copyOf(genes, genes.length);
  }

  /**
   * Gets a gene at the given index.
   *
   * @param index a int
   * @return a float
   */
  public float get(int index) {
    return genes[index];
  }

  /**
   * Gets the length of this chromosome.
   *
   * @return a int
   */
  public int length() {
    return genes.length;
  }

  /**
   * Returns whether this chromosome is empty.
   *
   * @return a boolean
   */
  public boolean isEmpty() {
    return genes.length == 0;
  }

  /**
   * Returns a stream of genes.
   *
   * @return a {@link java.util.stream.Stream} object
   */
  public Stream<Float> stream() {
    return IntStream.range(0, genes.length).mapToObj(i -> genes[i]);
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Chromosome)) {
      return false;
    }
    Chromosome other = (Chromosome) obj;
    if (genes.length != other.genes.length) {
      return false;
    }

    // Use approximate equality like Rust's approx::relative_eq!
    double epsilon = 1e-7;
    for (int i = 0; i < genes.length; i++) {
      double diff = Math.abs(genes[i] - other.genes[i]);
      if (diff > epsilon) {
        return false;
      }
    }
    return true;
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    return Arrays.hashCode(genes);
  }

  /**
   * Mutates this chromosome using the given mutation method.
   *
   * @param mutationMethod the mutation method to use
   * @param random the random number generator to use
   * @return the mutated chromosome
   */
  public Chromosome mutate(final MutationMethod mutationMethod, final Random random) {
    mutationMethod.mutate(random, this.genes);
    return this;
  }

  /**
   * Converts this chromosome to a float array.
   *
   * @return an array of {@link float} objects
   */
  public float[] toArray() {
    float[] result = Arrays.copyOf(genes, genes.length);
    return result;
  }
}
