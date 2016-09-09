package org.iproduct.iptpi.domain.audio;

import java.io.BufferedInputStream;
import java.io.FileInputStream;

import com.pi4j.wiringpi.Gpio;

import javazoom.jl.player.Player;

public class MP3 {
	private String filename;
	private Player player;

	// constructor that takes the name of an MP3 file
	public MP3(String filename) {
		this.filename = filename;
	}

	public void close() {
		if (player != null)
			player.close();
	}

	// play the MP3 file to the sound card
	public void play() {
		try {
			FileInputStream fis = new FileInputStream(filename);
			BufferedInputStream bis = new BufferedInputStream(fis);
			player = new Player(bis);
		} catch (Exception e) {
			System.out.println("Problem playing file " + filename);
			System.out.println(e);
		}

		// run in new thread to play in background
		new Thread() {
			public void run() {
				try {
					player.play();
				} catch (Exception e) {
					System.out.println(e);
				}
			}
		}.start();

	}

	// test client
	public static void main(String[] args) {
		// initialize wiringPi library
		Gpio.wiringPiSetupGpio();

		Gpio.pinMode(17, Gpio.OUTPUT);
		Gpio.pullUpDnControl(17, Gpio.PUD_DOWN);

		Gpio.digitalWrite(17, true);

		String filename = "/home/pi/Music/05. Moon Temple.mp3";
		// "C:\\Personal\\MP3\\Karunesh - Enlightenment, A Sacred Collection
		// 2008\\05. Moon Temple.mp3";
		MP3 mp3 = new MP3(filename);
		mp3.play();

		System.out.println("Playing: " + filename);
		try {
			Thread.sleep(20000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// when the computation is done, stop playing it
		mp3.close();
		Gpio.digitalWrite(17, false);
		System.out.println("Finished playing: " + filename);

		// // play from the beginning
		// mp3 = new MP3(filename);
		// mp3.play();

	}

}
