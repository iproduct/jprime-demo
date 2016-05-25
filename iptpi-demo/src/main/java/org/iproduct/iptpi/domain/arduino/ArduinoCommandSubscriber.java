package org.iproduct.iptpi.domain.arduino;

import static java.lang.Math.abs;
import static java.lang.Math.asin;
import static java.lang.Math.hypot;
import static java.lang.Math.signum;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static org.iproduct.iptpi.demo.robot.RobotParametrs.MAX_ROBOT_LINEAR_VELOCITY;
import static org.iproduct.iptpi.domain.arduino.ArduinoCommand.*;

import java.io.IOException;

import org.iproduct.iptpi.domain.Command;
import org.iproduct.iptpi.domain.movement.AbsoluteMovement;
import org.iproduct.iptpi.domain.movement.MotorsCommand;
import org.iproduct.iptpi.domain.movement.RelativeMovement;
import org.iproduct.iptpi.domain.position.Position;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialPortException;
import com.pi4j.wiringpi.Gpio;

import reactor.core.subscriber.ConsumerSubscriber;
import reactor.core.subscriber.Subscribers;
import reactor.rx.Fluxion;

public class ArduinoCommandSubscriber extends ConsumerSubscriber<ArduinoCommand>  {
	
	private Serial serial;
	
	public ArduinoCommandSubscriber(Serial serial) {
		this.serial = serial;
	}

	@Override
	public void doNext(ArduinoCommand command) {
		switch (command) {
		case FOLLOW_LINE :  // enable line following sensors readings
		case NOT_FOLLOW_LINE :  // disable line following sensors readings
			try {
				serial.write(command.getCode());
			} catch (IllegalStateException | IOException ex) {
		        System.out.println(" ==>> ERROR SENDING SERIAL DATA TO ARDUINO : " + ex.getMessage());
			}
			break;
		}
	}

}
