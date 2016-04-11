package edu.hm.cs.vadere.seating.traingen;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.docopt.Docopt;

import scenario.Topography;
import topographycreator.model.TopographyBuilder;
import topographycreator.utils.JSONWriter;

/**
 * See file referenced by DOC_FILENAME for documentation.
 * 
 * @author Jakob Schöttl
 *
 */
public class App {

	// https://github.com/docopt/docopt.java
	public static final String DOC_FILENAME = "res/doc.txt";

	public static void main(String[] args) {
		try (InputStream doc = new FileInputStream(DOC_FILENAME)) {
			Map<String, Object> opts = new Docopt(doc).parse(args);
			System.out.println(opts);
			
			TrainBuilder trainBuilder = new TrainBuilder(new TopographyBuilder());
			trainBuilder.createTrain(2);
			trainBuilder.blockExits();
			trainBuilder.blockEnds();
			Topography topography = trainBuilder.getResult();
			JSONWriter.writeTopography(topography, new File("topography-output-file.json"));
			
			
		} catch (IOException e) {
			System.err.println("File '" + DOC_FILENAME
					+ "' cannot be opened but is required for docopt.");
			e.printStackTrace();
		}
	}

}
