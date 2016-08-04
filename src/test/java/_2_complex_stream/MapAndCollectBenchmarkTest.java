package _2_complex_stream;

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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.toList;

public class MapAndCollectBenchmarkTest {
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

        List<String> strings;

        @Setup(Level.Trial)
        public void
        initialize() {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("sample-text-file.txt")));
            strings = bufferedReader.lines().limit(1_000).collect(toList());
        }
    }

    @Benchmark
    public void serialStreamMapping(BenchmarkState state, Blackhole bh) {
        List<Word> words = state.strings.stream()
                .map(Sentence::new)
                .flatMap(sentence -> sentence.getWords().stream())
                .collect(toList());
        bh.consume(words);
    }

    @Benchmark
    public void parallelStreamIteration(BenchmarkState state, Blackhole bh) {
        List<Word> words = state.strings.stream()
                .parallel()
                .map(Sentence::new)
                .flatMap(sentence -> sentence.getWords().stream())
                .collect(toList());
        bh.consume(words);
    }
}
