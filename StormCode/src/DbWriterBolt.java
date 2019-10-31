import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Tuple;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;
//import org.apache.storm.shade.org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Map;

import static java.util.Arrays.asList;

public class DbWriterBolt implements IRichBolt {
    MongoClient mongoClient = null;
    MongoCollection collection = null;

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        mongoClient = MongoClients.create(MongoClientSettings.builder()
                .applyToClusterSettings(builder ->
                        builder.hosts(asList(new ServerAddress(ProjectConstants.DB_SERVER_IP))))
                .build());

        MongoDatabase database = mongoClient.getDatabase(ProjectConstants.DB_NAME);
        collection = database.getCollection(ProjectConstants.COLLECTION_NAME);
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


        System.out.println("_id: " + statusID);
        System.out.println("username:" + "@"+screenName);
        System.out.println("tweeturl: " + "https://twitter.com/" + screenName + "/status/" + statusID);
        if (hashtags != null) {
            System.out.println("hashtags:" + hashtags);
        }

        for (String m : media) {
            JSONObject json = new JSONObject(m);
            System.out.println(json.toString(2));
        }


        Document doc = new Document("_id", statusID)
                .append("username", "@"+screenName)
                .append("tweeturl", "https://twitter.com/" + screenName + "/status/" + statusID);

        if (hashtags != null) {
            doc.append("hashtags", hashtags);
        }

        ArrayList<Document> mediaDocs = new ArrayList<>();
        for (String m : media) {
            try {
                JSONObject json = new JSONObject(m);

                Document mediaDoc = new Document("link", json.getString("link"));

                if (json.has("metadata")) {
                    JSONObject metadataJson = json.getJSONObject("metadata");
                    int width = metadataJson.getInt("width");
                    int height = metadataJson.getInt("height");
                    String format = metadataJson.getString("format");

                    Document metadataDoc = new Document("format", format).append("width", width).append("height", height);
                    mediaDoc.append("metadata", metadataDoc);
                }

                JSONArray aiTagsJson = json.getJSONArray("tags_ai");
                mediaDoc.append("tags_ai", jsonToDoc(aiTagsJson, true));

                if (json.has("tags_az")) {
                    JSONArray azTagsJson = json.getJSONArray("tags_az");
                    mediaDoc.append("tags_az", jsonToDoc(azTagsJson, false));
                }

                if (json.has("similarity")) {
                    mediaDoc.append("similarity", json.getDouble("similarity"));
                }

                mediaDocs.add(mediaDoc);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }


        doc.append("media", mediaDocs);
        try {
            collection.insertOne(doc);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private ArrayList<Document> jsonToDoc(JSONArray tagsJson, boolean isAiTags) {
        ArrayList<Document> tagDocs = new ArrayList<>();
        int i = 0;
        for (Object tag : tagsJson) {
            float conf = 0;
            if (isAiTags) {
                conf = ((JSONObject)tag).getFloat("confidence") / 100;
            } else {
                conf = ((JSONObject)tag).getFloat("confidence");
            }

            String tagName = ((JSONObject)tag).getString("name");

            i++;
            if (conf < 0.9 && i > 5) {
                break;
            }
            tagDocs.add(new Document("name", tagName).append("confidence", conf));
        }
        return tagDocs;
    }

    @Override
    public void cleanup() {

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {

    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }
}
