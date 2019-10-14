package com.jrodolfo.twitter;

import com.jrodolfo.twitter.util.PropertyUtil;
import com.twitter.hbc.core.Client;
import org.apache.kafka.clients.producer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class AppMain {

    private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private ProducerKafka producerKafka;
    private SourceSystemTwitter sourceSystemTwitter;
    private BlockingQueue<String> msgQueue;
    private String topic;

    private AppMain() {
        Properties properties = PropertyUtil.getProperties();
        topic = properties.getProperty("kafka.topic");
        producerKafka = new ProducerKafka();
        sourceSystemTwitter = new SourceSystemTwitter();
        // set up your blocking queues: be sure to size these properly based on expected TPS of your stream
        msgQueue = new LinkedBlockingQueue<String>(1_000);
    }

    public static void main(String[] args) {
        new AppMain().run();
    }

    private void run() {

        logger.info("Setting up...");

        // create a twitter client
        Client client = sourceSystemTwitter.createTwitterClient(msgQueue);
        // attempt to establish a connection
        client.connect();

        // create a kafka producer
        KafkaProducer<String, String> producer = producerKafka.createKafkaProducer();

        // add a shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("stopping application...");
            logger.info("shutting down client from twitter...");
            client.stop();
            logger.info("closing producer...");
            // we need to close the producer so that all messages are sent before the shutdown
            producer.close();
            logger.info("done!");
        }));

        // loop to send tweet to kafka on a different thread, or multiple different threads
        while (!client.isDone()) {
            String message = null;
            try {
                message = msgQueue.poll(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
                client.stop();
            }
            if (message != null) {
                logger.info("\n\n" + message);
                ProducerRecord<String, String> record = new ProducerRecord<>(topic, message);
                // send data asynchronously
                producer.send(record, (recordMetadata, e) -> {
                    // executes every time a record is successfully sent or an exception is thrown
                    if (e == null) {
                        // the record was successfully sent
                        Timestamp stamp = new Timestamp(recordMetadata.timestamp());
                        Date date = new Date(stamp.getTime());
                        logger.info("\n\n\t\tReceived new metadata:\n" +
                                "\n\tTopic: " + recordMetadata.topic() +
                                "\n\tPartition: " + recordMetadata.partition() +
                                "\n\tOffset: " + recordMetadata.offset() +
                                "\n\tDate: " + date);
                    } else {
                        logger.error("Error while producing", e);
                        e.printStackTrace();
                    }
                });
            }
        }
        logger.info("End of application");
    }
}
