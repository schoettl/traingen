package edu.hm.cs.vadere.seating.traingen;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import scenario.Et423Geometry;
import scenario.TrainGeometry;

/**
 * This is a wrapper class around the options Map returned by the <tt>Docopt.parse(...)</tt> method.
 * It should be seen as a template! Feel free to add getter methods, change method names or
 * implementation, ...
 * 
 * Usage:
 * <tt>DocoptOptionsWrapper opts = new DocoptOptionsWrapper(new Docopt(doc).parse(args));</tt>
 * 
 * @see org.docopt.Docopt Docopt
 * @see <a href="http://docopt.org/">docopt.org</a>
 * @author Jakob Schöttl
 */
public class DocoptOptionsWrapper {

	Map<String, Object> options;

	/**
	 * Create a wrapper object based on the <tt>Map<String, Object></tt> returned by
	 * <tt>Docopt.parse(...)</tt>.
	 * 
	 * @param options
	 */
	public DocoptOptionsWrapper(Map<String, Object> options) {
		this.options = options;
	}

	/**
	 * Return the original Map this wrapper is based on.
	 */
	public Map<String, Object> getOptionMap() {
		return options;
	}

	public String getOptionArgumentString(String option) {
		return (String) options.get(option);
	}

	public int getOptionArgumentInt(String option) {
		try {
			return Integer.valueOf((String) options.get(option));
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("argument for " + option + " must be an integer.");
		}
	}

	public double getOptionArgumentDouble(String option) {
		try {
			return Double.valueOf((String) options.get(option));
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("argument for " + option + " must be a floating point number.");
		}
	}

	public boolean isFlagOptionPresent(String option) {
		return (boolean) options.get(option);
	}

	public boolean isOptionPresent(String option) {
		return options.get(option) != null;
	}

	@SuppressWarnings("unchecked")
	public List<Stop> getStops() {
		List<String> list = (List<String>) options.get("--stop");
		return list.stream()
				.map(Stop::parseStopArgument)
				.collect(Collectors.toList());
	}

	public TrainGeometry getTrainGeometry() {
		final String option = "--train-geometry";
		String className = getOptionArgumentString(option);
		if (className == null) {
			return new Et423Geometry();
		}
		try {
			@SuppressWarnings("unchecked")
			Class<TrainGeometry> c = (Class<TrainGeometry>) Class.forName(className);
			return c.newInstance();
		} catch (Exception e) {
			throw new IllegalArgumentException("argument for option " + option + " must be the "
					+ "fully qualified class name of a concrete TrainGeometry class with a "
					+ "public default constructor.", e);
		}
	}
}
