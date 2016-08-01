package _1_simple_stream;

import org.junit.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import org.openjdk.jmh.runner.options.VerboseMode;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class IterationTest {
    @Test
    public void launchBenchmark() throws Exception {
        Options opt = new OptionsBuilder()
                // Specify which benchmarks to run.
                // You can be more specific if you'd like to run only one benchmark per test.
                .include(this.getClass().getSimpleName())
                // Set the following options as needed
                .mode(Mode.AverageTime)
                .timeUnit(TimeUnit.MICROSECONDS)
                .warmupTime(TimeValue.seconds(1))
                .warmupIterations(2)
                .measurementTime(TimeValue.seconds(1))
                .measurementIterations(2)
                .threads(2)
                .forks(1)
                .shouldFailOnError(true)
                .shouldDoGC(true)
                //.jvmArgs("-XX:+UnlockDiagnosticVMOptions", "-XX:+PrintInlining")
                //.addProfiler(WinPerfAsmProfiler.class)
                .build();

        new Runner(opt).run();
    }

    @State(Scope.Thread)
    public static class BenchmarkState {
        @Setup(Level.Trial)
        public void
        initialize() {
        }
    }

    @Benchmark
    public void sequentialIteration(BenchmarkState state, Blackhole bh) {
        for (int i = 0; i < 1_000; i++) {
            int a = i * i;
        }
    }

    @Benchmark
    public void sequentialStreamIteration(BenchmarkState state, Blackhole bh) {
        IntStream.iterate(0, i -> i)
                .limit(1_000)
                .forEach(i -> {
                    int a = i * i;
                });
    }

    @Benchmark
    public void parallelStreamIteration(BenchmarkState state, Blackhole bh) {
        IntStream.iterate(0, i -> i)
                .parallel()
                .limit(1_000)
                .forEach(i -> {
                    int a = i * i;
                });
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(IterationTest.class.getSimpleName())
                .warmupIterations(2)
                .warmupTime(TimeValue.milliseconds(2))
                .measurementIterations(2)
                .measurementTime(TimeValue.milliseconds(2))
                .verbosity(VerboseMode.NORMAL)
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}
