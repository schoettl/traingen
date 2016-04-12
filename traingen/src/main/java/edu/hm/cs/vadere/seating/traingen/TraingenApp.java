package edu.hm.cs.vadere.seating.traingen;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
			System.err.println(opts);
			
			TrainBuilder trainBuilder = new TrainBuilder();
			trainBuilder.createTrain(Integer.valueOf((String) opts.get("--number-entrance-areas")));
			if ((boolean) opts.get("--block-exits")) {
				trainBuilder.blockExits();
			}
			if ((boolean) opts.get("--block-ends")) {
				trainBuilder.blockEnds();
			}
			for (Stop s : getStops(opts)) {
				trainBuilder.addStop(s.time, s.entranceSideRightNotLeft, s.numberOfNewPassengers);
			}
			Topography topography = trainBuilder.getResult();

			JSONWriter.writeTopography(topography, new File("topography-output-file.json"));
//			JSONWriter.writeTopography(topography, System.out);
			
		} catch (IOException e) {
			System.err.println("File '" + DOC_FILENAME
					+ "' cannot be opened but is required for docopt.");
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	private static List<Stop> getStops(Map<String, Object> opts) {
		List<String> list = (List<String>) opts.get("--stop");
		return list.stream()
				.map(Stop::parseStopArgument)
				.collect(Collectors.toList());
	}

}
