using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Logging;
using DistComputingProject.Models;
using MongoDB.Driver;
using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;
using MongoDB.Bson.Serialization;


namespace DistComputingProject.Controllers
{
    public class HomeController : Controller
    {
        private readonly ILogger<HomeController> _logger;

        private MongoClient client;
        private IMongoDatabase db;

        private string FirstDoc { get; set; }

        public HomeController(ILogger<HomeController> logger)
        {
            _logger = logger;

            client = new MongoClient("mongodb://1.1.1.1:27017"); // change Server IP
            db = client.GetDatabase("twitterdb");
        }

        public IActionResult Index()
        {
            return View();
        }

        /// <summary>
        /// Returns 2 tables: Top 20 Hashtags and top 20 ImageAI tags over full collection
        /// </summary>
        /// <returns></returns>
        public IActionResult Top20ImageAiAndHashTags()
        {
            var collection = db.GetCollection<BsonDocument>("tweets");
            var document = collection.Find(new BsonDocument()).FirstOrDefault();
            //Console.WriteLine(document.ToString());
            FirstDoc = document.ToString();

            var hashtagsResults = collection.Aggregate().Project("{ _id:0, hashtags: 1 }")
                .Unwind("hashtags")
                .Group(new BsonDocument { { "_id", "$hashtags" }, { "total", new BsonDocument("$sum", 1) } })
                .Sort("{total: -1}")
                .Project("{_id:1}")
                .Limit(20)
                .ToList();

            var imageTagsResults = collection.Aggregate().Project("{ _id:0, media: 1 }")
                .Unwind("media")
                .Unwind("media.tags_ai")
                .Group(new BsonDocument { { "_id", "$media.tags_ai.name" }, { "total", new BsonDocument("$sum", 1) } })
                .Sort("{total: -1}")
                .Project("{_id:1}")
                .Limit(20)
                .ToList();

            List<TagResult> hashtags = new List<TagResult>();
            foreach (var r in hashtagsResults)
            {
                var ht = BsonSerializer.Deserialize<TagResult>(r);
                hashtags.Add(ht);
            }

            List<TagResult> imageTags = new List<TagResult>();
            foreach (var r in imageTagsResults)
            {
                var it = BsonSerializer.Deserialize<TagResult>(r);
                imageTags.Add(it);
            }

            ViewBag.Doc = FirstDoc;
            return View(new Tuple<IEnumerable<TagResult>, IEnumerable<TagResult>>(hashtags, imageTags));
        }

        /// <summary>
        /// Returns 2 tables: Top 20 ImageAI and Azure Vision Tags
        /// </summary>
        /// <returns></returns>
        public IActionResult Top20ImageTags()
        {
            var collection = db.GetCollection<BsonDocument>("tweets");

            var imageAiTagsResults = collection.Aggregate().Match(new BsonDocument("media.tags_az", new BsonDocument("$exists", "true")))
                //.Match("{ \"media.tags_az\": { $exists: true }}")
                .Project("{ _id:0, media: 1 }")
                .Unwind("media")
                .Unwind("media.tags_ai")
                .Group(new BsonDocument { { "_id", "$media.tags_ai.name" }, { "total", new BsonDocument("$sum", 1) } })
                .Sort("{total: -1}")
                //.Project("{_id:1}")
                .Limit(20)
                .ToList();

            var imageAzTagsResults = collection.Aggregate().Match("{ 'media.tags_az': { $exists: true }}")
                .Project("{ _id:0, media: 1 }")
                .Unwind("media")
                .Unwind("media.tags_az")
                .Group(new BsonDocument { { "_id", "$media.tags_az.name" }, { "total", new BsonDocument("$sum", 1) } })
                .Sort("{total: -1}")
                //.Project("{_id:1}")
                .Limit(20)
                .ToList();

            List<TagResult> imageAiTags = new List<TagResult>();
            foreach (var r in imageAiTagsResults)
            {
                var t = BsonSerializer.Deserialize<TagResult>(r);
                imageAiTags.Add(t);
            }

            List<TagResult> imageAzTags = new List<TagResult>();
            foreach (var r in imageAzTagsResults)
            {
                var it = BsonSerializer.Deserialize<TagResult>(r);
                imageAzTags.Add(it);
            }

            return View(new Tuple<IEnumerable<TagResult>, IEnumerable<TagResult>>(imageAzTags, imageAiTags));
        }

