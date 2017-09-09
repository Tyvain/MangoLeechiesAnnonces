package leechies;

import java.util.HashSet;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;

import leechies.model.Annonce;

public class DBManager {
	final static Logger logger = LoggerFactory.getLogger("DBManager");	
	private static MongoClientURI uri  = new MongoClientURI("mongodb://finvalab:fv2017@ds119524.mlab.com:19524/finvalab"); 
	private static MongoClient client = new MongoClient(uri);
	private static MongoDatabase db = client.getDatabase(uri.getDatabase());
	private static MongoCollection<Document> table = db.getCollection("annonces");
	private static HashSet<String> cacheUrls = null;
	
	public static void saveAnnonce(Annonce annonce) {
		try {
			Bson filter = Filters.eq("_id", annonce.url);
			UpdateOptions options = new UpdateOptions().upsert(true);
			Bson update =  new Document("$set",annonce.toDocument());
			table.updateOne(filter, update, options);
			cacheUrls.add(annonce.url);
		} catch (Exception e) {
			logger.error("saveAnnonce: " + e);
		}
	}

	public static boolean annonceExists(String url) {
		if (cacheUrls == null) {
			cacheUrls = new HashSet<>();
			table.find().forEach((Block<Document>) document -> {
			Annonce annonce = Annonce.toAnnonce(document);
			cacheUrls.add(annonce.url);					
			});	 
		}			
		return cacheUrls.contains(url);		
	}
}
