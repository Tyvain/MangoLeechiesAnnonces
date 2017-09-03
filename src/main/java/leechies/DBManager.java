package leechies;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

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
	private static HashMap<String, Annonce> cache = null;
	
	public static void saveAnnonce(Annonce annonce) {
		try {
			Bson filter = Filters.eq("_id", annonce.url);
			UpdateOptions options = new UpdateOptions().upsert(true);
			Bson update =  new Document("$set",annonce.toDocument());
			table.updateOne(filter, update, options);
			cache.put(annonce.url, annonce);
		} catch (Exception e) {
			logger.error("saveAnnonce: " + e);
		}
	}

	public static Map<String, Annonce> getAllAnnoncesMap() {		
		try {
			if (cache == null) {
				cache = new HashMap<>();
				table.find().forEach((Block<Document>) document -> {
				Annonce annonce = Annonce.toAnnonce(document);
				cache.put(annonce.url, annonce);
				});	 
			}			
			return cache;
		} catch (Exception e) {			
			logger.error("getAllAnnoncesMap" + e);
			return null;
		}
	}

	public static Stream<Annonce> getAllAnnonces() {
			return getAllAnnoncesMap().values().stream();		
	}

    public static Annonce getAnnoncesByUrl(String url) {
    	return getAllAnnoncesMap().get(url);
    }
	
    public static Stream<Annonce> getAnnoncesByCriteria(Boolean hasError, Boolean isUploaded, Boolean isCommerciale, Boolean hasImages) {
        return getAllAnnonces()
        .filter(f -> isCommerciale !=null? f.isCommerciale == isCommerciale:true)
        .filter(f -> hasImages!=null? (!hasImages && (f.imgs == null || f.imgs.size() == 0)) || (hasImages && f.imgs != null && f.imgs.size() > 0):true)
        .filter(f -> hasError!=null?f.hasError == hasError:true)
        .filter(f -> isUploaded!=null? (!isUploaded && f.uploadedTime == null) || (isUploaded && f.uploadedTime != null):true);
    }

	public static boolean annonceExists(String url) {
		boolean rez = getAllAnnoncesMap() != null ? getAllAnnoncesMap().containsKey(url) : false;
		return rez;
	}
}
