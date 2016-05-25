package org.iproduct.iptpi.demo.controller;

import org.iproduct.iptpi.domain.Command;
import org.iproduct.iptpi.domain.arduino.ArduinoCommand;
import org.iproduct.iptpi.domain.arduino.ArduinoCommandSubscriber;
import org.iproduct.iptpi.domain.movement.MovementData;
import org.iproduct.iptpi.domain.movement.ForwardMovement;
import org.iproduct.iptpi.domain.movement.MovementCommandSubscriber;
import org.iproduct.iptpi.domain.movement.RelativeMovement;
import org.reactivestreams.Subscriber;

import reactor.core.publisher.Mono;
import reactor.core.publisher.TopicProcessor;
import reactor.rx.Promise;

import static org.iproduct.iptpi.domain.CommandName.*;
import static org.iproduct.iptpi.domain.arduino.ArduinoCommand.NOT_FOLLOW_LINE;

public class RobotController {
	private MovementCommandSubscriber commandSub;
	private ArduinoCommandSubscriber arduinoSub;
	private TopicProcessor<Command> commands = TopicProcessor.create();
	private TopicProcessor<ArduinoCommand> arduinoCommands = TopicProcessor.create();
	private Subscriber<Integer> onExitSubscriber;
		
	public RobotController(Subscriber<Integer> onExitSubscriber, MovementCommandSubscriber commandSub,
			ArduinoCommandSubscriber arduinoSub) {
		this.onExitSubscriber = onExitSubscriber;
		this.commandSub = commandSub;
		commands.subscribe(commandSub);
		this.arduinoSub = arduinoSub;
		arduinoCommands.subscribe(arduinoSub);
	}

	public void stop() {
		commands.onNext(new Command(STOP, null));
		arduinoCommands.onNext(ArduinoCommand.NOT_FOLLOW_LINE);
	}

	public void moveUp() {
		commands.onNext(new Command(MOVE_FORWARD, new ForwardMovement(400, 50)));
	}

	public void moveDown() {
		commands.onNext(new Command(MOVE_FORWARD, new RelativeMovement(-200, 0, 0,  -50)));
	}

	public void moveLeft() {
		commands.onNext(new Command(MOVE_RELATIVE, new RelativeMovement(200, 0, 1/300f,  40)));
	}

	public void moveRight() {
		commands.onNext(new Command(MOVE_RELATIVE, new RelativeMovement(400, 0, -1/300f,  40)));
	}
	
	public void followLine() {
		commands.onNext(new Command(FOLLOW_LINE, new ForwardMovement(400, 50)));
		arduinoCommands.onNext(ArduinoCommand.FOLLOW_LINE);
	}

	public void exit() {
		Mono.just(0).subscribe(onExitSubscriber);
		
	}

}
