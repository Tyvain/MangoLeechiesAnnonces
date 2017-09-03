package leechies;

import java.io.File;
import java.io.FileReader;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.yamlbeans.YamlReader;

import leechies.model.Annonce;
import leechies.model.Source;
import leechies.sites.AbstractSite;


public class App {
    final static Logger logger = LoggerFactory.getLogger("App");
    
    public static int statNbAnnoncesTraitees = 0;
    public static int statNbAnnoncesUploadees = 0;    
    public static int statNbAnnoncesAlreadyInDB = 0;
    public static int statNbAnnoncesAlreadyUploaded = 0;
    public static int statNbNotAlreadyInDB = 0;
    public static Duration statTotalUploadAdTime = Duration.ZERO;
    public static AtomicInteger countAnnoncesTraitees = new AtomicInteger();
    public static AtomicLong avgTimeByAds = new AtomicLong();
    public static Instant start = Instant.now();
    public static Duration duration;

	public static String ALL_SOURCES[] =  { "sources-immonc.yml", "sources-annonces.yml", "sources-nautisme.yml", "sources-mode.yml", "sources-vehicules.yml" };
	//public static String ALL_SOURCES[] =  { "sources-vehicules.yml" };
	public static String SOURCES[] = ALL_SOURCES;

	// # !!!
	private static int FORCE_REMOVE_UPLOAD_ADS= 0; // remove the last x ads from website
	// # !!!
	
	private static int MAX_UPLOAD_ADS = 4000; // max ads on website	
	private static int LOG_ADS_EVERY  = 10; // log every x ads

	public static void main(String[] args) {
		
	 try {
	    int totalUpAnnonces = UploadManager.countAnnonces();
	    int diff = totalUpAnnonces - MAX_UPLOAD_ADS;
	   
	   if (diff > 0) {	       
	       UploadManager.removeLastAnnonces(diff);
	       }
	   
	   if (FORCE_REMOVE_UPLOAD_ADS > 0) {
		   UploadManager.removeLastAnnonces(FORCE_REMOVE_UPLOAD_ADS);
	   }
	   
	   String initTrace = "\n### DEBUT ### " ;
	   initTrace += "\nSOURCES: " + SOURCES.length;
	   initTrace += "\nFORCE_REMOVE_UPLOAD_ADS: " + FORCE_REMOVE_UPLOAD_ADS;
	   initTrace += "\nMAX_UPLOAD_ADS: " + MAX_UPLOAD_ADS;	    
	   initTrace += "\n### INFOS ### " ;	    
	   initTrace += "\n-- Total Ads online: " + totalUpAnnonces;	   	    
	   initTrace += "\n-- LOCAL DB";
       initTrace += "\n  - avec images: " + DBManager.getAnnoncesByCriteria(null, null, null, true).count();
       initTrace += "\n  - sans images: " + DBManager.getAnnoncesByCriteria(null, null, null, false).count();
       initTrace += "\n  - uploaded: " + DBManager.getAnnoncesByCriteria(null, true, null, null).count();
       initTrace += "\n  - non uploaded: " + DBManager.getAnnoncesByCriteria(null, false, null, null).count();
       initTrace += "\n  - commerciales: " + DBManager.getAnnoncesByCriteria(null, null, true, null).count();
       initTrace += "\n  - non commerciales: " + DBManager.getAnnoncesByCriteria(null, null, false, null).count();
       initTrace += "\n  - avec erreurs: " + DBManager.getAnnoncesByCriteria(true, null, null, null).count();
       initTrace += "\n  - sans erreurs: " + DBManager.getAnnoncesByCriteria(false, null, null, null).count();    
	    
	    logger.info(initTrace);
	   
	   goLeech();

	   logStats("\n### FIN ###");
		 } catch (Exception e) {
			 logger.error("\n### MAIN ERROR ### ", e);
		}
	}
	
	private static void goLeech() {
		logger.info("Starting goLeech...");
		App.getSourceStream().flatMap(s -> {
			return getAnnonceFromSource(s);
		}).forEach(a -> {
			DBManager.saveAnnonce(a);
			if (a.hasError == false && a.isCommerciale == false && (a.imgs != null && a.imgs.size() > 0)) {
				if (a.uploadedTime == null) {
					Instant startChrono = Instant.now();
					boolean isUploadSuccess = UploadManager.uploadAnnonceWithImage(a);
					statTotalUploadAdTime = statTotalUploadAdTime.plus(Duration.between(Instant.now(), startChrono));
					if (isUploadSuccess) {
						statNbAnnoncesUploadees++;
						System.exit(0);
					}
				} else {
					statNbAnnoncesAlreadyUploaded++;
				}
				statNbAnnoncesTraitees++;
			}

			// on trace toutes les x annonces
			duration = Duration.between(start, Instant.now());
			avgTimeByAds.set(duration.getSeconds() / countAnnoncesTraitees.incrementAndGet());
			if (countAnnoncesTraitees.get() % LOG_ADS_EVERY == 0) {
				logStats("");
			}
		});
		logger.info("... goLeech finished!");
	}

    public static void logStats(String m) {		
		  String msg = m + "\nNb annonces traitées: " + countAnnoncesTraitees.get() + "\nTemps moyen par annonce: " + avgTimeByAds + " sec";	
		  	msg += "\nTemps total: " + duration.getSeconds() / 60 + " min";
		  	msg += "\nstatNbAnnoncesTraitees: " + statNbAnnoncesTraitees;
		  	msg += "\nstatNbAnnoncesUploadees: " + statNbAnnoncesUploadees;
		  	msg += "\nstatNbAnnoncesAlreadyUploaded: " + statNbAnnoncesAlreadyUploaded;
		  	msg += "\nstatNbAnnoncesAlreadyInDB:" + statNbAnnoncesAlreadyInDB;
		  	msg += "\nstatNbNotAlreadyInDB:" + statNbNotAlreadyInDB;
		  	msg += "\nstatTotalUploadAdTime:" + statTotalUploadAdTime;
		  	if (statNbAnnoncesUploadees != 0) {
		  		msg += "\navg time upload 1 ad:" + statTotalUploadAdTime.dividedBy(statNbAnnoncesUploadees);
		  	}
		  	 logger.info(msg);
    }

	private static Stream<Annonce> getAnnonceFromSource(Source source) {
		return source.rubriques.stream()
				.flatMap(r -> r.subUrls.stream().flatMap(u -> App.getAnnonce(source, u, r.category.libelle)));
	}

	private static Stream<Annonce> getAnnonce(Source source, String url, String rub) {
		Class<?> clazz;
		try {
			clazz = Class.forName(source.className);
			AbstractSite site = (AbstractSite) clazz.newInstance();
			return site.getAnnonces(source.rootUrl, url, rub);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			logger.error("App.getAnnonce - " + e);
		}
		return null;
	}

	private static Stream<Source> getSourceStream() {
		Stream<Source> ret = Stream.empty();
		try {
			for (String source : SOURCES) {
			    
			    ClassLoader classLoader = App.class.getClassLoader();
			    File file = new File(classLoader.getResource(source).getFile());
			    logger.info("file : " + file);
			    YamlReader reader = new YamlReader(new FileReader(file));
			    
				//YamlReader reader = new YamlReader(new FileReader(source));
				@SuppressWarnings("unchecked")
				ArrayList<Source> wtf = (ArrayList<Source>) reader.read();
				ret = Stream.concat(ret, wtf.stream());
			}
		} catch (Exception e) {
			logger.error("App.getSourceStream - " + e);
		}
		return ret;
	}
}