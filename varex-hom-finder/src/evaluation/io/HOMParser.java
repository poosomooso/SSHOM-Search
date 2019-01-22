package evaluation.io;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import evaluation.analysis.HOM;
import evaluation.analysis.Mutation;

public class HOMParser {

	public static final HOMParser instance = new HOMParser();
	
	public Collection<HOM> getHOMS(Map<String, Mutation> mutations, File file) {
		Collection<HOM> homs = new ArrayList<>();
		HOMParser.instance.readWords(file, mutations, homs);
		return homs;
	}
	
	private void readWords(File file, Map<String, Mutation> mutations, Collection<HOM> homs) {
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = reader.readLine()) != null) {
				addMutation(line, mutations, homs);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void addMutation(String line, Map<String, Mutation> mutations, Collection<HOM> homs) {
		if (line.contains("[")) {
			line = line.substring(line.indexOf('[') + 1, line.length() - 1);
		}
		String[] split = line.split(",");
		
		HOM hom = new HOM();
		homs.add(hom);
		
		for (String m : split) {
			Mutation fom = mutations.get(m.trim());
			if (fom == null) {
				throw new RuntimeException("fom " + m + " does not exist");
			}
			hom.addFOM(fom);
		}
	}
}
