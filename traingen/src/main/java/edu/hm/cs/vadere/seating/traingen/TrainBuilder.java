package edu.hm.cs.vadere.seating.traingen;

import java.awt.geom.Rectangle2D;
import java.util.Random;

import attributes.scenario.AttributesObstacle;
import attributes.scenario.AttributesSource;
import attributes.scenario.AttributesTarget;
import geometry.shapes.VRectangle;
import geometry.shapes.VShape;
import scenario.Obstacle;
import scenario.Source;
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

	public TrainBuilder() {
	}
	
	public TrainBuilder createTrain(int numberOfEntranceAreas) {
		if (numberOfEntranceAreas <= 0) {
			throw new IllegalArgumentException("number of entrance areas must be positive.");
		}
		this.numberOfEntranceAreas = numberOfEntranceAreas;
		
//		attributes mit passender bounding box
//		Topography t = new Topography(attributes);
//		topographyBuilder = new TopographyBuilder(topography);
		topographyBuilder = new TopographyBuilder();

		for (int i = 0; i < numberOfEntranceAreas; i++) {
			buildEntranceArea(i);
		}
		for (int i = 0; i <= numberOfEntranceAreas; i++) {
			buildCompartments(i);
		}
		return this;
	}

	public TrainBuilder blockExits() {
		for (int i = 0; i < numberOfEntranceAreas; i++) {
			buildExitBlockades(i);
		}
		return this;
	}

	public TrainBuilder blockEnds() {
		buildEndBlockades();
		return this;
	}

	public TrainBuilder addStop(double time, boolean rightNotLeft, int numberOfNewPassengers) {
		int[] numbersPerDoor = spreadPassengers(numberOfNewPassengers);
		for (int i = 0; i < numberOfEntranceAreas; i++) {
			VShape shape = createSourceShape(rightNotLeft);
			AttributesSource attributes = new AttributesSource(DEFAULT_ID, shape); // TODO missing: start time, spawn number numbersPerDoor[i]
			Source source = new Source(attributes);
			topographyBuilder.addSource(source);
		}
		return this;
	}

	private VShape createSourceShape(boolean rightNotLeft) {
		return new VRectangle(0, 0, 1, 1);
	}

	private int[] spreadPassengers(int numberOfNewPassengers) {
		Random random = new Random();
		int[] result = new int[numberOfEntranceAreas];
		for (int i = 0; i < numberOfNewPassengers; i++) {
			result[random.nextInt(result.length)]++;
		}
		return result;
	}

	public Topography getResult() {
		return topographyBuilder.build();
	}
	
	private void buildEndBlockades() {
		double x, y1, y2;

		Rectangle2D leftHalfCompartment = TrainGeometry.getHalfCompartmentRect(0, 1);
		x = leftHalfCompartment.getX();
		y1 = leftHalfCompartment.getY();
		y2 = leftHalfCompartment.getY() + leftHalfCompartment.getHeight();
		buildWall(x, y1, x, y2);
		
		Rectangle2D rightHalfCompartment = TrainGeometry.getHalfCompartmentRect(numberOfEntranceAreas, 0);
		x = rightHalfCompartment.getX() + rightHalfCompartment.getWidth();
		y1 = rightHalfCompartment.getY();
		y2 = rightHalfCompartment.getY() + rightHalfCompartment.getHeight();
		buildWall(x, y1, x, y2);
	}

	private void buildExitBlockades(int index) {
		Rectangle2D entranceArea = TrainGeometry.getEntranceAreaRect(index);
		final double halfCompartmentWidth = TrainGeometry.AISLE_LENGTH / 2;
		double x1 = entranceArea.getX() - halfCompartmentWidth;
		double x2 = entranceArea.getX() + entranceArea.getWidth() + halfCompartmentWidth;
		double y1, y2;

		// upper exit blockade
		y1 = entranceArea.getY();
		y2 = entranceArea.getY() - 1;
		buildVerticalWall(x1, y1, y2);
		buildVerticalWall(x2, y1, y2);
		buildHorizontalWall(y2, x1, x2);

		// lower exit blockade
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
		
		Rectangle2D rect = TrainGeometry.getCompartmentRect(index);

		// backrests between seat groups
		double x = rect.getX() + rect.getWidth() / 2;
		double y1 = rect.getY();
		double y2 = rect.getY() + rect.getHeight();
		buildWall(x, y1, x, y1 + TrainGeometry.BENCH_WIDTH);
		buildWall(x, y2, x, y2 - TrainGeometry.BENCH_WIDTH);

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
		Rectangle2D rect = TrainGeometry.getHalfCompartmentRect(index, subindex);
		buildFourSeats(rect, rect.getY());
		buildFourSeats(rect, rect.getY() + TrainGeometry.BENCH_WIDTH + TrainGeometry.AISLE_WIDTH);
	}

	private void buildFourSeats(Rectangle2D halfCompartmentRect, double y) {
		final double distanceCenterToBackrest = (halfCompartmentRect.getWidth() - TrainGeometry.DISTANCE_BETWEEN_FACING_BENCHES) / 4;
		double x1 = halfCompartmentRect.getX() + distanceCenterToBackrest;
		double x2 = halfCompartmentRect.getX() + halfCompartmentRect.getWidth() - distanceCenterToBackrest;
		
		double distanceFromSide;
		distanceFromSide = TrainGeometry.BENCH_WIDTH / 4;
		buildSeat(x1, y + distanceFromSide);
		buildSeat(x2, y + distanceFromSide);
		distanceFromSide *= 3;
		buildSeat(x1, y + distanceFromSide);
		buildSeat(x2, y + distanceFromSide);
		
		// sides of benches
		// TODO
		double y1 = y;
		double y2 = y + TrainGeometry.BENCH_WIDTH;
		x1 = halfCompartmentRect.getX();
		x2 = x1 + TrainGeometry.getSeatDepth();
		buildHorizontalWall(y1, x1, x2);
		buildHorizontalWall(y2, x1, x2);
		x1 = halfCompartmentRect.getX() + halfCompartmentRect.getWidth();
		x2 = x1 - TrainGeometry.getSeatDepth();
		buildHorizontalWall(y1, x1, x2);
		buildHorizontalWall(y2, x1, x2);
	}

	private void buildSeat(double x, double y) {
		final double targetRadius = 0.02;
		VShape rect = new VRectangle(x - targetRadius/2, y - targetRadius/2, targetRadius, targetRadius);
		AttributesTarget attributes = new AttributesTarget(rect, DEFAULT_ID, false);
		topographyBuilder.addTarget(new Target(attributes));
	}

	private void buildEntranceArea(int index) {
		Rectangle2D rect = TrainGeometry.getEntranceAreaRect(index);
		final double width = (rect.getHeight() - TrainGeometry.AISLE_ENTRANCE_WIDTH) / 2;
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
