package evaluation.analysis;
import java.io.PrintWriter;
import java.util.Collection;

public class Result {

	public final String name;

	public Result(String name) {
		this.name = name;
	}

	public static final int FOM_INDEX = 0;
	private static final int SSHOM_INDEX = 1;
	private static final int SSHOM_RED_INDEX = 2;
	public static final int FOM_SUB_INDEX = 3;
	public int[] stats = null;
	private int[] methodDistribution;
	private int[] classDistribution;
	private int[] orderDistribution;
	
	public static final String STATS_HEADER = "Subject & FOMs & SSHOMs & SSHOMs red & subsumed FOMs\\\\";
	public Collection<HOM> homs;
	private Statistics statistics;
	
	public void setStats(int... stats) {
		this.stats = stats;
	}
	
	public void printStats(PrintWriter out) {
		out.print(name);
		for (int s : stats) {
			out.print(" & ");
			out.print(s);
		}
		out.print(" (");
		int percantageSubsumedFOMs = stats[FOM_SUB_INDEX] * 100 / stats[FOM_INDEX];
		out.print(percantageSubsumedFOMs);
		out.print("\\%)");
		out.print("\\\\");
	}

	public void setDistirbutions(int[] methodDistribution, int[] classDistribution) {
		this.methodDistribution = methodDistribution;
		this.classDistribution = classDistribution;
	}
	
	public int[] getMethodDistribution() {
		return methodDistribution;
	}
	
	public int[] getClassDistribution() {
		return classDistribution;
	}


	public void setOrderDistribution(int[] distribution) {
		this.orderDistribution = distribution;
	}
	
	public int[] getOrderDistribution() {
		return orderDistribution;
	}

	public void setHOMs(Collection<HOM> homs) {
		this.homs = homs;
	}

	public void setStatistics(Statistics statistics) {
		this.statistics = statistics;
	}
	
	public Statistics getStatistics() {
		return statistics;
	}

}
