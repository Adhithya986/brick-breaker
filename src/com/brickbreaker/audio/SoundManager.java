package com.brickbreaker.audio;

import javax.sound.sampled.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Generates procedural synthesized sound effects using pure Java Sound API.
 * Requires zero external audio files and runs asynchronously.
 */
public class SoundManager {
    private static boolean soundEnabled = true;
    private static final ExecutorService soundPool = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r, "SoundThread");
        t.setDaemon(true);
        return t;
    });

    public static boolean isSoundEnabled() {
        return soundEnabled;
    }

    public static void setSoundEnabled(boolean enabled) {
        soundEnabled = enabled;
    }

    public static void playPaddleBounce() {
        if (!soundEnabled) return;
        soundPool.submit(() -> generateToneSlide(320, 240, 0.06, 0.4));
    }

    public static void playBrickHit() {
        if (!soundEnabled) return;
        soundPool.submit(() -> generateToneSlide(540, 680, 0.05, 0.35));
    }

    public static void playBrickBreak() {
        if (!soundEnabled) return;
        soundPool.submit(() -> generateToneSlide(440, 880, 0.08, 0.45));
    }

    public static void playPowerUp() {
        if (!soundEnabled) return;
        soundPool.submit(() -> {
            generateTone(523.25, 0.06, 0.4); // C5
            generateTone(659.25, 0.06, 0.4); // E5
            generateTone(783.99, 0.10, 0.5); // G5
        });
    }

    public static void playLostLife() {
        if (!soundEnabled) return;
        soundPool.submit(() -> generateToneSlide(340, 110, 0.25, 0.5));
    }

    public static void playGameOver() {
        if (!soundEnabled) return;
        soundPool.submit(() -> {
            generateToneSlide(392, 330, 0.15, 0.5);
            generateToneSlide(330, 261, 0.20, 0.5);
            generateToneSlide(261, 164, 0.35, 0.6);
        });
    }

    public static void playLevelWin() {
        if (!soundEnabled) return;
        soundPool.submit(() -> {
            generateTone(440, 0.08, 0.4); // A4
            generateTone(554, 0.08, 0.4); // C#5
            generateTone(659, 0.08, 0.4); // E5
            generateTone(880, 0.20, 0.5); // A5
        });
    }

    private static void generateTone(double freq, double seconds, double volume) {
        try {
            float sampleRate = 22050f;
            int numSamples = (int) (seconds * sampleRate);
            byte[] buffer = new byte[numSamples];

            for (int i = 0; i < numSamples; i++) {
                double angle = 2.0 * Math.PI * i / (sampleRate / freq);
                double envelope = 1.0 - (double) i / numSamples; // linear fade out
                short val = (short) (Math.sin(angle) * 32767 * volume * envelope);
                buffer[i] = (byte) (val >> 8);
            }

            playPcmBuffer(buffer, sampleRate);
        } catch (Exception ignored) {}
    }

    private static void generateToneSlide(double startFreq, double endFreq, double seconds, double volume) {
        try {
            float sampleRate = 22050f;
            int numSamples = (int) (seconds * sampleRate);
            byte[] buffer = new byte[numSamples];

            for (int i = 0; i < numSamples; i++) {
                double progress = (double) i / numSamples;
                double currentFreq = startFreq + (endFreq - startFreq) * progress;
                double angle = 2.0 * Math.PI * i / (sampleRate / currentFreq);
                double envelope = 1.0 - progress;
                short val = (short) (Math.sin(angle) * 32767 * volume * envelope);
                buffer[i] = (byte) (val >> 8);
            }

            playPcmBuffer(buffer, sampleRate);
        } catch (Exception ignored) {}
    }

    private static void playPcmBuffer(byte[] buffer, float sampleRate) {
        try {
            AudioFormat format = new AudioFormat(sampleRate, 8, 1, true, true);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            if (!AudioSystem.isLineSupported(info)) return;

            try (SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info)) {
                line.open(format);
                line.start();
                line.write(buffer, 0, buffer.length);
                line.drain();
            }
        } catch (Exception ignored) {}
    }
}
