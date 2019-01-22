package evaluation.io;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import evaluation.analysis.Mutation;

public class TestParser {

	public static final TestParser instance = new TestParser();
	
	public void getTestResults(Map<String, Mutation> mutations, File file) {
		readWords(file, mutations);
	}
	
	private void readWords(File file, Map<String, Mutation> mutations) {
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = reader.readLine()) != null) {
				addTestResults(line, mutations);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void addTestResults(String line, Map<String, Mutation> mutations) {
		String[] split = line.split(",");
		
		if (split.length == 0) {
			throw new ParserException(line);
		}
		String mutationName = split[0];
		Mutation m = mutations.get(mutationName);
		if (m == null) {
			throw new ParserException("Mutation " + mutationName + " noit found");
		}
		
		for (int i = 1; i < split.length; i++) {
			String testName = split[i].intern();
			m.addFailedTest(testName);
		}
	}
}
