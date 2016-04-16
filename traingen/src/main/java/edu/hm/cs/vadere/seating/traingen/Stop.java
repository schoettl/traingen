package edu.hm.cs.vadere.seating.traingen;

import java.util.Arrays;
import java.util.NoSuchElementException;

public class Stop {
	
	/**
	 * Side of entrance. TOP/BOTTOM refer to the scenario map (top view). It's not left/right
	 * because the scenario cannot define the train's direction.
	 */
	public static enum EntranceSide {
		TOP, BOTTOM;
		public static EntranceSide parse(String s) {
			return Arrays.stream(values())
					.filter(o -> o.toString().startsWith(s))
					.findFirst().get();
		}
		@Override
		public String toString() {
			return super.toString().toLowerCase();
		}
	}
	
	public double time;
	public EntranceSide entranceSide;
	public int numberOfNewPassengers;

	public Stop(double time, EntranceSide entranceSide, int numberOfNewPassengers) {
		this.time = time;
		this.entranceSide = entranceSide;
		this.numberOfNewPassengers = numberOfNewPassengers;
	}

	public static Stop parseStopArgument(String stopArgument) {
		try {
			String[] args = stopArgument.split(",");

			double time = Double.valueOf(args[0]);
			EntranceSide entranceSide;
			int numberOfNewPassengers = Integer.valueOf(args[2]);
			try {
				entranceSide = EntranceSide.parse(args[1]);
			} catch (NoSuchElementException e) {
				throw new IllegalArgumentException(String.format(
						"<side> argument of --stop option must be either '%s' or '%s'.",
						EntranceSide.TOP, EntranceSide.BOTTOM));
			}

			return new Stop(time, entranceSide, numberOfNewPassengers);

		} catch (Exception e) {
			throw new IllegalArgumentException("argument of --stop option is malformed.", e);
		}
	}

}
