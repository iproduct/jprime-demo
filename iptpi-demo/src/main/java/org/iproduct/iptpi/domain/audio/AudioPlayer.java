package org.iproduct.iptpi.domain.audio;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.pi4j.wiringpi.Gpio;
import com.sun.media.codec.audio.mp3.JavaDecoder;

public class AudioPlayer {

	public AudioPlayer() {
		// initialize wiringPi library
		// Gpio.wiringPiSetupGpio();

		// GPIO output pin
		Gpio.pinMode(17, Gpio.OUTPUT);
		Gpio.pullUpDnControl(17, Gpio.PUD_DOWN);
	}

	public void enable() {
		Gpio.digitalWrite(17, true);
	}

	public void disable() {
		Gpio.digitalWrite(17, false);
	}

	public void play() {

		String strFilename = "/home/pi/az_robot.wav";
		File soundFile = new File(strFilename);

		new Thread(() -> {	
			System.out.println(soundFile);
			enable(); // enable speakers power
			try (AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile)) {
				// AudioFormat audioFormat = audioInputStream.getFormat();
				// DataLine.Info info = new DataLine.Info(SourceDataLine.class,
				// audioFormat, BUFFER_SIZE);
				try (Clip clip = AudioSystem.getClip()) {
					clip.open(audioInputStream);
					clip.setFramePosition(0);
					System.out.println(Arrays.toString(clip.getControls()));
					clip.start();
					clip.drain();

					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					clip.stop();
					clip.flush();
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					clip.close();
					
					

				} catch (LineUnavailableException e) {
					e.printStackTrace();
					// System.exit(1);
				}

			} catch (UnsupportedAudioFileException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			disable(); // disable speakers power
			System.out.println("Audio playback finished.");

		}).start();

	}

}