package util;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MutantClassExtractor {
	
	public static void main(String[] args) throws IOException {
		File mutantFile = new File("C:\\Users\\Jens Meinicke\\git\\mutationtest-varex\\varex-hom-finder\\resources\\evaluationfiles\\ant\\mapping.txt");
		if (!mutantFile.exists()) {
			throw new RuntimeException();
		}
		// todo read
		List<String> classes = readClassesFromMutantFile(mutantFile);
		File output = new File("ant-classes.txt");
		if (!output.exists()) {
			boolean success = output.createNewFile();
			if (!success) {
				throw new RuntimeException(output.toString());
			}
		}
		try (PrintWriter out = new PrintWriter(new BufferedOutputStream(new FileOutputStream(output)))) {
			for (String klass : classes) {
				out.println(klass);
			}
		}
		
	}

	private static List<String> readClassesFromMutantFile(File mutantFile) {
		try (BufferedReader in = new BufferedReader(new FileReader(mutantFile))) {
			// ignopre first line
			in.readLine();
			Set<String> klasses = new HashSet<>();
			String line;
			while ((line = in.readLine()) != null) {
				String[] split = line.split(",");
				klasses.add(split[1]);
			}
			List<String> sortedClasses = new ArrayList<>(klasses);
			Collections.sort(sortedClasses);
			return sortedClasses;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
