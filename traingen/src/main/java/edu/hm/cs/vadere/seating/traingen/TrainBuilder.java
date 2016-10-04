package edu.hm.cs.vadere.seating.traingen;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.vadere.gui.topographycreator.model.AgentWrapper;
import org.vadere.gui.topographycreator.model.TopographyBuilder;
import org.vadere.state.attributes.AttributesBuilder;
import org.vadere.state.attributes.scenario.AttributesAgent;
import org.vadere.state.attributes.scenario.AttributesSource;
import org.vadere.state.attributes.scenario.AttributesTarget;
import org.vadere.state.attributes.scenario.AttributesTopography;
import org.vadere.state.scenario.Pedestrian;
import org.vadere.state.scenario.Source;
import org.vadere.state.scenario.Target;
import org.vadere.state.scenario.Topography;
import org.vadere.state.scenario.TrainGeometry;
import org.vadere.util.geometry.shapes.VRectangle;
import org.vadere.util.geometry.shapes.VShape;

import edu.hm.cs.vadere.seating.traingen.ObstacleBuilder.WallAlignment;
import edu.hm.cs.vadere.seating.traingen.Stop.EntranceSide;

/**
 * Builder for train topographies. Words like "left", "right", "top", and "bottom" refer to the map
 * view of the scenario, not the actual train (since there is no direction of travel defined in the
 * topography).
 * 
 * @author Jakob Schöttl
 *
 */
public class TrainBuilder {

	private static final double MEAN_INTER_ENTER_TIME = 1.4; // from data: survey/rate/analysis.R
	private TopographyBuilder topographyBuilder;
	private ObstacleBuilder obstacleBuilder;
	private int numberOfEntranceAreas = 0;
	/** Extra list of seats; for internal use only. */
	private List<Target> seats;
	private double doorToSourceDistance;

	private TrainGeometry trainGeometry;
	private Random random;

	// Michael: "Die ID muss nur innerhalb der jeweiligen Elementgruppe eindeutig sein."
	private int targetIdCounter = 1;
	private int sourceIdCounter = 1;

	public TrainBuilder(TrainGeometry trainGeometry, Random random) {
		this.trainGeometry = trainGeometry;
		this.random = random;
	}

	/** Must be called before all other methods. */
	public void createTrain(int numberOfEntranceAreas) {
		if (numberOfEntranceAreas <= 0) {
			throw new IllegalArgumentException("number of entrance areas must be positive.");
		}
		this.numberOfEntranceAreas = numberOfEntranceAreas;
		this.topographyBuilder = createTopographyBuilder();
		this.obstacleBuilder = new ObstacleBuilder(topographyBuilder);
		// each entrance area makes up 1 compartment, each compartment has 16 seats
		this.seats = new ArrayList<>(16 * numberOfEntranceAreas);

		for (int i = 0; i < numberOfEntranceAreas; i++) {
			buildEntranceArea(i);
		}
		for (int i = 0; i <= numberOfEntranceAreas; i++) {
			buildCompartments(i);
		}
	}

	public void setDoorToSourceDistance(double doorToSourceDistance) {
		this.doorToSourceDistance  = doorToSourceDistance;
	}

	public void blockExits() {
		for (int i = 0; i < numberOfEntranceAreas; i++) {
			buildExitBlockades(i);
		}
	}

	public void blockEnds() {
		buildEndBlockades();
	}

	public void addStop(Stop stop) {
		int[] numbersPerDoor = spreadPassengers(stop.numberOfNewPassengers);
		for (int i = 0; i < numberOfEntranceAreas; i++) {
			VShape shape = createSourceShape(i, stop.entranceSide);
			AttributesBuilder<AttributesSource> attributesBuilder =
					new AttributesBuilder<>(new AttributesSource(sourceIdCounter++, shape));
			attributesBuilder.setField("startTime", stop.time);
			attributesBuilder.setField("endTime", stop.time + 1e10);
			attributesBuilder.setField("distributionParameters", Collections.singletonList(MEAN_INTER_ENTER_TIME));
			// omit this line to use default constant distribution:
			attributesBuilder.setField("interSpawnTimeDistribution", ExponentialDistribution.class.getName());
			attributesBuilder.setField("maxSpawnNumberTotal", numbersPerDoor[i]);
			attributesBuilder.setField("spawnAtRandomPositions", true);
			
			final Source source = new Source(attributesBuilder.build());
			topographyBuilder.addSource(source);
		}
	}

