package com.example.demonozzle;

import org.cloudfoundry.doppler.DopplerClient;
import org.cloudfoundry.doppler.Envelope;
import org.cloudfoundry.doppler.FirehoseRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class DemoNozzle implements ApplicationRunner {
    private final DopplerClient dopplerClient;
    private final CloudFoundryProps props;
    private static final Logger log = LoggerFactory.getLogger(DemoNozzle.class);

    public DemoNozzle(DopplerClient dopplerClient, CloudFoundryProps props) {
        this.dopplerClient = dopplerClient;
        this.props = props;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        FirehoseRequest request = FirehoseRequest.builder()
                .subscriptionId(this.props.getFirehose().getSubscriptionId())
                .build();
        Flux<Envelope> firehose = this.dopplerClient.firehose(request)
                .doOnNext(envelope -> {
                    log.info("{}", envelope);
                })
                .doOnError(e -> {
                    log.error("firehose error", e);
                });
        firehose.subscribe();
    }
}
