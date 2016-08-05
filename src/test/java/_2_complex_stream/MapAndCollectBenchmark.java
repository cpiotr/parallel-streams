package _2_complex_stream;

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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.toList;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class MapAndCollectBenchmark {
    @State(Scope.Thread)
    public static class BenchmarkState {

        List<String> strings;

        @Setup(Level.Trial)
        public void initialize() {
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

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(MapAndCollectBenchmark.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}
