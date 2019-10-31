import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.json.JSONObject;
//import org.apache.storm.shade.org.json.simple.JSONObject;

import java.net.URI;
import java.util.ArrayList;
import java.util.Map;



public class AzTagsExtractorBolt implements IRichBolt {
    private static final long serialVersionUID = 1L;
    private OutputCollector collector;

    static String endpoint = "https://dc1compvision.cognitiveservices.azure.com/";

    int counter = 0;

    private static final String uriBase = endpoint + "vision/v2.0/tag";

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.collector = outputCollector;
    }

    @Override
    public void execute(Tuple tuple) {
        String screenName = "";
        Long statusID = null;
        String text = "";
        ArrayList<String> hashtags = null;
        ArrayList<String> media = null;

        screenName = (String) tuple.getValueByField("screenName");
        statusID = (Long) tuple.getValueByField("statusID");
        text = (String) tuple.getValueByField("text");
        hashtags = (ArrayList<String>) tuple.getValueByField("hashtags");
        media = (ArrayList<String>) tuple.getValueByField("media");


        ArrayList<String> updatedMedia = new ArrayList<>();

        for (String m : media) {
            JSONObject json = new JSONObject(m);
            String url = json.get("link").toString();

            if (counter < ProjectConstants.MAX_COUNT) {
                JSONObject azResult = extractImageTags(url);
                if (azResult != null) {
                    if (azResult.has("metadata")) {
                        json.put("metadata", azResult.get("metadata"));
                    }
                    if (azResult.has("tags")) {
                        json.put("tags_az", azResult.get("tags"));
                    }
                }
            }
            else {
                System.out.println("Maximum number of Azure Vision requests exceeded");
            }

            updatedMedia.add(json.toString());
        }

        collector.emit(new Values(screenName,
                statusID,
                text,
                hashtags,
                updatedMedia));
    }

    public JSONObject extractImageTags(String imageToAnalyze) {
        counter ++;
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();

        try {
            int a = 1;
            URIBuilder builder = new URIBuilder(uriBase);

            // Request parameters. All of them are optional.
            builder.setParameter("visualFeatures", "Categories,Description,Color");
            builder.setParameter("language", "en");

            // Prepare the URI for the REST API method.
            URI uri = builder.build();
            HttpPost request = new HttpPost(uri);

            // Request headers.
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Ocp-Apim-Subscription-Key", ProjectConstants.SUBSCRIPTION_KEY);


            // Request body.
            StringEntity requestEntity =
                    new StringEntity("{\"url\":\"" + imageToAnalyze + "\"}");
            request.setEntity(requestEntity);

            // Call the REST API method and get the response entity.
            HttpResponse response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                String jsonString = EntityUtils.toString(entity);
                JSONObject json = new JSONObject(jsonString);
                return json;

            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
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
                "media"));
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }

}

