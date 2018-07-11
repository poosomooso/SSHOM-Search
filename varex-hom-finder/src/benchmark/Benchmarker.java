package benchmark;

public class Benchmarker {
  private long startTime = -1L;

  public void start() {
    startTime = System.currentTimeMillis();
  }

  public void timestamp(String descriptor) {
    System.out
        .printf("%10d|%s\n", System.currentTimeMillis() - startTime, descriptor);
  }

}
