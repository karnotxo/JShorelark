/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.simulation.physics;

import java.util.Random;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Vector2D")
class Vector2DTest {
  private static final float EPSILON = 0.0001f;

  @Nested
  @DisplayName("random")
  class RandomTest {
    @Test
    @DisplayName("creates random vector within bounds")
    void createsRandomVector() {
      var random = new Random(42); // Fixed seed for reproducibility
      var vector = Vector2D.random(random);
      assertTrue(vector.x() >= 0 && vector.x() <= 1);
      assertTrue(vector.y() >= 0 && vector.y() <= 1);
    }
  }

  @Nested
  @DisplayName("add")
  class Add {
    @Test
    @DisplayName("adds two vectors")
    void addsTwoVectors() {
      var v1 = new Vector2D(1.0f, 2.0f);
      var v2 = new Vector2D(3.0f, 4.0f);
      var sum = v1.add(v2);
      assertEquals(4.0f, sum.x(), EPSILON);
      assertEquals(6.0f, sum.y(), EPSILON);
    }
  }

  @Nested
  @DisplayName("subtract")
  class Subtract {
    @Test
    @DisplayName("subtracts two vectors")
    void subtractsTwoVectors() {
      var v1 = new Vector2D(4.0f, 6.0f);
      var v2 = new Vector2D(1.0f, 2.0f);
      var diff = v1.subtract(v2);
      assertEquals(3.0f, diff.x(), EPSILON);
      assertEquals(4.0f, diff.y(), EPSILON);
    }
  }

  @Nested
  @DisplayName("scale")
  class Scale {
    @Test
    @DisplayName("scales vector by factor")
    void scalesByFactor() {
      var vector = new Vector2D(2.0f, 3.0f);
      var scaled = vector.scale(2.0f);
      assertEquals(4.0f, scaled.x(), EPSILON);
      assertEquals(6.0f, scaled.y(), EPSILON);
    }
  }

  @Nested
  @DisplayName("length")
  class Length {
    @Test
    @DisplayName("calculates correct length")
    void calculatesCorrectLength() {
      var vector = new Vector2D(3.0f, 4.0f);
      assertEquals(5.0f, vector.length(), EPSILON);
    }

    @Test
    @DisplayName("returns zero for zero vector")
    void returnsZeroForZeroVector() {
      assertEquals(0.0f, Vector2D.zero().length(), EPSILON);
    }
  }

  @Nested
  @DisplayName("normalize")
  class Normalize {
    @Test
    @DisplayName("normalizes non-zero vector")
    void normalizesNonZeroVector() {
      var vector = new Vector2D(3.0f, 4.0f);
      var normalized = vector.normalize();
      assertEquals(1.0f, normalized.length(), EPSILON);
      assertEquals(0.6f, normalized.x(), EPSILON);
      assertEquals(0.8f, normalized.y(), EPSILON);
    }

    @Test
    @DisplayName("returns zero for zero vector")
    void returnsZeroForZeroVector() {
      var normalized = Vector2D.zero().normalize();
      assertEquals(Vector2D.zero(), normalized);
    }
  }

  @Nested
  @DisplayName("rotate")
  class Rotate {
    @Test
    @DisplayName("rotates vector by angle")
    void rotatesByAngle() {
      var vector = Vector2D.right();
      var rotated = vector.rotate((float) Math.PI / 2);
      assertEquals(0.0f, rotated.x(), EPSILON);
      assertEquals(1.0f, rotated.y(), EPSILON);
    }
  }

  @Nested
  @DisplayName("distance")
  class Distance {
    @Test
    @DisplayName("calculates distance between vectors")
    void calculatesDistance() {
      var v1 = new Vector2D(1.0f, 1.0f);
      var v2 = new Vector2D(4.0f, 5.0f);
      assertEquals(5.0f, v1.distance(v2), EPSILON);
    }
  }
}
