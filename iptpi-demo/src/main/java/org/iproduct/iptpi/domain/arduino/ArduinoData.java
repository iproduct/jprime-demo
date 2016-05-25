package org.iproduct.iptpi.domain.arduino;
// START SNIPPET: serial-snippet

import java.io.IOException;
import java.nio.ByteBuffer;

import org.reactivestreams.Subscriber;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataEventListener;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.SerialPortException;

import reactor.core.subscriber.SignalEmitter;
import reactor.rx.Broadcaster;
import reactor.rx.Fluxion;

/**
 * This class represents an event stream (Fluxion in Reactor terms) providing fluent API 
 * for event transformations and allowing all interested consumenrs to listen for 
 * EncoderReadings received from Arduino Leonardo USB connected microcontroller.
 *  
 * @author Trayan Iliev
 */
public class ArduinoData {
	
//	public final long START_ARDUINO_SERVICE_TIME;
	public static final long ARDUNO_SERIAL_REPORT_PERIOD = 100; //ms
	protected static final byte IN_ENCODERS_POSITION = 1;
	protected static final byte IN_LINE_READINGS = 2;
	private final Serial serial;
	private final Broadcaster<EncoderReadings> positionsFluxion;
	private final SignalEmitter<EncoderReadings> positionsEmitter;
	private final Broadcaster<LineReadings> lineFluxion;
	private final SignalEmitter<LineReadings> lineEmitter;
	private long numberReadings = 0;

	public ArduinoData(Serial serial) {
		this.serial = serial;
		
		positionsFluxion = Broadcaster.create();
		positionsEmitter = positionsFluxion.startEmitter();
                
		lineFluxion = Broadcaster.create();
		lineEmitter = lineFluxion.startEmitter();
                
        System.out.println("Arduino serial communication started.");
        System.out.println(" Connection settings: 38400, N, 8, 1.");
        
        // create and register the serial data listener
        serial.addListener(new SerialDataEventListener() {
        	private ByteBuffer buffer = ByteBuffer.allocate(1024);
			
            @Override
            public void dataReceived(SerialDataEvent event) {
				try {
					ByteBuffer newBuffer = event.getByteBuffer();
					buffer.put(newBuffer);
					buffer.flip();
					System.out.println("Readings length: " + buffer.remaining());
					boolean hasMoreData = true;
					while(buffer.hasRemaining() && hasMoreData) {
						switch(buffer.get(buffer.position())) {
						case IN_ENCODERS_POSITION:
							if(buffer.remaining() >= 13) {
								buffer.get();
								long timestamp = buffer.getInt(); //get timestamp
								int encoderL = -buffer.getInt(); //two motors are mirrored
								int encoderR = buffer.getInt();
								EncoderReadings readings = new EncoderReadings(encoderR, encoderL, timestamp);
//								EncoderReadings readings = new EncoderReadings(encoderR, encoderL,
//										START_ARDUINO_SERVICE_TIME + (numberReadings++) * ARDUNO_SERIAL_REPORT_PERIOD);
//								System.out.println(readings);
								if(positionsEmitter.hasRequested())
									positionsEmitter.submit(readings); 
							} else {
								hasMoreData = false;
							}
							break;	
					    case IN_LINE_READINGS:
							if(buffer.remaining() >= 11) {
								buffer.get();
								long timestamp = buffer.getInt(); //get timestamp
								int lineL = buffer.getShort(); //read left line sensor 
								int lineM = buffer.getShort(); //read left line sensor 
								int lineR = buffer.getShort(); //read left line sensor 
								System.out.println("@@@@@@@@@@@@@ " + timestamp + " : " 
										+ lineL + ", " + lineM + ", " + lineR
										);
								LineReadings lineReadings = new LineReadings(lineL, lineM, lineR, timestamp);
								if(lineEmitter.hasRequested())
									lineEmitter.submit(lineReadings); 
							} else {
								hasMoreData = false;
							}
							break;
						}	
					}
					buffer.compact();
				} catch (Exception e) {
					e.printStackTrace();
				}      
            }
        });
                
//		START_ARDUINO_SERVICE_TIME  = System.currentTimeMillis();
	}
	
	public Fluxion<EncoderReadings> getPositionsFluxion() {
		return positionsFluxion;	
	}
	
	public Fluxion<LineReadings> getLineFluxion() {
		return lineFluxion;
	}
}

