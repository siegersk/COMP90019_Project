import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import org.apache.storm.utils.Utils;
import java.util.*;

//import storm tuple packages
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;

//import Spout interface packages
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichSpout;
import org.apache.storm.topology.OutputFieldsDeclarer;


public class TweetSpout implements IRichSpout {

    private SpoutOutputCollector collector;
    private LinkedBlockingQueue<Status> queue;
    private TwitterStream twitterStream;


    @Override
    public void open(Map map, TopologyContext topologyContext, SpoutOutputCollector spoutOutputCollector) {
        this.collector = spoutOutputCollector;
        queue = new LinkedBlockingQueue(100);


        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setOAuthConsumerKey(ProjectConstants.CONSUMER_KEY_KEY);
        cb.setOAuthConsumerSecret(ProjectConstants.CONSUMER_SECRET_KEY);
        cb.setOAuthAccessToken(ProjectConstants.ACCESS_TOKEN_KEY);
        cb.setOAuthAccessTokenSecret(ProjectConstants.ACCESS_TOKEN_SECRET_KEY);

        twitterStream = new TwitterStreamFactory(cb.build()).getInstance();

        StatusListener listener = new StatusListener() {
            @Override
            public void onStatus(Status status) {
                queue.offer(status);
            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) { }

            @Override
            public void onTrackLimitationNotice(int numberOfLimitedStatuses) { }

            @Override
            public void onScrubGeo(long userId, long upToStatusId) { }

            @Override
            public void onStallWarning(StallWarning warning) { }

            @Override
            public void onException(Exception ex) { ex.printStackTrace(); }
        };

        twitterStream.addListener(listener);
        twitterStream.sample();

    }

    @Override
    public void close() {
        twitterStream.shutdown();
    }

    @Override
    public void activate() {

    }

    @Override
    public void deactivate() {

    }

    @Override
    public void nextTuple() {
        Status status = queue.poll();
        if(status == null) {
            Utils.sleep(50);
        } else {
            collector.emit(new Values(status));
        }
    }

    @Override
    public void ack(Object o) {

    }

    @Override
    public void fail(Object o) {

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("status"));
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }
}
