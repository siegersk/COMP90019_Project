public class ProjectConstants {


    public static String PROJECT_PATH = "/home/sk/DCproject/StromCode/";

    // DataBase Settings
    public static String DB_SERVER_IP = "xx.xx.xx.xx";
    public static String DB_NAME = "twitterdb";
    public static String COLLECTION_NAME = "tweets";

    // Twitter Stream API Settings
    static final String CONSUMER_KEY_KEY = "...";
    static final String CONSUMER_SECRET_KEY = "...";
    static final String ACCESS_TOKEN_KEY = "...";
    static final String ACCESS_TOKEN_SECRET_KEY = "...";

    // Python Settings
    public static String PYTHON_PATH = PROJECT_PATH + "ImageAnalysis/";
    public static String PYTHON = "python3";
    public static String PYTHON_BOLT = PYTHON_PATH + "imAITagsExtractorBoltPython.py";
    public static String PYTHON_COMPARISON_PATH = PROJECT_PATH + "Comparison/";
    public static String PYTHON_COMPARISON_BOLT = PYTHON_COMPARISON_PATH + "comparisonBoltPython.py";

    // Azure Vision Settings
    public static int MAX_COUNT = 20000;
    public static String SUBSCRIPTION_KEY = "...";

}
