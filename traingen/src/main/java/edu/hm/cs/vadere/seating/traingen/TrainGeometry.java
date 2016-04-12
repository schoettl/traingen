package edu.hm.cs.vadere.seating.traingen;

import java.awt.geom.Rectangle2D;

/**
 * 
 * @author Jakob Schöttl
 *
 */
public class TrainGeometry {

	public static final double DOOR_WIDTH = 1.8;
	public static final double ENTRANCE_AREA_WIDTH = 2.4;

	public static final double BENCH_WIDTH = 1.15;
	public static final double DISTANCE_BETWEEN_FACING_BENCHES = 0.75;

	public static final double AISLE_WIDTH = 1.3;
	public static final double AISLE_ENTRANCE_WIDTH = 0.7;
	public static final double AISLE_LENGTH = 4.4;
	
	public static final double X_OFFSET = 6;
	public static final double Y_OFFSET = 2;

	public static Rectangle2D getEntranceAreaRect(int index) {
		double w = ENTRANCE_AREA_WIDTH;
		double h = getTrainInteriorWidth();
		double x = index * (w + AISLE_LENGTH) + X_OFFSET;
		double y = 0 + Y_OFFSET;
		return new Rectangle2D.Double(x, y, w, h);
	}

	public static Rectangle2D getCompartmentRect(int index) {
		double w = AISLE_LENGTH;
		double h = getTrainInteriorWidth();
		double x = ENTRANCE_AREA_WIDTH + (index - 1) * (ENTRANCE_AREA_WIDTH + w) + X_OFFSET;
		double y = 0 + Y_OFFSET;
		return new Rectangle2D.Double(x, y, w, h);
	}

	public static Rectangle2D getHalfCompartmentRect(int index, int subindex) {
		Rectangle2D compartment = getCompartmentRect(index);
		double w = compartment.getWidth() / 2;
		double x;
		if (subindex == 0) {
			x = compartment.getX();
		} else if (subindex == 1) {
			x = compartment.getX() + w;
		} else {
			throw new IllegalArgumentException("subindex must be 0 or 1.");
		}
		return new Rectangle2D.Double(x, compartment.getY(), w, compartment.getHeight());
	}
	
	public static double getSeatDepth() {
		return (AISLE_LENGTH - 2 * DISTANCE_BETWEEN_FACING_BENCHES) / 4;
	}

	public static double getTrainInteriorWidth() {
		return 2 * BENCH_WIDTH + AISLE_WIDTH;
	}

}
