package edu.hm.cs.vadere.seating.traingen;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Usage: DocoptOptionsWrapper opts = new DocoptOptionsWrapper(new Docopt(doc).parse(args));
 * 
 * @author Jakob Schöttl
 *
 */
public class DocoptOptionsWrapper {

	Map<String, Object> options;

	/**
	 * Create a wrapper object based on the Map<String, Object> returned by Docopt.parse().
	 * 
	 * @param options
	 */
	public DocoptOptionsWrapper(Map<String, Object> options) {
		this.options = options;
	}

	/**
	 * Return the Map this wrapper is based on.
	 */
	public Map<String, Object> getOptionMap() {
		return options;
	}

	public int getOptionArgumentInt(String option) {
		try {
			return Integer.valueOf((String) options.get(option));
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("argument for " + option + " must be an integer.");
		}
	}

	public boolean isFlagOptionPresent(String option) {
		return (boolean) options.get(option);
	}

	@SuppressWarnings("unchecked")
	public List<Stop> getStops() {
		List<String> list = (List<String>) options.get("--stop");
		return list.stream()
				.map(Stop::parseStopArgument)
				.collect(Collectors.toList());
	}
}
