package benchmark;

public class VarexOptions {
	public enum SOLVER {
		BDD, SAT
	}
	
	private static SOLVER solver = SOLVER.BDD;
	
	public static SOLVER getSolver() {
		return solver;
	}
	
	private static boolean computeStrict = false;
	
	public static boolean isComputeStrict() {
		return computeStrict;
	}
	
	private static boolean SATsplitExpr1 = false;
	private static boolean SATsplitExpr2 = false;
	private static boolean SATsplitExpr3 = false;
	
	// split duration in ms
	private static long SatSplitDuration = 0;
	
	public static boolean splitExpr1() {
		return SATsplitExpr1;
	}
	
	public static boolean splitExpr2() {
		return SATsplitExpr2;
	}
	
	public static boolean splitExpr3() {
		return SATsplitExpr3;
	}
	
	// the duration at which the bdd expressions gets split
	public static long getSatSplitDuration() {
		return SatSplitDuration;
	}
}
