/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.simulation.ui.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;

import lombok.extern.slf4j.Slf4j;

/** Manages sound effects for the simulation. */
@Slf4j
public class SoundManager {
  private static final float SAMPLE_RATE = 44100f;
  private static final int SAMPLE_SIZE = 16;
  private static final int CHANNELS = 1;
  private static final boolean SIGNED = true;
  private static final boolean BIG_ENDIAN = true;

  private final Clip foodSound;
  private final Clip generationSound;
  private boolean enabled = true;
  private float volume = 0.5f; // Default volume 50%

  public SoundManager() {
    // Create synthesized sounds
    foodSound = createFoodSound();
    generationSound = createGenerationSound();
  }

  private Clip createFoodSound() {
    // Create a short "pop" sound (10ms)
    int duration = (int) (0.1 * SAMPLE_RATE);
    byte[] data = new byte[duration * 2]; // 16-bit = 2 bytes per sample

    // Generate a sine wave that starts at 880Hz and drops to 440Hz
    double frequency = 880.0;
    double freqStep = (440.0 - 880.0) / duration;

    for (int i = 0; i < duration; i++) {
      frequency += freqStep;
      double angle = 2.0 * Math.PI * frequency * i / SAMPLE_RATE;
      // Amplitude decreases over time (envelope)
      double amplitude = Math.max(0.0, 1.0 - (double) i / duration) * 32767.0;
      short sample = (short) (Math.sin(angle) * amplitude);
      data[i * 2] = (byte) (sample >> 8);
      data[i * 2 + 1] = (byte) (sample & 0xFF);
    }

    return createClip(data);
  }

  private Clip createGenerationSound() {
    // Create a longer "achievement" sound (500ms)
    int duration = (int) (0.5 * SAMPLE_RATE);
    byte[] data = new byte[duration * 2];

    // Generate a chord of frequencies (C major: 523.25Hz, 659.25Hz, 783.99Hz)
    double[] frequencies = {523.25, 659.25, 783.99};

    for (int i = 0; i < duration; i++) {
      double time = i / SAMPLE_RATE;
      double sample = 0;

      // Mix the frequencies
      for (double freq : frequencies) {
        // Add some vibrato
        double vibrato = 1.0 + 0.01 * Math.sin(2 * Math.PI * 5 * time);
        double angle = 2.0 * Math.PI * freq * vibrato * time;
        // Envelope: attack-decay-sustain-release (ADSR)
        double envelope = getADSREnvelope(i, duration);
        sample += Math.sin(angle) * envelope;
      }

      // Normalize and convert to 16-bit
      sample = (sample / frequencies.length) * 32767;
      short value = (short) Math.max(Math.min(sample, 32767), -32767);
      data[i * 2] = (byte) (value >> 8);
      data[i * 2 + 1] = (byte) (value & 0xFF);
    }

    return createClip(data);
  }

  private double getADSREnvelope(int sample, int duration) {
    double attackTime = 0.1; // 10% of duration
    double decayTime = 0.2; // 20% of duration
    double sustainLevel = 0.7; // 70% of peak
    double releaseTime = 0.3; // 30% of duration

    double position = (double) sample / duration;

    if (position < attackTime) {
      // Attack phase - linear increase
      return position / attackTime;
    } else if (position < attackTime + decayTime) {
      // Decay phase - exponential decrease to sustain level
      double decayPosition = (position - attackTime) / decayTime;
      return 1.0 - (1.0 - sustainLevel) * decayPosition;
    } else if (position < 1.0 - releaseTime) {
      // Sustain phase
      return sustainLevel;
    } else {
      // Release phase - exponential decrease to zero
      double releasePosition = (position - (1.0 - releaseTime)) / releaseTime;
      return sustainLevel * (1.0 - releasePosition);
    }
  }

  private Clip createClip(byte[] data) {
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

  /** Enables or disables sound effects. */
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  /** Plays the food collection sound. */
  public void playFoodSound() {
    playSound(foodSound);
  }

  /** Plays the generation complete sound. */
  public void playGenerationSound() {
    playSound(generationSound);
  }

  /** Sets the volume level (0.0 to 1.0). */
  public void setVolume(float volume) {
    this.volume = Math.max(0.0f, Math.min(1.0f, volume));
    if (foodSound != null
        && foodSound.isControlSupported(javax.sound.sampled.FloatControl.Type.MASTER_GAIN)) {
      javax.sound.sampled.FloatControl gainControl =
          (javax.sound.sampled.FloatControl)
              foodSound.getControl(javax.sound.sampled.FloatControl.Type.MASTER_GAIN);
      float dB = (float) (Math.log10(volume) * 20.0f);
      gainControl.setValue(
          Math.max(gainControl.getMinimum(), Math.min(gainControl.getMaximum(), dB)));
    }
    if (generationSound != null
        && generationSound.isControlSupported(javax.sound.sampled.FloatControl.Type.MASTER_GAIN)) {
      javax.sound.sampled.FloatControl gainControl =
          (javax.sound.sampled.FloatControl)
              generationSound.getControl(javax.sound.sampled.FloatControl.Type.MASTER_GAIN);
      float dB = (float) (Math.log10(volume) * 20.0f);
      gainControl.setValue(
          Math.max(gainControl.getMinimum(), Math.min(gainControl.getMaximum(), dB)));
    }
  }

  /** Gets the current volume level (0.0 to 1.0). */
  public float getVolume() {
    return volume;
  }

  private void playSound(Clip clip) {
    if (enabled && clip != null) {
      try {
        clip.setFramePosition(0);
        // Update volume before playing
        if (clip.isControlSupported(javax.sound.sampled.FloatControl.Type.MASTER_GAIN)) {
          javax.sound.sampled.FloatControl gainControl =
              (javax.sound.sampled.FloatControl)
                  clip.getControl(javax.sound.sampled.FloatControl.Type.MASTER_GAIN);
          float dB = (float) (Math.log10(volume) * 20.0f);
          gainControl.setValue(
              Math.max(gainControl.getMinimum(), Math.min(gainControl.getMaximum(), dB)));
        }
        clip.start();
      } catch (Exception e) {
        log.warn("Failed to play sound effect", e);
      }
    }
  }
}
