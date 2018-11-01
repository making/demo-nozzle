package com.example.demonozzle;

import org.cloudfoundry.doppler.DopplerClient;
import org.cloudfoundry.doppler.Envelope;
import org.cloudfoundry.doppler.FirehoseRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.retry.Backoff;
import reactor.retry.Repeat;
import reactor.retry.Retry;

import javax.annotation.PreDestroy;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class DemoNozzle implements ApplicationRunner {
    private final DopplerClient dopplerClient;
    private final CloudFoundryProps props;
    private static final Logger log = LoggerFactory.getLogger(DemoNozzle.class);
    private Disposable disposable;

    public DemoNozzle(DopplerClient dopplerClient, CloudFoundryProps props) {
        this.dopplerClient = dopplerClient;
        this.props = props;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        FirehoseRequest request = FirehoseRequest.builder()
                .subscriptionId(this.props.getFirehose().getSubscriptionId())
                .build();

        AtomicBoolean completed = new AtomicBoolean(false);
        Repeat<AtomicBoolean> repeat = Repeat.<AtomicBoolean>onlyIf(ctx -> ctx.applicationContext().get())
                .withApplicationContext(completed)
                .doOnRepeat(x -> log.info("doOnRepeat({})", x))
                .backoff(Backoff.fixed(Duration.ofSeconds(1)));
        Retry<AtomicBoolean> retry = Retry.<AtomicBoolean>onlyIf(ctx -> ctx.applicationContext().get())
                .withApplicationContext(completed)
                .doOnRetry(x -> log.info("doOnRetry({})", x))
                .backoff(Backoff.fixed(Duration.ofSeconds(1)))
                .timeout(Duration.ofMinutes(10));

        Flux<Envelope> firehose = this.dopplerClient.firehose(request)
                .doOnNext(envelope -> {
                    Instant instant = Instant.ofEpochMilli(envelope.getTimestamp() / 1_000_000);
                    log.info("{}\t:\t{}", LocalDateTime.ofInstant(instant, ZoneId.systemDefault()), envelope);
                })
                .doOnRequest(x -> log.info("doOnRequest({})", x))
                .doOnTerminate(() -> log.info("doOnTerminate()"))
                .doOnCancel(() -> log.info("doOnCancel()"))
                .doOnError(e -> {
                    if (!completed.get()) {
                        log.error("doOnError()", e);
                    }
                })
                .doOnComplete(() -> {
                    completed.set(true);
                    log.info("doOnComplete()");
                })
                .onBackpressureDrop(envelope -> log.warn("Drop {}", envelope))
                .repeatWhen(repeat)
                .retryWhen(retry);
        this.disposable = firehose.subscribe();
    }

    @PreDestroy
    public void close() {
        log.info("Closing...");
        this.disposable.dispose();
    }
}
