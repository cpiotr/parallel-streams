package _2_complex_stream;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

class Word {
    String value;
    List<Integer> randomStats;

    public Word(String value) {
        this.value = value.toUpperCase();
        randomStats = generateRandom();
    }
    private List<Integer> generateRandom() {
        List<Integer> collect = Stream.generate(() -> Math.random() * 1000)
                .map(Double::intValue)
                .limit(500)
                .collect(toList());
        return collect;
    }

    @Override
    public String toString() {
        return value;
    }
}
