package _6_sizing_pool;

import com.google.common.collect.Sets;
import org.junit.Test;

import java.util.Set;
import java.util.stream.IntStream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class SetCustomPoolSizeTest {
    @Test
    public void shouldLimitFJPool() throws Exception {
        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "2");

        Set<String> threadNames = Sets.newConcurrentHashSet();
        IntStream.range(0, 10_000)
                .parallel()
                .mapToObj(__ -> Thread.currentThread().getName())
                .forEach(threadNames::add);

        System.out.println(threadNames);

        assertThat(threadNames.size(), is(2+1));
    }
}