	public void addCompartmentTargets() {
		addLeftHalfCompartmentTarget();
		addRightHalfCompartmentTarget();
		// all other compartments inbetween:
		for (int i = 1; i < numberOfEntranceAreas; i++) {
			addCompartmentTarget(i);
		}
	}

	public void placePersons(int numberOfPersons) {
		final int numberOfSeats = seats.size();
		if (numberOfPersons > numberOfSeats) {
			throw new IllegalArgumentException(String.format(
					"number of sitting persons (%d) cannot be greater than number of seats (%d).",
					numberOfPersons, numberOfSeats));
		}
		List<Integer> list = createRangeList(0, numberOfSeats - 1);
		Collections.shuffle(list, random);
		for (Integer seatIndex : list.subList(0, numberOfPersons)) {
			Target target = seats.get(seatIndex);
			Pedestrian person = new Pedestrian(new AttributesAgent(), random);
			person.setNextTargetListIndex(0);
			person.setPosition(target.getShape().getCentroid());
			topographyBuilder.addPedestrian(new AgentWrapper(person));
		}
	}

	public Topography getResult() {
		return topographyBuilder.build();
	}

	private List<Integer> createRangeList(int min, int max) {
		List<Integer> result = new ArrayList<>(max - min + 1);
		for (int i = min; i <= max; i++) {
			result.add(i);
		}
		return result;
	}

	private void addCompartmentTarget(int compartmentIndex) {
		final Rectangle2D rect = trainGeometry.getCompartmentRect(compartmentIndex);
		buildAisleTarget(rect);
	}

	private void addLeftHalfCompartmentTarget() {
		final Rectangle2D rect = trainGeometry.getHalfCompartmentRect(0, 1);
		buildAisleTarget(rect);
	}

	private void addRightHalfCompartmentTarget() {
		final Rectangle2D rect = trainGeometry.getHalfCompartmentRect(numberOfEntranceAreas, 0);
		buildAisleTarget(rect);
	}

	private TopographyBuilder createTopographyBuilder() {
		AttributesBuilder<AttributesTopography> attributesBuilder =
				new AttributesBuilder<>(new AttributesTopography());

		double width = numberOfEntranceAreas
				* (trainGeometry.getAisleLength() + trainGeometry.getEntranceAreaWidth())
				+ trainGeometry.getAisleLength() + 4;
		double height = trainGeometry.getTrainInteriorWidth() + 4;
		attributesBuilder.setField("bounds", new VRectangle(0, 0, width, height));

		final Topography topography = new Topography(attributesBuilder.build(), new AttributesAgent());
		return new TopographyBuilder(topography);
	}

	private VShape createSourceShape(int entranceAreaIndex, EntranceSide entranceSide) {
		Rectangle2D entranceArea = trainGeometry.getEntranceAreaRect(entranceAreaIndex);
		double x = entranceArea.getX() + (trainGeometry.getEntranceAreaWidth() - trainGeometry.getDoorWidth()) / 2;
		double y;
		final double thickness = 0.1;
		if (entranceSide == EntranceSide.BOTTOM) {
			y = entranceArea.getY() - thickness - doorToSourceDistance;
		} else if (entranceSide == EntranceSide.TOP) {
			y = entranceArea.getY() + entranceArea.getHeight() + doorToSourceDistance;
		} else {
			throw new NullPointerException("entranceSide parameter must not be null");
		}
		// evtl. weniger breit als getDoorWidth(), weil Personen ja auch einen Durchmesser haben
		return new VRectangle(x, y, trainGeometry.getDoorWidth(), thickness);
	}
	
	private int[] spreadPassengers(int numberOfNewPassengers) {
		// Hier könnte man auch eine Verteilung einbauen
		// (Verteilung der Personen am Bahnsteig)
		int[] result = new int[numberOfEntranceAreas];
		for (int i = 0; i < numberOfNewPassengers; i++) {
			result[random.nextInt(result.length)]++;
		}
		return result;
	}
	
