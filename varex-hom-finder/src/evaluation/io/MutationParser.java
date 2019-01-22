package evaluation.io;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import evaluation.analysis.Mutation;

public class MutationParser {

	public static final MutationParser instance = new MutationParser();
	
	public Map<String, Mutation> getMutations(File file) {
		Map<String, Mutation> mutations = new HashMap<>();
		readWords(file, mutations);
		return mutations;
	}
	
	private void readWords(File file, Map<String, Mutation> mutations) {
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = reader.readLine()) != null) {
				addMutation(line, mutations);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void addMutation(String line, Map<String, Mutation> mutations) {
		String[] split = line.split(",");
		
		if (split.length != 5) {
			throw new ParserException(line);
		}
		
		Mutation m = new Mutation(split[0].trim(), split[1].trim(), split[2].trim(), split[3].trim(), split[4].trim());
		mutations.put(m.name, m);
	}
}
