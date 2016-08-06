package _5_custom_pool;

import com.diffplug.common.base.Errors;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.base.Stopwatch;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class WrapParallelStreamTest {
    public static final int DELAY_MILLIS = 2_000;

    public static final int NUMBER_OF_URLS = 5;

    @Rule
    public WireMockRule service = new WireMockRule(WireMockConfiguration.options().jettyAcceptors(4));

    private CloseableHttpClient httpClient;

    private List<String> urls;

    @Test(timeout = NUMBER_OF_URLS * DELAY_MILLIS * 2)
    public void shouldFailDueToBlockedOperations() throws Exception {
        int totalSerialDelay = urls.size() * DELAY_MILLIS;

        Stopwatch stopwatch = Stopwatch.createStarted();
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        ForkJoinTask<?> task = forkJoinPool.submit(() ->
                urls.stream()
                        .parallel()
                        .map(url -> url + "slooow")
                        .map(HttpGet::new)
                        .map(Errors.rethrow().wrapFunction(httpClient::execute))
                        .map(HttpResponse::getStatusLine)
                        .forEach(System.out::println)
        );
        forkJoinPool.shutdown();
        stopwatch.stop();

        while(!task.isCompletedNormally()) {
            System.out.println("Meh, sleeping...");
            Thread.sleep(500);
        }

        assertThat(stopwatch.elapsed(TimeUnit.MILLISECONDS), is(lessThan(totalSerialDelay)));
    }

    @Before
    public void setUp() throws Exception {
        httpClient = HttpClientBuilder.create()
                .setMaxConnTotal(8)
                .setMaxConnPerRoute(8)
                .build();

        service.stubFor(get(urlEqualTo("/slooow")).willReturn(
                aResponse()
                        .withStatus(200)
                        .withFixedDelay(DELAY_MILLIS)));

        urls = Stream.generate(() -> String.format("http://localhost:%d/", service.port()))
                .limit(NUMBER_OF_URLS)
                .collect(toList());
    }

    @After
    public void tearDown() throws Exception {
        httpClient.close();
    }

    static private <T extends Number, U extends Number> Matcher<T> lessThan(U value) {
        return new TypeSafeMatcher<T>() {
            @Override
            protected boolean matchesSafely(T item) {
                return Double.compare(item.doubleValue(), value.doubleValue()) < 0;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("less than ")
                        .appendValue(value);
            }
        };
    }

}
