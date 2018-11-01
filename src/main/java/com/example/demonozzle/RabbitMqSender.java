package com.example.demonozzle;

import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Component;
import reactor.core.scheduler.Schedulers;
import reactor.rabbitmq.ReactorRabbitMq;
import reactor.rabbitmq.ResourcesSpecification;
import reactor.rabbitmq.Sender;
import reactor.rabbitmq.SenderOptions;

import javax.annotation.PreDestroy;

import static reactor.rabbitmq.ExchangeSpecification.exchange;
import static reactor.rabbitmq.ResourcesSpecification.queue;

@Component
public class RabbitMqSender implements FactoryBean<Sender> {
    private final Sender sender;
    private static Logger log = LoggerFactory.getLogger(RabbitMqSender.class);

    public RabbitMqSender() {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.useNio();
        // Create a sender
        SenderOptions senderOptions = new SenderOptions()
                .connectionFactory(connectionFactory)
                .resourceManagementScheduler(Schedulers.elastic());
        this.sender = ReactorRabbitMq.createSender(senderOptions);
        this.sender.declare(exchange("demo").type("topic"))
                .then(sender.declare(queue("demo.sink").durable(true)))
                .then(sender.bind(ResourcesSpecification.binding("demo", "#", "demo.sink")))
                .doOnSuccess(x -> log.info("Exchange and queue declared and bound."))
                .doOnError(e -> {
                    log.error("Connection failed.", e);
                    System.exit(1);
                }).block();
    }

    @PreDestroy
    void close() {
        log.info("Closing ...");
        this.sender.close();
    }

    @Override
    public Sender getObject() {
        return this.sender;
    }

    @Override
    public Class<?> getObjectType() {
        return Sender.class;
    }
}
