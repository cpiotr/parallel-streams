package _2_complex_stream;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

class Sentence {
    public static final String SEPARATORS = "[ .,?!]";
    private List<Word> words;

    public Sentence(String line) {
        this.words = Arrays.stream(line.split(SEPARATORS))
                .map(Word::new)
                .collect(toList());
    }

    public List<Word> getWords() {
        return words;
    }
}
