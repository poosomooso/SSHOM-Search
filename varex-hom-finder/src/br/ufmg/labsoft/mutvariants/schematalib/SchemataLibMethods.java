package br.ufmg.labsoft.mutvariants.schematalib;

public class SchemataLibMethods {

	public static ISchemataLibMethodsListener listener = new ISchemataLibMethodsListener() {
//        @Override
        public void listen() {
            // nothing
        }
    };

//AOR
//plus
	public static int AOR_plus(int left, int right, boolean... mutants) {
		listener.listen();
		return (mutants[3] ? (left % right) : (mutants[2] ? (left / right) : (mutants[1] ? (left * right) : (mutants[0] ? (left - right) : (left + right)))));
	}

	public static long AOR_plus(long left, long right, boolean... mutants) {
		listener.listen();
		return (mutants[3] ? (left % right) : (mutants[2] ? (left / right) : (mutants[1] ? (left * right) : (mutants[0] ? (left - right) : (left + right)))));
	}

	public static float AOR_plus(float left, float right, boolean... mutants) {
		listener.listen();
		return (mutants[3] ? (left % right) : (mutants[2] ? (left / right) : (mutants[1] ? (left * right) : (mutants[0] ? (left - right) : (left + right)))));
	}

	public static double AOR_plus(double left, double right, boolean... mutants) {
		listener.listen();
		return (mutants[3] ? (left % right) : (mutants[2] ? (left / right) : (mutants[1] ? (left * right) : (mutants[0] ? (left - right) : (left + right)))));
	}

//minus
	public static int AOR_minus(int left, int right, boolean... mutants) {
		listener.listen();
		return (mutants[3] ? (left % right) : (mutants[2] ? (left / right) : (mutants[1] ? (left * right) : (mutants[0] ? (left + right) : (left - right)))));
	}

	public static long AOR_minus(long left, long right, boolean... mutants) {
		listener.listen();
		return (mutants[3] ? (left % right) : (mutants[2] ? (left / right) : (mutants[1] ? (left * right) : (mutants[0] ? (left + right) : (left - right)))));
	}

	public static float AOR_minus(float left, float right, boolean... mutants) {
		listener.listen();
		return (mutants[3] ? (left % right) : (mutants[2] ? (left / right) : (mutants[1] ? (left * right) : (mutants[0] ? (left + right) : (left - right)))));
	}

	public static double AOR_minus(double left, double right, boolean... mutants) {
		listener.listen();
		return (mutants[3] ? (left % right) : (mutants[2] ? (left / right) : (mutants[1] ? (left * right) : (mutants[0] ? (left + right) : (left - right)))));
	}

//multiply
	public static int AOR_multiply(int left, int right, boolean... mutants) {
		listener.listen();
		return (mutants[3] ? (left % right) : (mutants[2] ? (left / right) : (mutants[1] ? (left - right) : (mutants[0] ? (left + right) : (left * right)))));
	}

	public static long AOR_multiply(long left, long right, boolean... mutants) {
		listener.listen();
		return (mutants[3] ? (left % right) : (mutants[2] ? (left / right) : (mutants[1] ? (left - right) : (mutants[0] ? (left + right) : (left * right)))));
	}

	public static float AOR_multiply(float left, float right, boolean... mutants) {
		listener.listen();
		return (mutants[3] ? (left % right) : (mutants[2] ? (left / right) : (mutants[1] ? (left - right) : (mutants[0] ? (left + right) : (left * right)))));
	}

	public static double AOR_multiply(double left, double right, boolean... mutants) {
		listener.listen();
		return (mutants[3] ? (left % right) : (mutants[2] ? (left / right) : (mutants[1] ? (left - right) : (mutants[0] ? (left + right) : (left * right)))));
	}

//divide
	public static int AOR_divide(int left, int right, boolean... mutants) {
		listener.listen();
		return (mutants[3] ? (left % right) : (mutants[2] ? (left * right) : (mutants[1] ? (left - right) : (mutants[0] ? (left + right) : (left / right)))));
	}

