package org.iproduct.iptpi.demo;
import static org.iproduct.iptpi.domain.arduino.ArduinoCommand.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import org.iproduct.iptpi.demo.controller.RobotController;
import org.iproduct.iptpi.demo.net.RobotWSService;
import org.iproduct.iptpi.demo.view.RobotView;
import org.iproduct.iptpi.domain.arduino.ArduinoCommandSubscriber;
import org.iproduct.iptpi.domain.arduino.ArduinoData;
import org.iproduct.iptpi.domain.arduino.ArduinoFactory;
import org.iproduct.iptpi.domain.audio.AudioFactory;
import org.iproduct.iptpi.domain.audio.AudioPlayer;
import org.iproduct.iptpi.domain.movement.MovementCommandSubscriber;
import org.iproduct.iptpi.domain.movement.MovementFactory;
import org.iproduct.iptpi.domain.position.PositionFactory;
import org.iproduct.iptpi.domain.position.PositionFluxion;

import reactor.core.subscriber.Subscribers;
import reactor.core.util.Logger;

public class IPTPIDemo {
	private RobotController controller;
	private RobotView view;
	private ArduinoData arduinoData;
	private ArduinoCommandSubscriber arduinoCommnadsSub;
	private PositionFluxion positionsPub;
	private MovementCommandSubscriber movementSub, movementSub2;
	private List<JComponent> presentationViews = new ArrayList<>();
	private RobotWSService positionsService;
	private AudioPlayer audio;
	
	public IPTPIDemo() throws IOException {
		//receive Arduino data readings
		arduinoData = ArduinoFactory.getInstance().createArduinoData(); 
		
		//calculate robot positions
		positionsPub = PositionFactory.createPositionFluxion(arduinoData.getPositionsFluxion());
		presentationViews.add(PositionFactory.createPositionPanel(positionsPub));
		
		//enable sending commands to Arduino
		arduinoCommnadsSub = ArduinoFactory.getInstance().createArduinoCommandSubscriber();
		
		//Audio player - added @jPrime 2016 Hackergarten 
		audio = AudioFactory.createAudioPlayer();
//		audio = null;
		
		//wire robot main controller with services
		movementSub = MovementFactory.createCommandMovementSubscriber(positionsPub, arduinoData.getLineFluxion(), audio);
		controller = new RobotController(Subscribers.consumer(this::tearDown), movementSub, arduinoCommnadsSub, audio);
		
		//create view with controller and delegate material views from query services
		view = new RobotView("IPTPI Reactive Robotics Demo", controller, presentationViews);
		
		//expose as WS service
		movementSub2 = MovementFactory.createCommandMovementSubscriber(positionsPub, arduinoData.getLineFluxion(), audio);
		positionsService = new RobotWSService(positionsPub, movementSub2);	
				
	}
	
	public void tearDown(Integer exitStatus) {
		Logger log = Logger.getLogger(this.getClass().getName());
		log.info("Tearing down services and exiting the system");
		try {
			positionsService.teardown();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.exit(exitStatus);
	}
	
	public static void main(String[] args) {
		try {
			IPTPIDemo demo = new IPTPIDemo();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
