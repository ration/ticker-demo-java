package tickerdemo.ticker;

import reactor.core.publisher.Flux;

public interface NewsProvider {
    Flux<News> news();

    void setSpeed(Long speed);
}
