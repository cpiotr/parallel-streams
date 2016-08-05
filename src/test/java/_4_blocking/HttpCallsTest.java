package _4_blocking;

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
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class HttpCallsTest {
    public static final int DELAY_MILLIS = 500;
    
    @Rule
    public WireMockRule service = new WireMockRule(WireMockConfiguration.options().jettyAcceptors(4));

    private CloseableHttpClient httpClient;

    private List<String> urls;

    @Test(timeout = 100 * DELAY_MILLIS)
    public void shouldName() throws Exception {
        int totalSerialDelay = urls.size() * DELAY_MILLIS;

        Stopwatch stopwatch = Stopwatch.createStarted();
        urls.stream()
                .parallel()
                .map(HttpGet::new)
                .map(Errors.rethrow().wrapFunction(httpClient::execute))
                .map(HttpResponse::getStatusLine)
                .forEach(System.out::println);
        stopwatch.stop();

        assertThat(stopwatch.elapsed(TimeUnit.MILLISECONDS), is(lessThan(totalSerialDelay)));
    }

    @Before
    public void setUp() throws Exception {
        httpClient = HttpClientBuilder.create()
                .setMaxConnTotal(8)
                .setMaxConnPerRoute(8)
                .build();

        service.stubFor(get(urlEqualTo("/slow")).willReturn(
                aResponse()
                        .withStatus(200)
                        .withFixedDelay(DELAY_MILLIS)));

        urls = Stream.generate(() -> String.format("http://localhost:%d/slow", service.port()))
                .limit(5)
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
