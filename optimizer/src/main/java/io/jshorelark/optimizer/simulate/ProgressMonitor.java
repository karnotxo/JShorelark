/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://codeberg.org/pwy/shorelark
 */
package io.jshorelark.optimizer.simulate;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;

/**
 * Periodically logs how many generations have been written and the estimated remaining time.
 *
 * <p>Mirrors the reference implementation's progress line. It is a daemon thread owned by the
 * adapter: the domain knows nothing about progress reporting.
 */
@Slf4j
@SuppressWarnings("PMD.DoNotUseThreads") // Adapter: the optimizer owns the only worker threads.
final class ProgressMonitor implements AutoCloseable {

  /** How often the line is refreshed. */
  private static final long REFRESH_MILLIS = 1_000L;

  /** How long {@link #close()} waits for the monitor to stop before interrupting it. */
  private static final long JOIN_MILLIS = 10_000L;

  private final AtomicBoolean done = new AtomicBoolean(false);
  private final AtomicInteger written;

  private final Thread thread;

  private ProgressMonitor(AtomicInteger written, int total, Instant startedAt) {
    this.written = Objects.requireNonNull(written, "written");
    this.thread = new Thread(() -> report(total, startedAt), "optimizer-progress");
    thread.setDaemon(true);
    thread.start();
  }

  /**
   * Starts the monitor.
   *
   * @param written the counter the simulations increment
   * @param total how many generations the run will produce
   * @param startedAt when the run started, used for the ETA
   * @return the running monitor
   */
  static ProgressMonitor start(AtomicInteger written, int total, Instant startedAt) {
    Objects.requireNonNull(startedAt, "startedAt");
    return new ProgressMonitor(written, total, startedAt);
  }

  @Override
  public void close() {
    done.set(true);
    try {
      thread.join(JOIN_MILLIS);
      if (thread.isAlive()) {
        log.warn("optimizer-progress did not finish in time");
        thread.interrupt();
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  private void report(int total, Instant startedAt) {
    while (!done.get()) {
      int completed = written.get();
      long elapsedSeconds = Duration.between(startedAt, Instant.now()).toSeconds();
      long etaSeconds = completed > 0 ? elapsedSeconds * (total - completed) / completed : 0L;
      log.info("{}/{} generations | eta {}s", completed, total, etaSeconds);
      if (sleep()) {
        return;
      }
    }
  }

  private static boolean sleep() {
    try {
      Thread.sleep(REFRESH_MILLIS);
      return false;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return true;
    }
  }
}