        public IActionResult ImageAZTagCloud()
        {
            var collection = db.GetCollection<BsonDocument>("tweets");

            var imageAzTagsResults = collection.Aggregate().Match("{ 'media.tags_az': { $exists: true }}")
                .Project("{ _id:0, media: 1 }")
                .Unwind("media")
                .Unwind("media.tags_az")
                .Group(new BsonDocument { { "_id", "$media.tags_az.name" }, { "total", new BsonDocument("$sum", 1) } })
                .ToList();

            List<TagResult> imageAzTags = new List<TagResult>();
            foreach (var r in imageAzTagsResults)
            {
                var it = BsonSerializer.Deserialize<TagResult>(r);
                imageAzTags.Add(it);
            }

            return View(imageAzTags);
        }

        /// <summary>
        /// Returns 2 tables: Top 20 ImageAI and Azure Vision Tags sorted by sum of confidence
        /// </summary>
        /// <returns></returns>
        public IActionResult Top20ImageTagsConfidence()
        {
            var collection = db.GetCollection<BsonDocument>("tweets");

            var imageAiTagsResults = collection.Aggregate().Match("{ 'media.tags_az': { $exists: true }}")
                .Project("{ _id:0, media: 1 }")
                .Unwind("media")
                .Unwind("media.tags_ai")
                .Group(new BsonDocument { { "_id", "$media.tags_ai.name" }, { "sum_confidence", new BsonDocument("$sum", "$media.tags_ai.confidence") }, { "total", new BsonDocument("$sum", 1) } })
                .Sort("{sum_confidence: -1}")
                .Limit(20)
                .ToList();

            var imageAzTagsResults = collection.Aggregate().Match("{ 'media.tags_az': { $exists: true }}")
                .Project("{ _id:0, media: 1 }")
                .Unwind("media")
                .Unwind("media.tags_az")
                .Group(new BsonDocument { { "_id", "$media.tags_az.name" }, { "sum_confidence", new BsonDocument("$sum", "$media.tags_az.confidence") }, { "total", new BsonDocument("$sum", 1) } })
                .Sort("{total: -1}")
                .Limit(20)
                .ToList();

            List<TagResultConfidence> imageAiTags = new List<TagResultConfidence>();
            foreach (var r in imageAiTagsResults)
            {
                var t = BsonSerializer.Deserialize<TagResultConfidence>(r);
                imageAiTags.Add(t);
            }

            List<TagResultConfidence> imageAzTags = new List<TagResultConfidence>();
            foreach (var r in imageAzTagsResults)
            {
                var it = BsonSerializer.Deserialize<TagResultConfidence>(r);
                imageAzTags.Add(it);
            }

            return View(new Tuple<IEnumerable<TagResultConfidence>, IEnumerable<TagResultConfidence>>(imageAzTags, imageAiTags));
        }

        /// <summary>
        /// Returns Tag Info: Top 20 tags from opposite model accured with this tag, List of images with this tag
        /// </summary>
        /// <param name="id"></param>
        /// <returns></returns>
        public IActionResult AzTagDetails(string id)
        {
            var collection = db.GetCollection<BsonDocument>("tweets");

            var imageAiTags = GetTop10TagsFromOppositeModel(collection, id, ModelType.ImageAZ);

            // List of Images (Media Objects)
            var mediaObjects = GetMediaModels(collection, id, ModelType.ImageAZ);

            ViewBag.Tag = id;
            ViewBag.ModelType = ModelType.ImageAZ;
            return View("TagDetails", new Tuple<IEnumerable<TagResult>, IEnumerable<MediaModel>>(imageAiTags, mediaObjects));
        }

