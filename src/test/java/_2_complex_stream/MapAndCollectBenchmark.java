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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.toList;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(2)
public class MapAndCollectBenchmark {
    @State(Scope.Thread)
    public static class BenchmarkState {

        List<String> strings;

        @Setup(Level.Trial)
        public void initialize() {
            InputStream sampleFileAsStream = getClass().getClassLoader().getResourceAsStream("sample-text-file.txt");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(sampleFileAsStream));
            strings = bufferedReader.lines()
                    .limit(1_000)
                    .collect(toList());
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
    public void parallelStreamMapping(BenchmarkState state, Blackhole bh) {
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
                .shouldDoGC(false)
                .build();

        new Runner(opt).run();
    }
}
