import java.util.ArrayList;
import java.util.Map;

import twitter4j.HashtagEntity;
import twitter4j.MediaEntity;
import twitter4j.Status;

import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.apache.storm.tuple.Tuple;

//import Bolt interface packages
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;


public class DetailsExtractorBolt implements IRichBolt {
    private static final long serialVersionUID = 1L;
    private OutputCollector collector;


    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.collector = outputCollector;
    }

    @Override
    public void execute(Tuple tuple) {
        Status status = (Status)tuple.getValueByField("status");

        if (!status.getLang().equals("en")) {
            return;
        }

        MediaEntity[] media = status.getMediaEntities();
        if (media.length == 0) {
            return;
        }

        ArrayList<String> mediaUrls = new ArrayList<>();
        for (MediaEntity m : media) {
            String mediaUrl = m.getMediaURL();
            if (mediaUrl.endsWith(".jpg") || mediaUrl.endsWith(".png")) {
                mediaUrls.add(mediaUrl);
            }
        }

        ArrayList<String> hashtags = new ArrayList<>();
        for(HashtagEntity h : status.getHashtagEntities()) {
            hashtags.add(h.getText());
        }

        collector.emit(new Values(status.getUser().getScreenName(),
                                  status.getId(),
                                  status.getText(),
                                  hashtags,
                                  mediaUrls));
    }

    @Override
    public void cleanup() {

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("screenName",
                                                "statusID",
                                                "text",
                                                "hashtags",
                                                "mediaUrls"));
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }

}
