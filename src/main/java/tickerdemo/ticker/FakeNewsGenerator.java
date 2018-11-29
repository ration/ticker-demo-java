package tickerdemo.ticker;

import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.BufferOverflowStrategy;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class FakeNewsGenerator implements NewsProvider {
    private final EmitterProcessor<News> source = EmitterProcessor.create();
    private final Random generator = new Random();
    private long generationSpeed = 100L;
    private final DirectProcessor<Long> ticker = DirectProcessor.create();
    private AtomicLong counter = new AtomicLong(0);

    @SuppressWarnings("unused")
    private final Disposable tickerSubscription;

    public FakeNewsGenerator() {
        tickerSubscription = ticker.switchMap((x) -> Flux.interval(Duration.ofMillis(generationSpeed))).
                subscribe(val -> {
                    long id = counter.incrementAndGet();
                    source.onNext(fakeNews(id));
                    ticker.onNext(generationSpeed);
                });
        ticker.onNext(generationSpeed);
    }

    private News fakeNews(long id) {
        boolean breaking = generator.nextInt(10) > 8;
        return new News(id, breaking, System.currentTimeMillis(), id + ": News Title", "Longer description");
    }

    @Override
    public Flux<News> news() {
        final AtomicBoolean priorityOnly = new AtomicBoolean(false);
        return source.
                onBackpressureBuffer(100, news -> enablePriority(priorityOnly),
                        BufferOverflowStrategy.DROP_OLDEST).
                filter(news -> !priorityOnly.get() || news.isBreaking());
    }

    private void enablePriority(AtomicBoolean priorityOnly) {
        priorityOnly.set(true);
        Flux.just(0).
                delayElements(Duration.ofSeconds(10)).
                subscribeOn(Schedulers.elastic()).
                subscribe(v -> priorityOnly.set(false));
    }

    @Override
    public void setSpeed(Long speed) {
        generationSpeed = speed;
    }
}
