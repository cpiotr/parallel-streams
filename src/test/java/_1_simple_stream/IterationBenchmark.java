package _1_simple_stream;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(2)
public class IterationBenchmark {
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
            bh.consume(a);
        }
    }

    @Benchmark
    public void sequentialStreamIteration(BenchmarkState state, Blackhole bh) {
        IntStream.iterate(0, i -> i)
                .limit(1_000)
                .forEach(i -> {
                    int a = i * i;
                    bh.consume(a);
                });
    }

    @Benchmark
    public void parallelStreamIteration(BenchmarkState state, Blackhole bh) {
        IntStream.iterate(0, i -> i)
                .parallel()
                .limit(1_000)
                .forEach(i -> {
                    int a = i * i;
                    bh.consume(a);
                });
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(IterationBenchmark.class.getSimpleName())
                .shouldDoGC(false)
                .build();

        new Runner(opt).run();
    }
}
