# Kafka Producer Twitter

In this Maven Application we have a Producer consuming tweets from Twitter, producing messages, and sending them to Kafka.

The majority of the code of this app is based on what I learned from this course:
                                                    
    Apache Kafka Series - Learn Apache Kafka for Beginners v2, by Stephane Maarek
    https://www.udemy.com/course/apache-kafka/

The course is great - I highly recommend it.

See the instructions from doc/kafka-on-windows.txt to learn 
how to install Kafka on Windows and make it work, so that you
will be able to test this app. Do not forget to create the topic before running the app:

    C:\>kafka-topics --zookeeper 127.0.0.1:2181 --create --topic twitter_tweets --partitions 6 --replication-factor 1

You will need a developer account at Twitter so that you can enter the keys, secret,
token and token secret values at resources/app.properties.

We use the library HoseBird Client (hbc) to consume messages from Twitter:

    https://github.com/twitter/hbc
