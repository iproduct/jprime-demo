package org.iproduct.iptpi.domain.position;

import org.iproduct.iptpi.domain.arduino.EncoderReadings;

import reactor.rx.Fluxion;

public class PositionFactory {
	public static PositionFluxion createPositionFluxion(Fluxion<EncoderReadings> encoderReadings) {
		return new PositionFluxion(encoderReadings);
	}
	public static PositionPanel createPositionPanel(PositionFluxion fluxion) {
		PositionPanel positionPanel = new PositionPanel();
		fluxion.subscribe(positionPanel);
		return positionPanel;
	}
}
