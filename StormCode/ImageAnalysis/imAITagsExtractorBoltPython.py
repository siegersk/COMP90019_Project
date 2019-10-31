import storm
from imageai.Prediction import ImagePrediction
import os
import urllib.request as r

class ImAITagExtractorBoltPython(storm.BasicBolt):
    def initialize(self, conf, context):
        self._conf = conf
        self._context = context

        self.path = os.path.dirname(__file__)

        self.image_predictions = ImagePrediction()
        self.image_predictions.setModelTypeAsResNet()
        self.image_predictions.setModelPath(os.path.join(self.path, "resnet50_weights_tf_dim_ordering_tf_kernels.h5"))
        self.image_predictions.loadModel()


    def process(self, tuple):
        screenName = tuple.values[0]
        statusID = tuple.values[1]
        text = tuple.values[2]
        hashtags = tuple.values[3]
        media_urls = tuple.values[4]

        image_path = self.path + "/image.jpeg"
        media = []
        for url in media_urls:
            r.urlretrieve(url, image_path)

            results_array = self.image_predictions.predictImage(image_path, result_count=10)
        
            tags = self.consolidate_prediction_results(results_array)
            res_str = {"link" : url, "tags_ai" : tags}
            media.append(str(res_str))
        
        storm.emit([str(screenName), statusID, str(text), hashtags, media])

    def consolidate_prediction_results(self, results):
        predictions, percentage_probabilities = results

        list_of_tags = []
        for i, prediction in enumerate(predictions):
            tag = {"name" : prediction, "confidence" : percentage_probabilities[i]}
            list_of_tags.append(tag)
        return list_of_tags


ImAITagExtractorBoltPython().run()
