/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.genetic;

import lombok.Value;

/**
 * A simple pair class to mimic Rust's tuple return types.
 *
 * @author Jose
 * @version $Id: $Id
 */
@Value
public class Pair<F, S> {
  /** The first element. */
  F first;

  /** The second element. */
  S second;

  /**
   * Gets the first element.
   *
   * @return a F object
   */
  public F getFirst() {
    return first;
  }

  /**
   * Gets the second element.
   *
   * @return a S object
   */
  public S getSecond() {
    return second;
  }
}
