package _1_simple_stream;

import com.google.common.base.Stopwatch;

import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;

public class IterationTest {
    // -XX:+UnlockDiagnosticVMOptions -XX:+UnlockCommercialFeatures -XX:+FlightRecorder
    public static void main(String[] args) throws Exception {
        System.in.read();

        Stopwatch watch = Stopwatch.createStarted();
        LongStream.range(0, 1_000_000_000_000L)
                .parallel()
                .forEach(i -> {
                    // no-op
                });
        watch.stop();
        System.out.format("%d seconds%n", watch.elapsed(TimeUnit.SECONDS));

    }
}
