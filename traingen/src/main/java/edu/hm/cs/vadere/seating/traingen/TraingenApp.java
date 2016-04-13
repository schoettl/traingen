package edu.hm.cs.vadere.seating.traingen;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.docopt.Docopt;

import scenario.Topography;
import topographycreator.utils.JSONWriter;

/**
 * See file referenced by DOC_FILENAME for documentation.
 * 
 * @author Jakob Schöttl
 *
 */
public class TraingenApp {

	// https://github.com/docopt/docopt.java
	public static final String DOC_FILENAME = "res/doc.txt";

	public static void main(String[] args) {
		try (InputStream doc = new FileInputStream(DOC_FILENAME)) {
			Map<String, Object> opts = new Docopt(doc).parse(args);
			//System.err.println(opts);
			if (isFlagOptionPresent(opts, "--help")) {
				Files.lines(Paths.get(DOC_FILENAME)).forEach(System.out::println);
				return;
			}
			
			TrainBuilder trainBuilder = new TrainBuilder();
			trainBuilder.createTrain(getOptionIntArgument(opts, "--number-entrance-areas"));
			if (isFlagOptionPresent(opts, "--block-exits")) {
				trainBuilder.blockExits();
			}
			if (isFlagOptionPresent(opts, "--block-ends")) {
				trainBuilder.blockEnds();
			}
			for (Stop s : getStops(opts)) {
				trainBuilder.addStop(s.time, s.entranceSideRightNotLeft, s.numberOfNewPassengers);
			}
			Topography topography = trainBuilder.getResult();

			JSONWriter.writeTopography(topography, new File("topography-output-file.json"));
			JSONWriter.writeTopography(topography, System.out);
			
		} catch (IOException e) {
			System.err.println("file '" + DOC_FILENAME
					+ "' cannot be opened but is required (for docopt).");
			e.printStackTrace();
		}
	}

	private static int getOptionIntArgument(Map<String, Object> opts, String option) {
		try {
			return Integer.valueOf((String) opts.get(option));
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("argument for " + option + " must be an integer.");
		}
	}

	private static boolean isFlagOptionPresent(Map<String, Object> opts, String option) {
		return (boolean) opts.get(option);
	}

	@SuppressWarnings("unchecked")
	private static List<Stop> getStops(Map<String, Object> opts) {
		List<String> list = (List<String>) opts.get("--stop");
		return list.stream()
				.map(Stop::parseStopArgument)
				.collect(Collectors.toList());
	}

}
