package _1_simple_stream;

import java.util.stream.LongStream;

public class IterationTest {
    // -XX:+UnlockDiagnosticVMOptions -XX:+UnlockCommercialFeatures -XX:+FlightRecorder
    public static void main(String[] args) throws Exception {
        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "128");
        System.in.read();

        LongStream.range(0, 1_000_000_000_000L)
                .parallel()
                .forEach(i -> {
                    // no-op
                });
    }
}
