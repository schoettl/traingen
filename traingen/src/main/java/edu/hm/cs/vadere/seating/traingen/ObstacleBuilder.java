package edu.hm.cs.vadere.seating.traingen;

import attributes.scenario.AttributesObstacle;
import geometry.shapes.VRectangle;
import scenario.Obstacle;
import topographycreator.model.TopographyBuilder;

public class ObstacleBuilder {
	
	private static final int ID_UNDEFINED = -1;

	private TopographyBuilder topographyBuilder;

	public ObstacleBuilder(TopographyBuilder topographyBuilder) {
		this.topographyBuilder = topographyBuilder;
	}
	
	public void buildVerticalWall(double x, double y1, double y2) {
		buildWall(x, y1, x, y2);
	}

	public void buildHorizontalWall(double y, double x1, double x2) {
		buildWall(x1, y, x2, y);
	}

	public void buildWall(double x1, double y1, double x2, double y2) {
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
		topographyBuilder.addObstacle(new Obstacle(new AttributesObstacle(ID_UNDEFINED, rect)));
	}
}
