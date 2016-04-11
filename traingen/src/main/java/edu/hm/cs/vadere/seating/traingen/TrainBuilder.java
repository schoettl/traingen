package edu.hm.cs.vadere.seating.traingen;

import java.awt.geom.Rectangle2D;
import attributes.scenario.AttributesObstacle;
import attributes.scenario.AttributesTarget;
import geometry.shapes.VRectangle;
import geometry.shapes.VShape;
import scenario.Obstacle;
import scenario.Target;
import scenario.Topography;
import topographycreator.model.TopographyBuilder;

/**
 * 
 * @author Jakob Schöttl
 *
 */
public class TrainBuilder {

	private static final int DEFAULT_ID = 0; // TODO ersetzen durch id counter

	private TopographyBuilder topographyBuilder;
	private int numberOfEntranceAreas = 0;

	public TrainBuilder(TopographyBuilder topographyBuilder) {
		this.topographyBuilder = topographyBuilder;
	}
	
	public void createTrain(int numberOfEntranceAreas) {
		if (numberOfEntranceAreas <= 0) {
			throw new IllegalArgumentException("number of entrance areas must be positive.");
		}
		this.numberOfEntranceAreas = numberOfEntranceAreas;
		
//		Topography t = new Topography(attributes);
//		new TopographyBuilder(t);

		for (int i = 0; i < numberOfEntranceAreas; i++) {
			buildEntranceArea(i);
		}
		for (int i = 0; i <= numberOfEntranceAreas; i++) {
			buildCompartments(i);
		}
	}

	public void blockExits() {
		for (int i = 0; i < numberOfEntranceAreas; i++) {
			buildExitBlockades(i);
		}
	}

	public void blockEnds() {
		buildEndBlockades();
	}

	public void addStop(double time, boolean rightNotLeft, int numberOfNewPassengers) {
		int numberOfNewPassengersPerDoor = numberOfNewPassengers / numberOfEntranceAreas; // TODO so einfach is es nicht. numberOfNewPassengers soll schon eingehalten werden!
		// TODO add source with this properties...
		
	}

	public Topography getResult() {
		return topographyBuilder.build();
	}
	
	private void buildEndBlockades() {
		double x, y1, y2;

		Rectangle2D leftHalfCompartment = Train.getHalfCompartmentRect(0, 1);
		x = leftHalfCompartment.getX();
		y1 = leftHalfCompartment.getY();
		y2 = leftHalfCompartment.getY() + leftHalfCompartment.getHeight();
		buildWall(x, y1, x, y2);
		
		Rectangle2D rightHalfCompartment = Train.getHalfCompartmentRect(numberOfEntranceAreas, 0);
		x = rightHalfCompartment.getX() + rightHalfCompartment.getWidth();
		y1 = rightHalfCompartment.getY();
		y2 = rightHalfCompartment.getY() + rightHalfCompartment.getHeight();
		buildWall(x, y1, x, y2);
	}

	private void buildExitBlockades(int index) {
		Rectangle2D entranceArea = Train.getEntranceAreaRect(index);
		final double halfCompartmentWidth = Train.AISLE_LENGTH / 2;
		double x1 = entranceArea.getX() - halfCompartmentWidth;
		double x2 = entranceArea.getX() + entranceArea.getWidth() + halfCompartmentWidth;
		double y1, y2;

		// upper exit blockade
		y1 = entranceArea.getY();
		y2 = entranceArea.getY() - 1;
		buildVerticalWall(x1, y1, y2);
		buildVerticalWall(x2, y1, y2);
		buildHorizontalWall(y2, x1, x2);

		// upper exit blockade
		y1 = entranceArea.getY() + entranceArea.getHeight();
		y2 = entranceArea.getY() + entranceArea.getHeight() + 1;
		buildVerticalWall(x1, y1, y2);
		buildVerticalWall(x2, y1, y2);
		buildHorizontalWall(y2, x1, x2);
		
	}
	
	private void buildVerticalWall(double x, double y1, double y2) {
		buildWall(x, y1, x, y2);
	}

