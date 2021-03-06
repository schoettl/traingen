package edu.hm.cs.vadere.seating.traingen;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import org.apache.commons.io.IOUtils;
import org.docopt.Docopt;
import org.vadere.gui.topographycreator.utils.TopographyJsonWriter;
import org.vadere.state.scenario.Topography;
import org.vadere.state.scenario.TrainGeometry;
import org.vadere.state.util.StateJsonConverter;

/**
 * See resource file referenced by DOC_RESOURCE_NAME for documentation.
 * 
 * @author Jakob Schöttl
 *
 */
public class TraingenApp {

	// https://github.com/docopt/docopt.java
	private static final String DOC_RESOURCE_NAME = "/doc.txt";

	public static void main(String[] args) {
		try (InputStream doc = getHelpMessageResourceAsStream()) {
			if (doc == null) {
				throw new IOException("could not open help resource");
			}

			final String helpMessage = IOUtils.toString(doc, "UTF-8");
			final DocoptOptionsWrapper opts = new DocoptOptionsWrapper(new Docopt(helpMessage).parse(args));
			//System.err.println(opts.getOptionMap());
			if (opts.isFlagOptionPresent("--help")) {
				System.out.println(helpMessage);
				return;
			}
			
			Random random;
			if (opts.isOptionPresent("--random-seed")) {
				random = new Random(opts.getOptionArgumentInt("--random-seed"));
			} else {
				random = new Random();
			}
			TrainGeometry trainGeometry = opts.getTrainGeometry();
			TrainBuilder trainBuilder = new TrainBuilder(trainGeometry, random);
			trainBuilder.createTrain(opts.getOptionArgumentInt("--number-entrance-areas"));
			trainBuilder.setDoorToSourceDistance(opts.getOptionArgumentDouble("--door-source-distance"));
			if (opts.isFlagOptionPresent("--block-exits")) {
				trainBuilder.blockExits();
			}
			if (opts.isFlagOptionPresent("--block-ends")) {
				trainBuilder.blockEnds();
			}
			for (Stop s : opts.getStops()) {
				trainBuilder.addStop(s);
			}
			if (opts.isFlagOptionPresent("--compartment-targets")) {
				trainBuilder.addCompartmentTargets();
			}
			trainBuilder.placePersons(opts.getOptionArgumentInt("--number-sitting-persons"));
			Topography topography = trainBuilder.getResult();

			String outputFile = opts.getOptionArgumentString("--output-file");
			if (outputFile == null) {
				TopographyJsonWriter.writeTopography(topography, System.out);
			} else {
				TopographyJsonWriter.writeTopography(topography, new File(outputFile));
			}
			if (opts.isFlagOptionPresent("--clipboard")) {
				String json = StateJsonConverter.serializeTopography(topography);
				StringSelection selection = new StringSelection(json);
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				clipboard.setContents(selection, selection);
			}

		} catch (IOException e) {
			System.err.println("resource file '" + DOC_RESOURCE_NAME
					+ "' cannot be opened but is required (for docopt).");
			e.printStackTrace();
		}
	}

	private static InputStream getHelpMessageResourceAsStream() {
		return TraingenApp.class.getResourceAsStream(DOC_RESOURCE_NAME);
	}
}
