package _3_reducing;

import org.junit.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ReduceBenchmarkTest {
    @Test
    public void launchBenchmark() throws Exception {
        Options opt = new OptionsBuilder()
                .include(this.getClass().getSimpleName())
                .mode(Mode.AverageTime)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(5)
                .measurementIterations(5)
                .forks(1)
                .shouldFailOnError(true)
                .shouldDoGC(false)
                .build();

        new Runner(opt).run();
    }

    @State(Scope.Thread)
    public static class BenchmarkState {
        List<Integer> input;
        @Setup(Level.Trial)

        public void
        initialize() {
            input = IntStream.iterate(0, i -> i + 1)
                    .limit(100_000)
                    .boxed()
                    .collect(Collectors.toList());
        }
    }

    @Benchmark
    public void serialAddToList(BenchmarkState state, Blackhole bh) {
        List<Integer> list = new CopyOnWriteArrayList<>();
        state.input
                .stream()
                .forEach(list::add);

        bh.consume(list);
    }

    @Benchmark
    public void parallelAddToList(BenchmarkState state, Blackhole bh) {
        List<Integer> list = new CopyOnWriteArrayList<>();
        state.input
                .stream()
                .parallel()
                .forEach(list::add);

        bh.consume(list);
    }

    @Benchmark
    public void serialReduce(BenchmarkState state, Blackhole bh) {
        List<Integer> list = state.input.stream()
                .map(Arrays::asList)
                .reduce(new ArrayList<>(), ReduceBenchmarkTest::concat);
        bh.consume(list);
    }

    @Benchmark
    public void parallelReduce(BenchmarkState state, Blackhole bh) {
        List<Integer> list = state.input.stream()
                .parallel()
                .map(Arrays::asList)
                .reduce(new ArrayList<>(), ReduceBenchmarkTest::concat);
        bh.consume(list);
    }

    static <T> List<T> concat(List<T> first, List<T> second) {
        List<T> both = new ArrayList<T>(first);
        both.addAll(second);
        return both;
    }
}
