package edu.hm.cs.vadere.seating.traingen;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
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
			DocoptOptionsWrapper opts = new DocoptOptionsWrapper(new Docopt(doc).parse(args));
			//System.err.println(opts.getOptionMap());
			if (opts.isFlagOptionPresent("--help")) {
				Files.lines(Paths.get(DOC_FILENAME)).forEach(System.out::println);
				return;
			}
			
			TrainBuilder trainBuilder = new TrainBuilder();
			trainBuilder.createTrain(opts.getOptionArgumentInt("--number-entrance-areas"));
			if (opts.isFlagOptionPresent("--block-exits")) {
				trainBuilder.blockExits();
			}
			if (opts.isFlagOptionPresent("--block-ends")) {
				trainBuilder.blockEnds();
			}
			for (Stop s : opts.getStops()) {
				trainBuilder.addStop(s);
			}
			if (opts.isFlagOptionPresent("--interim-destinations")) {
				trainBuilder.addInterimDestinations();
			}
			trainBuilder.placePersons(opts.getOptionArgumentInt("--number-sitting-persons"));
			Topography topography = trainBuilder.getResult();

			JSONWriter.writeTopography(topography, new File("topography-output-file.json"));
//			JSONWriter.writeTopography(topography, System.out);
			
		} catch (IOException e) {
			System.err.println("file '" + DOC_FILENAME
					+ "' cannot be opened but is required (for docopt).");
			e.printStackTrace();
		}
	}
}