	public static long AOR_divide(long left, long right, boolean... mutants) {
		listener.listen();
		return (mutants[3] ? (left % right) : (mutants[2] ? (left * right) : (mutants[1] ? (left - right) : (mutants[0] ? (left + right) : (left / right)))));
	}

	public static float AOR_divide(float left, float right, boolean... mutants) {
		listener.listen();
		return (mutants[3] ? (left % right) : (mutants[2] ? (left * right) : (mutants[1] ? (left - right) : (mutants[0] ? (left + right) : (left / right)))));
	}

	public static double AOR_divide(double left, double right, boolean... mutants) {
		listener.listen();
		return (mutants[3] ? (left % right) : (mutants[2] ? (left * right) : (mutants[1] ? (left - right) : (mutants[0] ? (left + right) : (left / right)))));
	}

//remainder
	public static int AOR_remainder(int left, int right, boolean... mutants) {
		listener.listen();
		return (mutants[3] ? (left / right) : (mutants[2] ? (left * right) : (mutants[1] ? (left - right) : (mutants[0] ? (left + right) : (left % right)))));
	}

	public static long AOR_remainder(long left, long right, boolean... mutants) {
		listener.listen();
		return (mutants[3] ? (left / right) : (mutants[2] ? (left * right) : (mutants[1] ? (left - right) : (mutants[0] ? (left + right) : (left % right)))));
	}

	public static float AOR_remainder(float left, float right, boolean... mutants) {
		listener.listen();
		return (mutants[3] ? (left / right) : (mutants[2] ? (left * right) : (mutants[1] ? (left - right) : (mutants[0] ? (left + right) : (left % right)))));
	}

	public static double AOR_remainder(double left, double right, boolean... mutants) {
		listener.listen();
		return (mutants[3] ? (left / right) : (mutants[2] ? (left * right) : (mutants[1] ? (left - right) : (mutants[0] ? (left + right) : (left % right)))));
	}

//ROR
	public static boolean ROR_equals(int left, int right, boolean... mutants) {
		listener.listen();
		return (mutants[4] ? (left >= right) : (mutants[3] ? (left <= right) : (mutants[2] ? (left > right) : (mutants[1] ? (left < right) : (mutants[0] ? (left != right) : (left == right))))));
	}

	public static boolean ROR_equals(long left, long right, boolean... mutants) {
		listener.listen();
		return (mutants[4] ? (left >= right) : (mutants[3] ? (left <= right) : (mutants[2] ? (left > right) : (mutants[1] ? (left < right) : (mutants[0] ? (left != right) : (left == right))))));
	}

	public static boolean ROR_equals(double left, double right, boolean... mutants) {
		listener.listen();
		return (mutants[4] ? (left >= right) : (mutants[3] ? (left <= right) : (mutants[2] ? (left > right) : (mutants[1] ? (left < right) : (mutants[0] ? (left != right) : (left == right))))));
	}

	public static boolean ROR_not_equals(int left, int right, boolean... mutants) {
		listener.listen();
		return (mutants[4] ? (left >= right) : (mutants[3] ? (left <= right) : (mutants[2] ? (left > right) : (mutants[1] ? (left < right) : (mutants[0] ? (left == right) : (left != right))))));
	}
	public static boolean ROR_not_equals(long left, long right, boolean... mutants) {
		listener.listen();
		return (mutants[4] ? (left >= right) : (mutants[3] ? (left <= right) : (mutants[2] ? (left > right) : (mutants[1] ? (left < right) : (mutants[0] ? (left == right) : (left != right))))));
	}

	public static boolean ROR_not_equals(double left, double right, boolean... mutants) {
		listener.listen();
		return (mutants[4] ? (left >= right) : (mutants[3] ? (left <= right) : (mutants[2] ? (left > right) : (mutants[1] ? (left < right) : (mutants[0] ? (left == right) : (left != right))))));
	}

	public static boolean ROR_less(int left, int right, boolean... mutants) {
		listener.listen();
		return (mutants[4] ? (left >= right) : (mutants[3] ? (left <= right) : (mutants[2] ? (left > right) : (mutants[1] ? (left != right) : (mutants[0] ? (left == right) : (left < right))))));
	}

