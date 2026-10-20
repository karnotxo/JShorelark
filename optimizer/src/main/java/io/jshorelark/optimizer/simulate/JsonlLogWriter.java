/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://codeberg.org/pwy/shorelark
 */
package io.jshorelark.optimizer.simulate;

import io.jshorelark.optimizer.OptimizationLog;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

/**
 * Single-threaded JSONL sink for the optimizer's grid search.
 *
 * <p>Mirrors the reference implementation's writer thread: simulations hand entries over through an
 * unbounded queue, so a worker never blocks on disk, and exactly one thread touches the file. The
 * queue is unbounded on purpose — the writer only slows down, it never drops results.
 */
@Slf4j
@SuppressWarnings("PMD.DoNotUseThreads") // Adapter: the optimizer owns the only worker threads.
public final class JsonlLogWriter implements AutoCloseable {

  /** How long the writer blocks on the queue before re-checking the shutdown flag. */
  private static final long POLL_MILLIS = 50L;

  /** How long {@link #close()} waits for the writer to drain before interrupting it. */
  private static final long JOIN_MILLIS = 10_000L;

  private final LinkedBlockingQueue<OptimizationLog> queue = new LinkedBlockingQueue<>();
  private final AtomicBoolean closed = new AtomicBoolean(false);
  private final Thread thread;

  private JsonlLogWriter(Path outputPath, ObjectMapper mapper) {
    this.thread = new Thread(() -> writeAll(outputPath, mapper), "optimizer-writer");
    thread.setDaemon(true);
    thread.start();
  }

  /**
   * Starts the writer.
   *
   * @param outputPath the JSONL file to create or truncate
   * @param mapper the mapper that renders each entry
   * @return the running writer
   */
  public static JsonlLogWriter start(Path outputPath, ObjectMapper mapper) {
    Objects.requireNonNull(outputPath, "outputPath");
    Objects.requireNonNull(mapper, "mapper");
    return new JsonlLogWriter(outputPath, mapper);
  }

  /**
   * Queues one entry for the writer.
   *
   * @param entry the entry to persist
   */
  public void submit(OptimizationLog entry) {
    queue.add(Objects.requireNonNull(entry, "entry"));
  }

  @Override
  public void close() {
    closed.set(true);
    try {
      thread.join(JOIN_MILLIS);
      if (thread.isAlive()) {
        log.warn("optimizer-writer did not finish in time");
        thread.interrupt();
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  private void writeAll(Path outputPath, ObjectMapper mapper) {
    try (BufferedWriter out = Files.newBufferedWriter(outputPath)) {
      while (true) {
        OptimizationLog entry = queue.poll(POLL_MILLIS, TimeUnit.MILLISECONDS);
        if (entry == null) {
          if (closed.get() && queue.isEmpty()) {
            return;
          }
          continue;
        }
        out.write(mapper.writeValueAsString(entry));
        out.newLine();
      }
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to write " + outputPath, e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
