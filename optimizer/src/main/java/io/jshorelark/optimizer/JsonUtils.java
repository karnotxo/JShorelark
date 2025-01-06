/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.optimizer;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Utility class for JSON serialization.
 *
 * @author Jose
 * @version $Id: $Id
 */
public final class JsonUtils {
  /** Shared ObjectMapper instance for JSON serialization. */
  public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private JsonUtils() {
    // Utility class
  }
}
