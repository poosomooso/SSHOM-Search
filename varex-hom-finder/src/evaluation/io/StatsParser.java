package evaluation.io;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import evaluation.analysis.Statistics;

public class StatsParser {

	public static final StatsParser instance = new StatsParser();
	
	public Statistics readStats(File file) {
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line = reader.readLine();
			String[] split = line.split(",");
			int loc = Integer.parseInt(split[0]);
			int nrTests = Integer.parseInt(split[1]);
			int nrTestsVarex = Integer.parseInt(split[2]);
			return new Statistics(loc, nrTests, nrTestsVarex);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