        public IActionResult AiTagDetails(string id)
        {
            var collection = db.GetCollection<BsonDocument>("tweets");

            var imageAzTags = GetTop10TagsFromOppositeModel(collection, id, ModelType.ImageAI);

            var mediaObjects = GetMediaModels(collection, id, ModelType.ImageAI);

            ViewBag.Tag = id;
            ViewBag.ModelType = ModelType.ImageAI;
            return View("TagDetails", new Tuple<IEnumerable<TagResult>, IEnumerable<MediaModel>>(imageAzTags, mediaObjects));
        }

        

        private List<TagResult> GetTop10TagsFromOppositeModel(IMongoCollection<BsonDocument> collection, string tagName, ModelType type)
        {
            string modelOfGivenTag = type == ModelType.ImageAI ? "media.tags_ai.name" : "media.tags_az.name";
            string oppositeModel = type == ModelType.ImageAI ? "media.tags_az" : "media.tags_ai";

            var imageTagsResults = collection.Aggregate()
                .Project("{ _id:0, media: 1 }")
                .Unwind("media")
                .Match("{ 'media.tags_az': { $exists: true }}")
                .Match(new BsonDocument(modelOfGivenTag, new BsonDocument("$eq", tagName)))
                .Unwind(oppositeModel)
                .Group(new BsonDocument { { "_id", "$" + oppositeModel + ".name" }, { "total", new BsonDocument("$sum", 1) } })
                .Sort("{total: -1}")
                .Limit(10)
                .ToList();

            List<TagResult> imageTags = new List<TagResult>();
            foreach (var r in imageTagsResults)
            {
                var t = BsonSerializer.Deserialize<TagResult>(r);
                imageTags.Add(t);
            }

            return imageTags;
        }

        private List<MediaModel> GetMediaModels(IMongoCollection<BsonDocument> collection, string tagName, ModelType type)
        {
            string tagsToLook = type == ModelType.ImageAI ? "media.tags_ai.name" : "media.tags_az.name";
            var mediaObjectsResults = collection.Aggregate()
                .Project("{ _id:0, media: 1 }")
                .Unwind("media")
                .Match("{ 'media.tags_az': { $exists: true }}")
                .Match(new BsonDocument(tagsToLook, new BsonDocument("$eq", tagName)))
                .Limit(200)
                .ToList();

            List<MediaModel> mediaObjects = new List<MediaModel>();
            foreach (var r in mediaObjectsResults)
            {
                var t = BsonSerializer.Deserialize<MediaModel>(r);
                mediaObjects.Add(t);
            }

            return mediaObjects;
        }


        public IActionResult ConfidenceCharts()
        {
            var collection = db.GetCollection<BsonDocument>("tweets");

            int[] azData = new int[5];
            azData[0] = GetTagNumberWithConfidence(collection, Sign.Less, 0.6, ModelType.ImageAZ);
            azData[1] = GetTagNumberWithConfidenceInRange(collection, 0.6, 0.7, ModelType.ImageAZ);
            azData[2] = GetTagNumberWithConfidenceInRange(collection, 0.7, 0.8, ModelType.ImageAZ);
            azData[3] = GetTagNumberWithConfidenceInRange(collection, 0.8, 0.9, ModelType.ImageAZ);
            azData[4] = GetTagNumberWithConfidence(collection, Sign.Greater, 0.9, ModelType.ImageAZ);


            int[] aiData = new int[5];
            aiData[0] = GetTagNumberWithConfidence(collection, Sign.Less, 0.6, ModelType.ImageAI);
            aiData[1] = GetTagNumberWithConfidenceInRange(collection, 0.6, 0.7, ModelType.ImageAI);
            aiData[2] = GetTagNumberWithConfidenceInRange(collection, 0.7, 0.8, ModelType.ImageAI);
            aiData[3] = GetTagNumberWithConfidenceInRange(collection, 0.8, 0.9, ModelType.ImageAI);
            aiData[4] = GetTagNumberWithConfidence(collection, Sign.Greater, 0.9, ModelType.ImageAI);

            return View(new Tuple<int[], int[]>(azData, aiData));
        }

