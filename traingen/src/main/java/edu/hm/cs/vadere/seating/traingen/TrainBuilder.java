package edu.hm.cs.vadere.seating.traingen;

import java.awt.geom.Rectangle2D;
import java.util.Random;

import attributes.AttributesBuilder;
import attributes.scenario.AttributesObstacle;
import attributes.scenario.AttributesSource;
import attributes.scenario.AttributesTarget;
import attributes.scenario.AttributesTopography;
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

	public void createTrain(int numberOfEntranceAreas) {
		if (numberOfEntranceAreas <= 0) {
			throw new IllegalArgumentException("number of entrance areas must be positive.");
		}
		this.numberOfEntranceAreas = numberOfEntranceAreas;
		this.topographyBuilder = createTopographyBuilder();

		for (int i = 0; i < numberOfEntranceAreas; i++) {
			buildEntranceArea(i);
		}
		for (int i = 0; i <= numberOfEntranceAreas; i++) {
			buildCompartments(i);
		}
	}

	private TopographyBuilder createTopographyBuilder() {
		AttributesBuilder<AttributesTopography> attributesBuilder =
				new AttributesBuilder<>(new AttributesTopography());

		double width = numberOfEntranceAreas
				* (TrainGeometry.AISLE_LENGTH + TrainGeometry.ENTRANCE_AREA_WIDTH)
				+ TrainGeometry.AISLE_LENGTH + 4;
		double height = TrainGeometry.getTrainInteriorWidth() + 4;
		attributesBuilder.setField("bounds", new VRectangle(0, 0, width, height));

		final Topography topography = new Topography(attributesBuilder.build());
		return new TopographyBuilder(topography);
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
		int[] numbersPerDoor = spreadPassengers(numberOfNewPassengers);
		for (int i = 0; i < numberOfEntranceAreas; i++) {
			VShape shape = createSourceShape(i, rightNotLeft);
			AttributesBuilder<AttributesSource> attributesBuilder =
					new AttributesBuilder<>(new AttributesSource(DEFAULT_ID, shape));
			attributesBuilder.setField("startTime", time);
			attributesBuilder.setField("spawnDelay", 1); // TODO aus daten ermitteln
			attributesBuilder.setField("spawnNumber", numbersPerDoor[i]);
			attributesBuilder.setField("spawnAtRandomPositions", true);
			
			final Source source = new Source(attributesBuilder.build());
			topographyBuilder.addSource(source);
		}
	}

	public Topography getResult() {
		return topographyBuilder.build();
	}

	private VShape createSourceShape(int entranceAreaIndex, boolean rightNotLeft) {
		Rectangle2D rect = TrainGeometry.getEntranceAreaRect(entranceAreaIndex);
		double x = rect.getX() + (TrainGeometry.ENTRANCE_AREA_WIDTH - TrainGeometry.DOOR_WIDTH) / 2;
		double y;
		final double d = 0.1;
		if (rightNotLeft) {
			y = rect.getY() - d;
		} else {
			y = rect.getY() + rect.getHeight();
		}
		// evtl. weniger breit als DOOR_WIDTH, weil Personen ja auch einen Durchmesser haben
		return new VRectangle(x, y, TrainGeometry.DOOR_WIDTH, d);
	}
	
	private int[] spreadPassengers(int numberOfNewPassengers) {
		// Hier könnte man auch eine Verteilung einbauen
		// (Verteilung der Personen am Bahnsteig)
		Random random = new Random();
		int[] result = new int[numberOfEntranceAreas];
		for (int i = 0; i < numberOfNewPassengers; i++) {
			result[random.nextInt(result.length)]++;
		}
		return result;
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
