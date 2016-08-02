package _0_ordering;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

public class KeepOrderTest {
    @Test
    public void shouldKeepElementsOrder() throws Exception {
        List<Integer> list = IntStream.range(0, 10)
                .boxed()
                .parallel()
                .map(i -> i * i)
                .filter(i -> i % 2 != 0)
                .collect(toList());
        System.out.println("list = " + list);

        assertThat(list, is(sorted(Integer::compare)));
    }

    @Test
    public void shouldScrambleElements() throws Exception {
        List<Integer> list = new ArrayList<>();

        IntStream.range(0, 10)
                .boxed()
                .parallel()
                .map(i -> i * i)
                .filter(i -> i % 2 != 0)
                .forEach(list::add);
        System.out.println("list = " + list);

        assertThat(list, is(not(sorted(Integer::compare))));
    }

    private <T> Matcher<List<T>> sorted(Comparator<T> comparator) {
        return new TypeSafeMatcher<List<T>>() {
            @Override
            protected boolean matchesSafely(List<T> list) {
                for (int i = 1; i < list.size(); i++) {
                    T previous = list.get(i - 1);
                    T current = list.get(i);
                    if (!areInOrder(previous, current)) {
                        return false;
                    }
                }
                return true;
            }

            private boolean areInOrder(T previous, T current) {
                return comparator.compare(previous, current) <= 0;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("sorted");
            }
        };
    }
}
