import storm

import os
import numpy as np
from scipy import spatial

class comparisonBoltPython(storm.BasicBolt):
    def initialize(self, conf, context):
        self._conf = conf
        self._context = context

        glove_file = "glove/glove.6B.50d.txt"
        self.model = self.load_glove_model(glove_file)


    def process(self, tuple):
        screenName = tuple.values[0]
        statusID = tuple.values[1]
        text = tuple.values[2]
        hashtags = tuple.values[3]
        media = tuple.values[4]
        
        updated_media = []
        for i, m in enumerate(media):
            current_media = eval(m)
            tags_ai = self.transform_tags(current_media, 'tags_ai')
            tags_az = self.transform_tags(current_media, 'tags_az')
            if tags_ai is not None and tags_az is not None:
                sim = self.cosine_distance_wordembedding_method(tags_ai, tags_az)
                current_media['similarity'] = sim
                updated_media.append(str(current_media))    
        
        storm.emit([str(screenName), statusID, str(text), hashtags, updated_media])


    def load_glove_model(self, model_file):
        script_dir = os.path.dirname(__file__)
        with open(os.path.join(script_dir, model_file), encoding="utf8") as f:
            content = f.readlines()
        model = {}
        for line in content:
            split_line = line.split()
            word = split_line[0]
            embedding = np.array([float(val) for val in split_line[1:]])
            model[word] = embedding
        return model

    def preprocess(self, tags):
        result_words = set()
        for tag in tags:
            words = tag.lower().replace("-", " ").replace("_", " ").split()
            result_words.update(words)
        return result_words

    def cosine_distance_wordembedding_method(self, s1, s2):
        vector_1 = np.mean([self.model[word] for word in self.preprocess(s1) if word in self.model], axis=0)
        vector_2 = np.mean([self.model[word] for word in self.preprocess(s2) if word in self.model], axis=0)
        cosine = spatial.distance.cosine(vector_1, vector_2)
        similarity = (1-cosine) * 100
        return similarity

    def transform_tags(self, media_obj, element_name):
        transformed_tags = []
        if element_name in media_obj:
            tags = media_obj[element_name]
            for tag in tags:
                transformed_tags.append(tag['name'])
        if len(transformed_tags) == 0:
            return None
        else:
            return transformed_tags


comparisonBoltPython().run()
