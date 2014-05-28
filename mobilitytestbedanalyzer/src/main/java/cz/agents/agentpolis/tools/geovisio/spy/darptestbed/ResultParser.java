package cz.agents.agentpolis.tools.geovisio.spy.darptestbed;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.io.FileUtils;

import au.com.bytecode.opencsv.CSVWriter;

import com.google.common.collect.Lists;

public class ResultParser {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		CSVWriter csvWriter = new CSVWriter(new FileWriter(new File("merge_result.csv")));
		String[] header = new String[] { "Exp. number", "Request number", "Driver number", "Uni. dist driver",
				"Uni. dist, passenger", "Average passenger travel time (on-board) is",
				"Max passenger travel time (on-board) is", "Median passenger travel time (on-board) is",
				"Average passenger ride time (on-board) is", "Max passenger ride time (on-board) is",
				"Median passenger ride time (on-board) is", "Average passenger wait time is",
				"Max passenger wait time is", "Median passenger wait time is",
				"Total vehicle distance driven (in kilometers)", "Total values of CO2 [gram]",
				"Total values of CO [gram]", "Total values of NOx [gram]", "Total values of PM10 [gram]",
				"Total values of SOx [gram]", "Total values of [Liter] fuel" };

		csvWriter.writeNext(header);

		File resultsFile = new File("results");
		List<File> files = Lists.newArrayList(resultsFile.listFiles());
		Collections.sort(files, new Comparator<File>() {

			@Override
			public int compare(File o1, File o2) {
				return Integer.valueOf(o1.getName()) - Integer.valueOf(o2.getName());
			}
		});
		for (File result : files) {

			String[] line = new String[header.length];
			line[0] = result.getName();

			File[] resultDataFiles = result.listFiles(new FilenameFilter() {

				@Override
				public boolean accept(File arg0, String arg1) {
					return arg1.indexOf("result_testbed_san_francisco") != -1;
				}
			});

			if (resultDataFiles == null || resultDataFiles.length == 0) {
				continue;
			}

			File resultDataFile = resultDataFiles[0];

			String[] simConfig = resultDataFile.getName().split("_");
			line[1] = simConfig[5].replace("r", "").trim();
			line[2] = simConfig[6].replace("d", "").trim();
			line[3] = simConfig[7].replace("DU", "").trim();
			line[4] = simConfig[8].replace("PU", "").replace(".txt", "").trim();

			List<String> factors = FileUtils.readLines(resultDataFile).subList(1, 17);
			int ind = 5;
			for (String factor : factors) {
				line[ind++] = factor.substring(factor.indexOf(":") + 1, factor.length()).trim();
			}

			csvWriter.writeNext(line);
		}

		csvWriter.close();
	}
}