	private void buildEndBlockades() {
		double x, y1, y2;

		Rectangle2D leftHalfCompartment = trainGeometry.getHalfCompartmentRect(0, 1);
		x = leftHalfCompartment.getX();
		y1 = leftHalfCompartment.getY();
		y2 = leftHalfCompartment.getY() + leftHalfCompartment.getHeight();
		obstacleBuilder.buildVerticalWall(x, WallAlignment.BELOW, y1, y2);
		
		Rectangle2D rightHalfCompartment =
				trainGeometry.getHalfCompartmentRect(numberOfEntranceAreas, 0);
		x = rightHalfCompartment.getX() + rightHalfCompartment.getWidth();
		y1 = rightHalfCompartment.getY();
		y2 = rightHalfCompartment.getY() + rightHalfCompartment.getHeight();
		obstacleBuilder.buildVerticalWall(x, WallAlignment.ON_TOP, y1, y2);
	}

	private void buildExitBlockades(int index) {
		Rectangle2D entranceArea = trainGeometry.getEntranceAreaRect(index);
		final double halfCompartmentWidth = trainGeometry.getAisleLength() / 2;
		double x1 = entranceArea.getX() - halfCompartmentWidth;
		double x2 = entranceArea.getX() + entranceArea.getWidth() + halfCompartmentWidth;
		double y1, y2;

		// upper exit blockade
		y1 = entranceArea.getY();
		y2 = entranceArea.getY() - 1;
		obstacleBuilder.buildVerticalWall(x1, WallAlignment.CENTER, y1, y2);
		obstacleBuilder.buildVerticalWall(x2, WallAlignment.CENTER, y1, y2);
		obstacleBuilder.buildHorizontalWall(y2, WallAlignment.CENTER, x1, x2);

		// lower exit blockade
		y1 = entranceArea.getY() + entranceArea.getHeight();
		y2 = entranceArea.getY() + entranceArea.getHeight() + 1;
		obstacleBuilder.buildVerticalWall(x1, WallAlignment.CENTER, y1, y2);
		obstacleBuilder.buildVerticalWall(x2, WallAlignment.CENTER, y1, y2);
		obstacleBuilder.buildHorizontalWall(y2, WallAlignment.CENTER, x1, x2);
		
	}
	

	private void buildCompartments(int index) {
		buildHalfCompartment(index, 0);
		buildHalfCompartment(index, 1);
		
		Rectangle2D rect = trainGeometry.getCompartmentRect(index);

		// backrests between seat groups
		double x = rect.getX() + rect.getWidth() / 2;
		double y1 = rect.getY();
		double y2 = rect.getY() + rect.getHeight();
		obstacleBuilder.buildVerticalWall(x, WallAlignment.CENTER, y1, y1 + trainGeometry.getBenchWidth());
		obstacleBuilder.buildVerticalWall(x, WallAlignment.CENTER, y2, y2 - trainGeometry.getBenchWidth());
	}

	private void buildHalfCompartment(int index, int subindex) {
		if (index == 0 && subindex == 0 || index == numberOfEntranceAreas && subindex == 1) {
			return;
		}
		Rectangle2D rect = trainGeometry.getHalfCompartmentRect(index, subindex);
		buildFourSeats(rect, rect.getY());
		buildFourSeats(rect, rect.getY() + trainGeometry.getBenchWidth() + trainGeometry.getAisleWidth());

		// train outer walls
		double x1 = rect.getX();
		double x2 = rect.getX() + rect.getWidth();
		double y1 = rect.getY();
		double y2 = rect.getY() + rect.getHeight();
		obstacleBuilder.buildHorizontalWall(y1, WallAlignment.BELOW, x1, x2);
		obstacleBuilder.buildHorizontalWall(y2, WallAlignment.ON_TOP, x1, x2);

	}

