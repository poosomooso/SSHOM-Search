package evaluation.io;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collector;

import evaluation.analysis.HOM;
import evaluation.analysis.Mutation;
import evaluation.analysis.Result;

/**
 * Writes the content of a {@link Collector}.
 * 
 * @author Jens Meinicke
 *
 */
public class ResultsWriter {

	public static final ResultsWriter instance = new ResultsWriter();

	private ResultsWriter() {
		// private constructor
	}

	/**
	 * Writes the content of the collector to the given file.
	 * 
	 * @param results
	 * @param file      The file
	 * @param collector The collector
	 */
	public void write(List<Result> results, String folderName) throws IOException {
		File folder = new File(folderName);
		if (!folder.exists()) {
			folder.mkdirs();
		}
		try (OutputStream out = new BufferedOutputStream(new FileOutputStream(new File(folder, "stats.tex")))) {
			writeStats(results, out);
		}

		try (OutputStream out = new BufferedOutputStream(new FileOutputStream(new File(folder, "methods.csv")))) {
			writeDistributionRelative(results, Distribution.METHOD, out);
		}
		try (OutputStream out = new BufferedOutputStream(new FileOutputStream(new File(folder, "classes.csv")))) {
			writeDistributionRelative(results, Distribution.CLASS, out);
		}
		try (OutputStream out = new BufferedOutputStream(new FileOutputStream(new File(folder, "order.csv")))) {
			writeDistributionRelative(results, Distribution.ORDER, out);
		}

		System.out.println("write operator usage");
		try (OutputStream out = new BufferedOutputStream(new FileOutputStream(new File(folder, "homs.csv")))) {
			try (PrintWriter writer = new PrintWriter(out)) {
				for (Result result : results) {
					for (HOM hom : result.homs) {
						StringBuilder sb = new StringBuilder();
						Set<String> containedChanges = new HashSet<>();
						for (Mutation m : hom.foms) {
							String change = m.change.toString();
							int i = 1;
							while (containedChanges.contains(change)) {
								change = m.change.toString() + i++;
							}
							containedChanges.add(change);
							sb.append(change);
							sb.append(',');
						}
						sb.deleteCharAt(sb.length() - 1);
						writer.println(sb);
					}
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	enum Distribution {
		METHOD, CLASS, ORDER
	}

	private void writeDistributionRelative(List<Result> results, Distribution distribuiton, OutputStream out) {
		try (PrintWriter writer = new PrintWriter(out)) {
			writer.print("x");
			for (Result result : results) {
				writer.print(',');
				writer.print(result.name);
			}
			writer.println();
			
			for (int i = 1; i < 10; i++) {
				writer.print(i);
				for (Result result : results) {
					writer.print(',');

					int[] values;
					switch (distribuiton) {
					case CLASS:
						values = result.getClassDistribution();
						break;
					case METHOD:
						values = result.getMethodDistribution();
						break;
					case ORDER:
						values = result.getOrderDistribution();
						break;
					default:
						throw new RuntimeException();
					}
					
					if (i >= values.length) {
						continue;
					}
					if (result.homs.size() == 0) {
						writer.print(0);
					} else {
						int count = (values[i] * 100) / result.homs.size();
						if (count > 0) {
							writer.print(count);
						}
					}
				}
				writer.println();
			}
		}
	}

	/**
	 * Writes the content of the collector to the given {@link OutputStream}.
	 * 
	 * @param out       The output stream
	 * @param collector The collector
	 */
	public void writeStats(List<Result> results, OutputStream out) {
		results.sort((r1, r2) -> r2.getStatistics().loc - r1.getStatistics().loc);
		try (PrintWriter writer = new PrintWriter(out)) {
			for (Result result : results) {
				writer.print(result.name);
				writer.print(" & ");
				writer.print(result.getStatistics().loc);
				writer.print(" & ");
				writer.print(result.getStatistics().nrTests);
				writer.print(" & ");
				writer.print(result.getStatistics().nrTestsVarex);
				writer.print(" (");
				int percantage = result.getStatistics().nrTestsVarex * 100 / result.getStatistics().nrTests;
				writer.print(percantage);
				writer.print("\\%)");
				for (int s : result.stats) {
					writer.print(" & ");
					writer.print(s);
				}
				writer.print(" (");
				int percantageSubsumedFOMs = result.stats[Result.FOM_SUB_INDEX] * 100 / result.stats[Result.FOM_INDEX];
				writer.print(percantageSubsumedFOMs);
				writer.print("\\%)");
				writer.print("\\\\");
				writer.println();
			}
		}
	}

}
