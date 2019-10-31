using System;
using System.Collections.Generic;
using MongoDB.Bson.Serialization.Attributes;

namespace DistComputingProject.Models
{
    public enum ModelType
    {
        ImageAI,
        ImageAZ
    }

    [BsonIgnoreExtraElements]
    public class TagResult
    {
        [BsonElement("_id")]
        public string Tag { get; set; }
        [BsonElement("total")]
        public int Total { get; set; }
    }

    [BsonIgnoreExtraElements]
    public class TagResultConfidence : TagResult
    {
        [BsonElement("sum_confidence")]
        public double SumConfidence { get; set; }
    }

    [BsonIgnoreExtraElements]
    public class MediaModel
    {
        [BsonElement("media")]
        public MediaObjectModel MediaObj { get; set; }
    }

    [BsonIgnoreExtraElements]
    public class MediaObjectModel
    {
        [BsonElement("link")]
        public string ImageLink { get; set; }
        [BsonElement("metadata")]
        public MetaDataObjectModel MetaData { get; set; }
        [BsonElement("tags_ai")]
        public IEnumerable<TagObjectModel> TagsAI { get; set; }
        [BsonElement("tags_az")]
        public IEnumerable<TagObjectModel> TagsAZ { get; set; }
    }

    [BsonIgnoreExtraElements]
    public class MetaDataObjectModel
    {
        [BsonElement("format")]
        public string Format { get; set; }
        [BsonElement("width")]
        public int Width { get; set; }
        [BsonElement("height")]
        public int Height { get; set; }
    }

    [BsonIgnoreExtraElements]
    public class TagObjectModel
    {
        [BsonElement("name")]
        public string Name { get; set; }
        [BsonElement("confidence")]
        public double Confidence { get; set; }
    }

    [BsonIgnoreExtraElements]
    public class TagNumber
    {
        [BsonElement("total")]
        public int Number { get; set; }
    }
}