	private void buildHorizontalWall(double y, double x1, double x2) {
		buildWall(x1, y, x2, y);
	}

	private void buildWall(double x1, double y1, double x2, double y2) {
		final double wallThickness = 0.02;
		double x, y, w, h;
		if (x1 == x2) {
			x = x1;
			w = wallThickness;
			y = Math.min(y1, y2);
			h = Math.abs(y2 - y1);
		} else if (y1 == y2) {
			y = y1;
			h = wallThickness;
			x = Math.min(x1, x2);
			w = Math.abs(x2 - x1);
		} else {
			throw new IllegalArgumentException("skew walls are not possible yet.");
		}
		VRectangle rect = new VRectangle(x, y, w, h);
		topographyBuilder.addObstacle(new Obstacle(new AttributesObstacle(DEFAULT_ID, rect)));
	}

	private void buildCompartments(int index) {
		buildHalfCompartment(index, 0);
		buildHalfCompartment(index, 1);
		
		Rectangle2D rect = Train.getCompartmentRect(index);

		// backrests between seat groups
		double x = rect.getX() + rect.getWidth() / 2;
		double y1 = rect.getY();
		double y2 = rect.getY() + rect.getHeight();
		buildWall(x, y1, x, y1 + Train.BENCH_WIDTH);
		buildWall(x, y2, x, y2 - Train.BENCH_WIDTH);

		// train walls
		double x1 = rect.getX();
		double x2 = rect.getX() + rect.getWidth();
		y1 = rect.getY();
		y2 = rect.getY() + rect.getHeight();
		buildWall(x1, y1, x2, y1);
		buildWall(x1, y2, x2, y2);
	}

	private void buildHalfCompartment(int index, int subindex) {
		if (index == 0 && subindex == 0 || index == numberOfEntranceAreas && subindex == 1) {
			return;
		}
		Rectangle2D rect = Train.getHalfCompartmentRect(index, subindex);
		buildFourSeats(rect, rect.getY());
		buildFourSeats(rect, rect.getY() + Train.BENCH_WIDTH + Train.AISLE_WIDTH);
	}

	private void buildFourSeats(Rectangle2D halfCompartmentRect, double y) {
		final double distanceCenterToBackrest = (halfCompartmentRect.getWidth() - Train.DISTANCE_BETWEEN_FACING_BENCHES) / 4;
		final double x1 = halfCompartmentRect.getX() + distanceCenterToBackrest;
		final double x2 = halfCompartmentRect.getX() + halfCompartmentRect.getWidth() - distanceCenterToBackrest;
		
		double distanceFromSide;
		distanceFromSide = Train.BENCH_WIDTH / 4;
		buildSeat(x1, y + distanceFromSide);
		buildSeat(x2, y + distanceFromSide);
		distanceFromSide *= 3;
		buildSeat(x1, y + distanceFromSide);
		buildSeat(x2, y + distanceFromSide);
		
		// sides of benches
		// TODO
	}

	private void buildSeat(double x, double y) {
		final double targetRadius = 0.02;
		VShape rect = new VRectangle(x - targetRadius/2, y - targetRadius/2, targetRadius, targetRadius);
		AttributesTarget attributes = new AttributesTarget(rect, DEFAULT_ID, false);
		topographyBuilder.addTarget(new Target(attributes));
	}

	private void buildEntranceArea(int index) {
		Rectangle2D rect = Train.getEntranceAreaRect(index);
		final double width = (rect.getHeight() - Train.AISLE_ENTRANCE_WIDTH) / 2;
		double x;
		double y1 = rect.getY();
		double y2 = rect.getY() + rect.getHeight();
		
		x = rect.getX();
		buildVerticalWall(x, y1, y1 + width);
		buildVerticalWall(x, y2, y2 - width);

		x = rect.getX() + rect.getWidth();
		buildVerticalWall(x, y1, y1 + width);
		buildVerticalWall(x, y2, y2 - width);
	}

}