	private void buildFourSeats(Rectangle2D halfCompartmentRect, double y) {
		final double distanceCenterToBackrest = (halfCompartmentRect.getWidth() - trainGeometry.getDistanceBetweenFacingBenches()) / 4;
		double x1 = halfCompartmentRect.getX() + distanceCenterToBackrest;
		double x2 = halfCompartmentRect.getX() + halfCompartmentRect.getWidth() - distanceCenterToBackrest;
		
		double distanceFromSide;
		distanceFromSide = trainGeometry.getBenchWidth() / 4;
		buildSeat(x1, y + distanceFromSide);
		buildSeat(x2, y + distanceFromSide);
		distanceFromSide *= 3;
		buildSeat(x1, y + distanceFromSide);
		buildSeat(x2, y + distanceFromSide);
		
		// sides of benches
		double y1 = y;
		double y2 = y + trainGeometry.getBenchWidth();
		x1 = halfCompartmentRect.getX();
		x2 = x1 + trainGeometry.getSeatDepth();
		obstacleBuilder.buildHorizontalWall(y1, WallAlignment.CENTER, x1, x2);
		obstacleBuilder.buildHorizontalWall(y2, WallAlignment.CENTER, x1, x2);
		x1 = halfCompartmentRect.getX() + halfCompartmentRect.getWidth();
		x2 = x1 - trainGeometry.getSeatDepth();
		obstacleBuilder.buildHorizontalWall(y1, WallAlignment.CENTER, x1, x2);
		obstacleBuilder.buildHorizontalWall(y2, WallAlignment.CENTER, x1, x2);
	}

	private void buildSeat(double x, double y) {
		Target target = createTarget(x, y);
		topographyBuilder.addTarget(target);
		seats.add(target);
	}

	private void buildAisleTarget(Rectangle2D compartmentRect) {
		final double x = compartmentRect.getMinX();
		final double y = compartmentRect.getCenterY() - trainGeometry.getAisleWidth() / 2;
		final double w = compartmentRect.getWidth();
		final double h = trainGeometry.getAisleWidth();
		topographyBuilder.addTarget(createTarget(x, y, w, h));
	}
	
	/** Create point target (size as small as possible). */
	private Target createTarget(double x, double y) {
		final double targetDiameter = 0.1; // depends on grid resolution, see ObstacleBuilder.WALL_THICKNESS
		x = x - targetDiameter / 2;
		y = y - targetDiameter / 2;
		return createTarget(x, y, targetDiameter, targetDiameter);
	}

	private Target createTarget(double x, double y, double w, double h) {
		VShape rect = new VRectangle(x, y, w, h);
		AttributesTarget attributes = new AttributesTarget(rect, targetIdCounter++, false);
		return new Target(attributes);
	}

	private void buildEntranceArea(int index) {
		final Rectangle2D rect = trainGeometry.getEntranceAreaRect(index);
		final double width = (rect.getHeight() - trainGeometry.getAisleEntranceWidth()) / 2;
		final double x1 = rect.getX();
		final double x2 = rect.getX() + rect.getWidth();
		final double y1 = rect.getY();
		final double y2 = rect.getY() + rect.getHeight();
		
		obstacleBuilder.buildVerticalWall(x1, WallAlignment.CENTER, y1, y1 + width);
		obstacleBuilder.buildVerticalWall(x1, WallAlignment.CENTER, y2, y2 - width);
		obstacleBuilder.buildVerticalWall(x2, WallAlignment.CENTER, y1, y1 + width);
		obstacleBuilder.buildVerticalWall(x2, WallAlignment.CENTER, y2, y2 - width);
		
		final double distanceToDoor =
				(trainGeometry.getEntranceAreaWidth() - trainGeometry.getDoorWidth()) / 2;
		obstacleBuilder.buildHorizontalWall(y1, WallAlignment.BELOW, x1, x1 + distanceToDoor);
		obstacleBuilder.buildHorizontalWall(y2, WallAlignment.ON_TOP,  x1, x1 + distanceToDoor);
		obstacleBuilder.buildHorizontalWall(y1, WallAlignment.BELOW, x2, x2 - distanceToDoor);
		obstacleBuilder.buildHorizontalWall(y2, WallAlignment.ON_TOP,  x2, x2 - distanceToDoor);
	}

}
