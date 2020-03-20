package util;

import java.io.PrintStream;

import benchmark.Flags;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;

public class ProgressBarFactory {
	
	private ProgressBarFactory() {
		// private constructor
	}

	public static ProgressBar create(String taskName, int initialMax) {
		ProgressBarBuilder builder = new ProgressBarBuilder();
		builder.setTaskName(taskName);
		PrintStream out = System.out;
		if (!Flags.showProgressBar()) {
			builder.setPrintStream(new PrintStream(out) {
				@Override
				public void write(byte[] buf, int off, int len) {
				}
			});
		}
		builder.setInitialMax(initialMax);
		return builder.build();
	}
}
