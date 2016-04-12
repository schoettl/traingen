package edu.hm.cs.vadere.seating.traingen;

public class Stop {
	public double time;
	public boolean entranceSideRightNotLeft;
	public int numberOfNewPassengers;

	public Stop(double time, boolean entranceSideRightNotLeft, int numberOfNewPassengers) {
		this.time = time;
		this.entranceSideRightNotLeft = entranceSideRightNotLeft;
		this.numberOfNewPassengers = numberOfNewPassengers;
	}

	public static Stop parseStopArgument(String stopArgument) {
		try {
			String[] args = stopArgument.split(",");

			double time = Double.valueOf(args[0]);
			boolean entranceSideRightNotLeft;
			if ("right".startsWith(args[1])) {
				entranceSideRightNotLeft = true;
			} else if ("left".startsWith(args[1])) {
				entranceSideRightNotLeft = false;
			} else {
				throw new IllegalArgumentException("<side> argument of --stop option must be either 'right' or 'left'.");
			}
			int numberOfNewPassengers = Integer.valueOf(args[2]);

			return new Stop(time, entranceSideRightNotLeft, numberOfNewPassengers);

		} catch (Exception e) {
			throw new IllegalArgumentException("argument of --stop option is malformed.", e);
		}
	}

}
