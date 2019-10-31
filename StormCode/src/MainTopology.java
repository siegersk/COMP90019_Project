import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Tuple;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;

import java.util.HashMap;
import java.util.Map;

public class MainTopology {
    public static final String TOPOLOGY_NAME = "twitter-topology";
    public static final String TWITTER_SPOUT_ID = "twitterSpout";
    public static final String DETAILS_BOLT_ID = "detailsExtractorBolt";
    public static final String IMAGE_TAGS_EXTRACTOR_BOLD_ID = "imageTagsExtractorBolt";
    public static final String IMAGE_AI_TAGS_EXTRACTOR_BOLT_ID = "imAiTagsExtractorBolt";
    public static final String IMAGE_AZ_TAGS_EXTRACTOR_BOLT_ID = "imAzTagsExtractorBolt";
    public static final String COMPARISON_BOLT_ID = "comparisonBolt";
    public static final String DB_WRITER_BOLT_ID = "dbWriterBolt";

    public static void main(String[] args) throws AlreadyAliveException, InvalidTopologyException {
        TopologyBuilder topologyBuilder = new TopologyBuilder();
        topologyBuilder.setSpout(TWITTER_SPOUT_ID, new TweetSpout());

        topologyBuilder.setBolt(DETAILS_BOLT_ID, new DetailsExtractorBolt())
                .shuffleGrouping(TWITTER_SPOUT_ID);

        ImAITagsExtractorBolt bolt = new ImAITagsExtractorBolt();
        Map env = new HashMap();
        env.put("PYTHONPATH", ProjectConstants.PYTHON_PATH);
        bolt.setEnv(env);

        topologyBuilder.setBolt(IMAGE_AI_TAGS_EXTRACTOR_BOLT_ID, bolt)
                .shuffleGrouping(DETAILS_BOLT_ID);

        topologyBuilder.setBolt(IMAGE_AZ_TAGS_EXTRACTOR_BOLT_ID, new AzTagsExtractorBolt())
                .shuffleGrouping(IMAGE_AI_TAGS_EXTRACTOR_BOLT_ID);

        topologyBuilder.setBolt(COMPARISON_BOLT_ID, new ComparisonBolt())
                .shuffleGrouping(IMAGE_AZ_TAGS_EXTRACTOR_BOLT_ID);

        topologyBuilder.setBolt(DB_WRITER_BOLT_ID, new DbWriterBolt())
                .shuffleGrouping(COMPARISON_BOLT_ID);


        Config config = new Config();

        if (args != null && args.length > 0) {
            config.setNumWorkers(4);
            try {
                StormSubmitter.submitTopology(args[0], config, topologyBuilder.createTopology());
            } catch (AuthorizationException e) {
                e.printStackTrace();
            }
        }
        else {
            final LocalCluster cluster = new LocalCluster();
            cluster.submitTopology(TOPOLOGY_NAME, config, topologyBuilder.createTopology());
            
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run()    {
                    cluster.killTopology(TOPOLOGY_NAME);
                    cluster.shutdown();
                }
            });
        }
    }

}
