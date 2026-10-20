/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://codeberg.org/pwy/shorelark
 */
package io.jshorelark.simulation.ui.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import lombok.extern.slf4j.Slf4j;

/**
 * Synthesises and plays the two short UI sounds of the simulation.
 *
 * <p>The clips are generated at startup instead of shipped as assets, so the port has no binary
 * audio dependency. When the platform has no audio line the clips stay {@code null} and playback
 * becomes a no-op; sound is never a reason to fail the simulation.
 */
@Slf4j
public class SoundManager {

  private static final float SAMPLE_RATE = 44100f;
  private static final int SAMPLE_SIZE = 16;
  private static final int CHANNELS = 1;
  private static final boolean SIGNED = true;
  private static final boolean BIG_ENDIAN = true;

  /** Full-scale amplitude of a 16-bit signed sample. */
  private static final double MAX_AMPLITUDE = 32767.0;

  private final Clip foodSound;
  private final Clip generationSound;
  private boolean enabled = true;
  private float volume = 0.5f;

  /** Creates the two synthesised clips. */
  public SoundManager() {
    this.foodSound = createFoodSound();
    this.generationSound = createGenerationSound();
  }

  private static Clip createFoodSound() {
    // A 100 ms "pop": 880 Hz gliding down to 440 Hz with a linear fade.
    int duration = (int) (0.1 * SAMPLE_RATE);
    byte[] data = new byte[duration * 2];

    double frequency = 880.0;
    double freqStep = (440.0 - 880.0) / duration;

    for (int i = 0; i < duration; i++) {
      frequency += freqStep;
      double angle = 2.0 * Math.PI * frequency * i / SAMPLE_RATE;
      double amplitude = Math.max(0.0, 1.0 - (double) i / duration) * MAX_AMPLITUDE;
      writeSample(data, i, (short) (Math.sin(angle) * amplitude));
    }

    return createClip(data);
  }

  private static Clip createGenerationSound() {
    // A 500 ms C-major triad with vibrato and an ADSR envelope.
    int duration = (int) (0.5 * SAMPLE_RATE);
    byte[] data = new byte[duration * 2];
    double[] frequencies = {523.25, 659.25, 783.99};

    for (int i = 0; i < duration; i++) {
      double time = i / SAMPLE_RATE;
      double envelope = adsrEnvelope(i, duration);
      double mixed = 0.0;

      for (double freq : frequencies) {
        double vibrato = 1.0 + 0.01 * Math.sin(2 * Math.PI * 5 * time);
        mixed += Math.sin(2.0 * Math.PI * freq * vibrato * time) * envelope;
      }

      double normalised = mixed / frequencies.length * MAX_AMPLITUDE;
      writeSample(data, i, (short) Math.clamp(normalised, -MAX_AMPLITUDE, MAX_AMPLITUDE));
    }

    return createClip(data);
  }

  /** Stores a 16-bit big-endian sample at the given frame index. */
  private static void writeSample(byte[] data, int frame, short sample) {
    data[frame * 2] = (byte) (sample >> 8);
    data[frame * 2 + 1] = (byte) (sample & 0xFF);
  }

  /**
   * Evaluates a linear ADSR envelope at a sample position.
   *
   * @param sample the frame index
   * @param duration the total number of frames
   * @return the envelope gain in {@code [0, 1]}
   */
  private static double adsrEnvelope(int sample, int duration) {
    double attackTime = 0.1;
    double decayTime = 0.2;
    double sustainLevel = 0.7;
    double releaseTime = 0.3;
    double position = (double) sample / duration;

    if (position < attackTime) {
      return position / attackTime;
    }
    if (position < attackTime + decayTime) {
      double decayPosition = (position - attackTime) / decayTime;
      return 1.0 - (1.0 - sustainLevel) * decayPosition;
    }
    if (position < 1.0 - releaseTime) {
      return sustainLevel;
    }
    double releasePosition = (position - (1.0 - releaseTime)) / releaseTime;
    return sustainLevel * (1.0 - releasePosition);
  }

  private static Clip createClip(byte[] data) {
    try {
      AudioFormat format = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE, CHANNELS, SIGNED, BIG_ENDIAN);
      Clip clip = AudioSystem.getClip();
      clip.open(format, data, 0, data.length);
      return clip;
    } catch (LineUnavailableException e) {
      log.warn("Failed to create audio clip", e);
      return null;
    }
  }

  /**
   * Enables or disables sound effects.
   *
   * @param enabled whether playback should be audible
   */
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  /** Plays the food collection sound. */
  public void playFoodSound() {
    play(foodSound);
  }

  /** Plays the generation complete sound. */
  public void playGenerationSound() {
    play(generationSound);
  }

  /**
   * Sets the volume level.
   *
   * @param volume the requested level, clamped into {@code [0, 1]}
   */
  public void setVolume(float volume) {
    this.volume = Math.clamp(volume, 0.0f, 1.0f);
    applyGain(foodSound);
    applyGain(generationSound);
  }

  /**
   * Returns the current volume level.
   *
   * @return the volume in {@code [0, 1]}
   */
  public float getVolume() {
    return volume;
  }

  private void play(Clip clip) {
    if (!enabled || clip == null) {
      return;
    }
    try {
      clip.setFramePosition(0);
      applyGain(clip);
      clip.start();
    } catch (RuntimeException e) {
      log.warn("Failed to play sound effect", e);
    }
  }

  /** Applies the current volume to the clip's master gain when the line supports it. */
  private void applyGain(Clip clip) {
    if (clip == null || !clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
      return;
    }
    var gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
    // A zero volume is silence; log10(0) is -infinity, so clamp before converting to decibels.
    float decibels =
        volume <= 0.0f ? gainControl.getMinimum() : (float) (Math.log10(volume) * 20.0);
    gainControl.setValue(Math.clamp(decibels, gainControl.getMinimum(), gainControl.getMaximum()));
  }
}
