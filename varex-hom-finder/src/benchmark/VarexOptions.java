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
	
	public static boolean splitExpr1() {
		return SATsplitExpr1;
	}
	
	public static boolean splitExpr2() {
		return SATsplitExpr2;
	}
	
	public static boolean splitExpr3() {
		return SATsplitExpr3;
	}
}
