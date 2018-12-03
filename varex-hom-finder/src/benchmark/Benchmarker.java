package benchmark;

public class Benchmarker {
	
  public static final Benchmarker instance = new Benchmarker();
  
  private Benchmarker() {
	  // private constructor
  }
  
  private long startTime = Long.MIN_VALUE;

  public void start() {
    startTime = System.currentTimeMillis();
  }

  public void timestamp(String descriptor) {
	  if (startTime != Long.MIN_VALUE) {
		  System.out.printf("TIME %10d|%s\n", System.currentTimeMillis() - startTime, descriptor);
	  }
  }

}
