import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.ShellBolt;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;

import java.util.Map;

public class ComparisonBolt extends ShellBolt implements IRichBolt{
    public ComparisonBolt() {
        super(ProjectConstants.PYTHON, ProjectConstants.PYTHON_COMPARISON_BOLT);
    }


    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("screenName",
                "statusID",
                "text",
                "hashtags",
                "media"));
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }
}