        enum Sign
        {
            Equal,
            Greater,
            Less
        }

        private int GetTagNumberWithConfidence(IMongoCollection<BsonDocument> collection, Sign sign, double confidenceThreshold, ModelType type)
        {
            string tagsToLook = type == ModelType.ImageAI ? "media.tags_ai" : "media.tags_az";

            string signStr = null;
            switch (sign)
            {
                case Sign.Greater:
                    signStr = "$gt";
                    break;
                case Sign.Less:
                    signStr = "$lt";
                    break;
                default:
                    signStr = "$eq";
                    break;
            }

            var tagNumber = collection.Aggregate()
                .Project("{ _id:0, media: 1 }")
                .Unwind("media")
                .Match("{ 'media.tags_az': { $exists: true }}")
                .Unwind(tagsToLook)
                .Match(new BsonDocument(tagsToLook + ".confidence", new BsonDocument(signStr, confidenceThreshold)))
                .Group(new BsonDocument { { "_id", "null" }, { "total", new BsonDocument("$sum", 1) } })
                .ToList();

            
            if (tagNumber.Count > 1)
            {
                throw new Exception("Should be only one result");
            }
            var result = BsonSerializer.Deserialize<TagResult>(tagNumber.First());
    
            return result.Total;
        }

        private int GetTagNumberWithConfidenceInRange(IMongoCollection<BsonDocument> collection, double confidenceBottom, double confidenceTop, ModelType type)
        {
            string tagsToLook = type == ModelType.ImageAI ? "media.tags_ai" : "media.tags_az";

            var tagNumber = collection.Aggregate()
                .Project("{ _id:0, media: 1 }")
                .Unwind("media")
                .Match("{ 'media.tags_az': { $exists: true }}")
                .Unwind(tagsToLook)
                .Match(new BsonDocument(tagsToLook + ".confidence", new BsonDocument { { "$gt", confidenceBottom }, { "$lt", confidenceTop } } ))
                .Group(new BsonDocument { { "_id", "null" }, { "total", new BsonDocument("$sum", 1) } })
                .ToList();


            if (tagNumber.Count > 1)
            {
                throw new Exception("Should be only one result");
            }
            var result = BsonSerializer.Deserialize<TagResult>(tagNumber.First());

            return result.Total;
        }


        public IActionResult ImageAiTagCloud()
        {
            var collection = db.GetCollection<BsonDocument>("tweets");

            var imageAiTagsResults = collection.Aggregate()
                .Project("{ _id:0, media: 1 }")
                .Unwind("media")
                .Unwind("media.tags_ai")
                .Group(new BsonDocument { { "_id", "$media.tags_ai.name" }, { "total", new BsonDocument("$sum", 1) } })
                //.Sort("{total: -1}")
                .ToList();

            List<TagResult> imageAiTags = new List<TagResult>();
            foreach (var r in imageAiTagsResults)
            {
                var t = BsonSerializer.Deserialize<TagResult>(r);
                imageAiTags.Add(t);
            }

            return View(imageAiTags);
        }

        public IActionResult Privacy()
        {
            
            return View();
            

        }

        [ResponseCache(Duration = 0, Location = ResponseCacheLocation.None, NoStore = true)]
        public IActionResult Error()
        {
            return View(new ErrorViewModel { RequestId = Activity.Current?.Id ?? HttpContext.TraceIdentifier });
        }
    }
}
