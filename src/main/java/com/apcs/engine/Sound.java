package com.apcs.engine;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.CountDownLatch;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * <p>
 * This is a simple sound class that can play sounds in some formats (like wav).
 * It cannot play files in mp3 format. It will play sounds immediately and can
 * play the same sound multiple times in a row without interruption. Most sound
 * classes in comparison lag the first time you play a sound and can't handle
 * playing the same sound many times in a row quickly, so this is a great
 * solution for playing short sounds many times in a row. For looping a long
 * music track, you should use another option such as MediaPlayer.
 * </p>
 * <p>
 * Note that merely loading a single sound using this class will wake up the
 * sound system and remove the lag from the first sound played even if it is
 * played with another class such as AudioClip. If you want to compare the
 * performance of another sound class to this one, you should make sure this
 * class is not used anywhere in your code while testing the other class.
 * </p>
 * <p>
 * If you want to initialize the sound with multiple channels, you can do so in
 * the second constructor. This could make sense if you already know you will be
 * playing this sound many times in extremely quick succession, but most of the
 * time it is not needed because whenever a sound is told to play and there
 * isn't a channel available, a new channel will be created automatically.
 * Example Usage:
 * 
 * <pre>
     Sound.init();
     Sound sound = new Sound("<a href=
'https://github.com/tedMcleod/javafx.sound.demo/raw/refs/heads/main/src/resources/smw_coin.wav' >smw_coin.wav"</a>);
     for(int i=0; i<10; i++) {
         sound.play();
         Thread.sleep(100);
     }
 * </pre>
 */
public class Sound implements Closeable {
    /// dynamic circular queue of audio channels being played
    private Clip[] clips;
    /// index of clip that is played least recently
    private int clipIndex;
    /// location of sound file
    private final URL location;

    /**
     * Plays silence to load up Audio API classes. Make sure to call this method
     * before any Sound is instantiated.
     */
    public static void init() {
        try {
            // setup clip
            AudioFormat af = new AudioFormat(440f, 8, 1, true, false);
            AudioInputStream ais = new AudioInputStream(new ByteArrayInputStream(new byte[] { 0 }), af, 1);
            Clip c = AudioSystem.getClip();
            c.open(ais);

            CountDownLatch latch = new CountDownLatch(1);
            c.addLineListener(e -> {
                if (e.getType() == LineEvent.Type.STOP)
                    latch.countDown();
            });

            c.start();

            // wait until clip finishes
            latch.await();
        } catch (LineUnavailableException | IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * <p>
     * Initialize a Sound from a file at the given path
     * </p>
     * Example path: "resources/laserSound.wav"
     *
     * @param resourcePath the path to the sound file
     */
    public Sound(String resourcePath) { this(Sound.class.getClassLoader().getResource(resourcePath)); }

    public Sound(URL location) { this(location, 1); }

    /**
     * <p>
     * Initialize a Sound from a file at the given path with the given number of
     * channels preloaded.
     * </p>
     * Example path: "resources/laserSound.wav"
     *
     * @param location    the path to the sound file
     * @param numChannels the number of channels
     */
    public Sound(URL location, int numChannels) {
        this.location = location;

        if (numChannels < 0)
            numChannels = 0;
        clips = new Clip[numChannels];
        for (int i = 0; i < clips.length; i++) {
            clips[i] = getClip();
        }
        clipIndex = 0;
    }

    private Clip getClip() {
        try {
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(location);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            return clip;
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            throw new RuntimeException("failed to create clip from " + location, e);
        }
    }

    /**
     * Play the sound.
     */
    public void play() {
        Clip clip = clips[clipIndex];
        if (clip.isRunning()) {
            // add new clip to the front of the queue
            Clip[] clipsCopy = new Clip[clips.length + 1];
            for (int i = 0; i < clips.length; i++) {
                clipsCopy[i + 1] = clips[(i + clipIndex) % clips.length];
            }

            clip = (clips = clipsCopy)[clipIndex = 0] = getClip();
        }
        clip.setFramePosition(0);
        clip.start();
        // move current clip to back of the queue
        clipIndex = (clipIndex + 1) % clips.length;
    }

    /**
     * Stop the sound.
     */
    public void stop() {
        for (Clip c : clips) {
            c.stop();
        }
    }

    /**
     * Close the clips only when finished with them because the sounds won't be able
     * to be played again if you close them.
     */
    @Override
    public void close() {
        for (Clip c : clips) {
            c.close();
        }
    }

}