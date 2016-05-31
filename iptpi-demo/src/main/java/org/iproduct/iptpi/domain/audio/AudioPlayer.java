package org.iproduct.iptpi.domain.audio;
import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import com.pi4j.wiringpi.Gpio;



public class AudioPlayer
{
	private static final int	BUFFER_SIZE = 128000;

	public AudioPlayer() {
		// initialize wiringPi library
		Gpio.wiringPiSetupGpio();

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

	public void play()
	{
		
		String	strFilename = "/home/pi/jPrime.wav";
		File	soundFile = new File(strFilename);
		System.out.println(soundFile);
	
		AudioInputStream	audioInputStream = null;
		try
		{
			audioInputStream = AudioSystem.getAudioInputStream(soundFile);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		AudioFormat	audioFormat = audioInputStream.getFormat();

		SourceDataLine	line = null;
		DataLine.Info	info = new DataLine.Info(SourceDataLine.class,
												 audioFormat);
		try
		{
			line = (SourceDataLine) AudioSystem.getLine(info);
			line.open(audioFormat);
		}
		catch (LineUnavailableException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}

		line.start();

		int	bytesRead = 0;
		byte[]	abData = new byte[BUFFER_SIZE];
		while (bytesRead != -1)
		{
			try
			{
				bytesRead = audioInputStream.read(abData, 0, abData.length);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			if (bytesRead >= 0)
			{
				int	bytesWritten = line.write(abData, 0, bytesRead);
			}
		}

		line.drain();
		line.close();
		try {
			audioInputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}