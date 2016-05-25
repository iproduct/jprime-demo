package org.iproduct.iptpi.domain.movement;
import org.iproduct.iptpi.domain.arduino.LineReadings;
import org.iproduct.iptpi.domain.position.PositionFluxion;

import reactor.rx.Fluxion;

public class MovementFactory {
	public static MovementCommandSubscriber createCommandMovementSubscriber(
			PositionFluxion positions,
			Fluxion<LineReadings> lineReadings
			) {
		return new MovementCommandSubscriber(positions, lineReadings);
	}
} 