	public static boolean ROR_less(long left, long right, boolean... mutants) {
		listener.listen();
		return (mutants[4] ? (left >= right) : (mutants[3] ? (left <= right) : (mutants[2] ? (left > right) : (mutants[1] ? (left != right) : (mutants[0] ? (left == right) : (left < right))))));
	}

	public static boolean ROR_less(double left, double right, boolean... mutants) {
		listener.listen();
		return (mutants[4] ? (left >= right) : (mutants[3] ? (left <= right) : (mutants[2] ? (left > right) : (mutants[1] ? (left != right) : (mutants[0] ? (left == right) : (left < right))))));
	}

	public static boolean ROR_greater(int left, int right, boolean... mutants) {
		listener.listen();
		return (mutants[4] ? (left >= right) : (mutants[3] ? (left <= right) : (mutants[2] ? (left < right) : (mutants[1] ? (left != right) : (mutants[0] ? (left == right) : (left > right))))));
	}

	public static boolean ROR_greater(long left, long right, boolean... mutants) {
		listener.listen();
		return (mutants[4] ? (left >= right) : (mutants[3] ? (left <= right) : (mutants[2] ? (left < right) : (mutants[1] ? (left != right) : (mutants[0] ? (left == right) : (left > right))))));
	}

	public static boolean ROR_greater(double left, double right, boolean... mutants) {
		listener.listen();
		return (mutants[4] ? (left >= right) : (mutants[3] ? (left <= right) : (mutants[2] ? (left < right) : (mutants[1] ? (left != right) : (mutants[0] ? (left == right) : (left > right))))));
	}

	public static boolean ROR_less_equals(int left, int right, boolean... mutants) {
		listener.listen();
		return (mutants[4] ? (left >= right) : (mutants[3] ? (left > right) : (mutants[2] ? (left < right) : (mutants[1] ? (left != right) : (mutants[0] ? (left == right) : (left <= right))))));
	}

	public static boolean ROR_less_equals(long left, long right, boolean... mutants) {
		listener.listen();
		return (mutants[4] ? (left >= right) : (mutants[3] ? (left > right) : (mutants[2] ? (left < right) : (mutants[1] ? (left != right) : (mutants[0] ? (left == right) : (left <= right))))));
	}

	public static boolean ROR_less_equals(double left, double right, boolean... mutants) {
		listener.listen();
		return (mutants[4] ? (left >= right) : (mutants[3] ? (left > right) : (mutants[2] ? (left < right) : (mutants[1] ? (left != right) : (mutants[0] ? (left == right) : (left <= right))))));
	}

	public static boolean ROR_greater_equals(int left, int right, boolean... mutants) {
		listener.listen();
		return (mutants[4] ? (left <= right) : (mutants[3] ? (left > right) : (mutants[2] ? (left < right) : (mutants[1] ? (left != right) : (mutants[0] ? (left == right) : (left >= right))))));
	}

	public static boolean ROR_greater_equals(long left, long right, boolean... mutants) {
		listener.listen();
		return (mutants[4] ? (left <= right) : (mutants[3] ? (left > right) : (mutants[2] ? (left < right) : (mutants[1] ? (left != right) : (mutants[0] ? (left == right) : (left >= right))))));
	}
	
	public static boolean ROR_greater_equals(double left, double right, boolean... mutants) {
		listener.listen();
		return (mutants[4] ? (left <= right) : (mutants[3] ? (left > right) : (mutants[2] ? (left < right) : (mutants[1] ? (left != right) : (mutants[0] ? (left == right) : (left >= right))))));
	}

//LCR
	public static boolean LCR_or(boolean left, boolean right, boolean... mutants) {
		listener.listen();
		return mutants[0] ? (left && right) : (left || right);
	}

	public static boolean LCR_and(boolean left, boolean right, boolean... mutants) {
		listener.listen();
		return mutants[0] ? (left || right) : (left && right);
	}
}
