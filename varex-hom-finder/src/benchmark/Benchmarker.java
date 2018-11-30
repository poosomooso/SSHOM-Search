package benchmark;

public class Benchmarker {
	
  public static final Benchmarker instance = new Benchmarker();
  private Benchmarker() {
	  // private constructor
  }
  
  private long startTime = -1L;

  public void start() {
    startTime = System.currentTimeMillis();
  }

  public void timestamp(String descriptor) {
    System.out
        .printf("TIME %10d|%s\n", System.currentTimeMillis() - startTime, descriptor);
  }

}
