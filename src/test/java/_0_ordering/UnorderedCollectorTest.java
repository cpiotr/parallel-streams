package _0_ordering;

import com.google.common.collect.Sets;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertThat;

public class UnorderedCollectorTest {
    @Test
    public void shouldShuffleElements() {
        Set<String> set = Sets.newHashSet("Ala", "ma", "kota", "który", "jest", "niesforny");
        System.out.println("input  =\t\t\t" + set);

        Collection<String> expectedCollection = set.parallelStream()
                .map(String::toUpperCase)
                .collect(toList());
        System.out.println("toList =\t\t\t" + expectedCollection);

        Collection<String> actualCollection = set.parallelStream()
                .map(String::toUpperCase)
                .collect(toUnorderedList());
        System.out.println("toUnorderedList =\t" + actualCollection);

        assertThat(actualCollection, hasItems(asArray(expectedCollection)));
    }

    static <T> Collector<T, List<T>, List<T>> toUnorderedList() {
        return new Collector<T, List<T>, List<T>>() {
            @Override
            public Supplier<List<T>> supplier() {
                return ArrayList::new;
            }

            @Override
            public BiConsumer<List<T>, T> accumulator() {
                return (ts, e) -> {
                    waitAMoment();
                    ts.add(e);
                };
            }

            @Override
            public BinaryOperator<List<T>> combiner() {
                return (first, second) -> {
                    List<T> result = new ArrayList<T>(first);
                    first.addAll(second);
                    return result;
                };
            }

            @Override
            public Function<List<T>, List<T>> finisher() {
                return Function.identity();
            }

            @Override
            public Set<Characteristics> characteristics() {
                Set<Characteristics> characteristics = new HashSet<>();
                characteristics.add(Characteristics.CONCURRENT);
                characteristics.add(Characteristics.UNORDERED);
                characteristics.add(Characteristics.IDENTITY_FINISH);
                return characteristics;
            }

            private void waitAMoment() {
                try {
                    Thread.sleep((long) (Math.random() * 100));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private String[] asArray(Collection<String> collection) {
        return collection.toArray(new String[collection.size()]);
    }
}
