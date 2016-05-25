package org.iproduct.iptpi.domain.position;

import org.iproduct.iptpi.domain.arduino.EncoderReadings;
import org.reactivestreams.Subscriber;

import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.bus.selector.Selector;
import reactor.rx.Broadcaster;
import reactor.rx.Fluxion;

import static java.lang.Math.*;
import static org.iproduct.iptpi.demo.robot.RobotParametrs.*;

public class PositionFluxion extends Fluxion<Position> {
	private Fluxion<EncoderReadings> encoderReadings;
	private Selector<?> selector;
	private PositionPanel panel =  new PositionPanel();
	private Position currentPosition;
	private Fluxion<Position> fluxion;

	public PositionFluxion(Fluxion<EncoderReadings> encoderReadings) {
		this.encoderReadings = encoderReadings;
		
//		currentPosition = new Position(0, 0, PI/2);
//		bus.on(selector, (Event<EncoderReadings> ev) -> {
//			EncoderReadings enc = ev.getData(),
//				delta = new EncoderReadings(enc.getEncoderA() - previous.encoderA, 
//						enc.getEncoderB() - previous.encoderB);
//			currentPosition = new Position(enc.getEncoderA(), enc.getEncoderB());
//			panel.updateReport(currentPosition);
//			bus.notify(outEventTopic, Event.wrap(currentPosition)); 
//		});
		
//		Fluxion<? extends Event<?>> events = bus.on(selector);
//		Fluxion<? extends Event<?>> skip1 = events.skip(1);
//		events.zipWith(skip1, (curr, prev) -> {
//			int currA = ((EncoderReadings)curr.getData()).getEncoderA();
//			int currB = ((EncoderReadings)curr.getData()).getEncoderB();
//			int prevA = ((EncoderReadings)prev.getData()).getEncoderA();
//			int prevB = ((EncoderReadings)prev.getData()).getEncoderB();
//			return new Position(currA - prevA, currB - prevB, 0); 
//		}).consume(pos -> {
//			currentPosition = pos;
//			panel.updateReport(pos);
//		});
		
		Fluxion<EncoderReadings> skip1 = encoderReadings.skip(1);
		fluxion = Fluxion.zip(encoderReadings, skip1)
			.map(tupple -> {
//				System.out.println(tupple); 
				return tupple;
			})
			.scan(new Position(0, 0, 0), (last, tupple) -> {
				EncoderReadings prev = tupple.getT1();
				EncoderReadings curr = tupple.getT2();
				int prevL = prev.getEncoderL();
				int prevR = prev.getEncoderR();
				int currL = curr.getEncoderL();
				int currR = curr.getEncoderR();
				int sL = currL - prevL;
				int sR = currR - prevR;
				double alpha0 = last.getHeading();
				if(sR == sL) {
					return new Position((float)(last.getX() + sL * ENCODER_STEP_LENGTH * cos(alpha0)), 
							(float)(last.getY()+ sL * ENCODER_STEP_LENGTH * sin(alpha0)), alpha0, curr.getTimestamp());
				} else {
					double x = last.getX() + (MAIN_AXE_LENGTH * (sL+sR) * 
							(sin((sR - sL) * ENCODER_STEP_LENGTH /MAIN_AXE_LENGTH  + alpha0) - sin(alpha0)))
							/ (2 * (sR - sL));
					double y = last.getY() - (MAIN_AXE_LENGTH * (sL+sR) * 
							(cos((sR - sL) * ENCODER_STEP_LENGTH /MAIN_AXE_LENGTH  + alpha0) - cos(alpha0)))
							/ (2 * (sR - sL));
					double alpha = alpha0 + (sR - sL) * ENCODER_STEP_LENGTH / MAIN_AXE_LENGTH;
					return new Position((float)x, (float)y, alpha, curr.getTimestamp());
				}
			}).map(pos -> {
//				System.out.println("    #########   " + pos); 
				return pos;
			});
	}
	
	public PositionPanel getPresentationComponent() {
		return panel;
	}

	public Position getReport() {
		return currentPosition;
	}

	@Override
	public void subscribe(Subscriber<? super Position> s) {
		fluxion.subscribe(s);	
	}

}
