package tickerdemo.ticker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.rsocket.AbstractRSocket;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.transport.netty.server.NettyContextCloseable;
import io.rsocket.transport.netty.server.WebsocketServerTransport;
import io.rsocket.util.DefaultPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Controller
public final class NewsSocket {
    private final Logger LOG = LoggerFactory.getLogger("NewsSocket");
    private final ObjectMapper mapper = new ObjectMapper();
    private final int port = 9988;
    private final NewsProvider newsProvider;

    public NewsSocket(@Autowired NewsProvider newsProvider) {
        this.newsProvider = newsProvider;
        Mono<NettyContextCloseable> closeable = RSocketFactory
                .receive()
                .acceptor((a, b) -> handler())
                .transport(WebsocketServerTransport.create("localhost", port))
                .start();
        closeable.subscribe((v) -> LOG.info("Subsrcriber"));
    }

    private Mono<RSocket> handler() {
        return Mono.just(new AbstractRSocket() {
            public Flux<Payload> requestStream(Payload payload) {
                return newsProvider.news().publishOn(Schedulers.single()).map(value -> mapToJsonString(value));
            }
        });
    }

    private Payload mapToJsonString(News value) {
        try {
            return DefaultPayload.create(mapper.writeValueAsString(value));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
