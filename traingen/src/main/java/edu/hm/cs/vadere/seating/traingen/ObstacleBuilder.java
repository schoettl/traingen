package edu.hm.cs.vadere.seating.traingen;

import org.vadere.gui.topographycreator.model.TopographyBuilder;
import org.vadere.state.attributes.scenario.AttributesObstacle;
import org.vadere.state.scenario.Obstacle;
import org.vadere.util.geometry.shapes.VRectangle;

public class ObstacleBuilder {
	
	/**
	 * Alignment for walls. A horizontal wall (a) can be centered around the y
	 * value, (b) can be on top of the y value (y to y+thickness), or (c) can be
	 * below the y value (y-thickness to y). Same for vertical walls.
	 */
	public static enum WallAlignment {
		BELOW, ON_TOP, CENTER;
	}
	
	private static final int ID_UNDEFINED = -1;
	
	public static final double WALL_THICKNESS = 0.1; // depends on the Model attribute FLOORFIELD -> potentialFieldResolution

	private TopographyBuilder topographyBuilder;

	public ObstacleBuilder(TopographyBuilder topographyBuilder) {
		this.topographyBuilder = topographyBuilder;
	}
	
	public void buildVerticalWall(double x, WallAlignment alignment, double y1, double y2) {
		x = correctPosition(x, alignment);
		buildWall(x, y1, x, y2);
	}

	public void buildHorizontalWall(double y, WallAlignment alignment, double x1, double x2) {
		y = correctPosition(y, alignment);
		buildWall(x1, y, x2, y);
	}

	public void buildWall(double x1, double y1, double x2, double y2) {
		double x, y, w, h;
		if (x1 == x2) {
			x = x1;
			w = WALL_THICKNESS;
			y = Math.min(y1, y2);
			h = Math.abs(y2 - y1);
		} else if (y1 == y2) {
			y = y1;
			h = WALL_THICKNESS;
			x = Math.min(x1, x2);
			w = Math.abs(x2 - x1);
		} else {
			throw new IllegalArgumentException("skew walls are not possible yet.");
		}
		VRectangle rect = new VRectangle(x, y, w, h);
		topographyBuilder.addObstacle(new Obstacle(new AttributesObstacle(ID_UNDEFINED, rect)));
	}

	/**
	 * Correct the position of the wall depending on the alignment. The position is the x value for
	 * vertical walls and the y value for horizontal walls.
	 * 
	 * @param position
	 * @param alignment
	 * @return
	 */
	private double correctPosition(double position, WallAlignment alignment) {
		if (alignment == WallAlignment.CENTER) {
			position -= WALL_THICKNESS / 2;
		} else if (alignment == WallAlignment.BELOW) {
			position -= WALL_THICKNESS;
		}
		return position;
	}
}
