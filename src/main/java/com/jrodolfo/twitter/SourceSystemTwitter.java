package com.jrodolfo.twitter;

import com.jrodolfo.twitter.util.PropertyUtil;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.Hosts;
import com.twitter.hbc.core.HttpHosts;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;

class SourceSystemTwitter {

    private String consumerKey;
    private String consumerSecret;
    private String accessToken;
    private String accessTokenSecret;
    private List<String> twitterTopics;

    SourceSystemTwitter() {
        Properties properties = PropertyUtil.getProperties();
        consumerKey = properties.getProperty("oauth.consumerKey");
        consumerSecret = properties.getProperty("oauth.consumerSecret");
        accessToken = properties.getProperty("oauth.accessToken");
        accessTokenSecret = properties.getProperty("oauth.accessTokenSecret");
        String twitterTopicsStr = properties.getProperty("twitter.topics");
        String[] twitterTopicsArray = twitterTopicsStr.split("\\s*,\\s*");
        twitterTopics = new ArrayList<>(Arrays.asList(twitterTopicsArray));
    }

    Client createTwitterClient(BlockingQueue<String> msgQueue) {
        // Declare the host you want to connect to, the endpoint, and authentication (basic auth or oauth)
        Hosts hoseBirdHosts = new HttpHosts(Constants.STREAM_HOST);
        StatusesFilterEndpoint hoseBirdEndpoint = new StatusesFilterEndpoint();
        hoseBirdEndpoint.trackTerms(twitterTopics);
        Authentication hoseBirdAuth = new OAuth1(consumerKey, consumerSecret, accessToken, accessTokenSecret);
        // Creating a client
        ClientBuilder builder = new ClientBuilder()
                .name("HoseBird-Client-01") // optional: mainly for the logs
                .hosts(hoseBirdHosts)
                .authentication(hoseBirdAuth)
                .endpoint(hoseBirdEndpoint)
                .processor(new StringDelimitedProcessor(msgQueue));
        return builder.build();
    }
}
