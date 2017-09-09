package leechies;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.yamlbeans.YamlReader;

import leechies.model.Annonce;
import leechies.model.Source;
import leechies.sites.AbstractSite;


public class App {
    final static Logger logger = LoggerFactory.getLogger("App");
    
    public static int statNbAnnoncesUploadees = 0;    
    public static int statNbAnnoncesAlreadyInDB = 0;
    public static Duration statTotalUploadAdTime = Duration.ZERO;
    public static AtomicInteger statNbNewAnnonces = new AtomicInteger();
    public static AtomicLong avgTimeByAds = new AtomicLong();
    public static Instant start = Instant.now();
    public static Duration duration;

	public static String ALL_SOURCES[] =  { "sources-nautisme.yml", "sources-annonces.yml", "sources-immonc.yml", "sources-mode.yml", "sources-vehicules.yml" };
	//public static String ALL_SOURCES[] =  { "sources-vehicules.yml" };
	public static String SOURCES[] = ALL_SOURCES;

	// # !!!
	private static int FORCE_REMOVE_UPLOAD_ADS= 0; // remove the last x ads from website
	// # !!!
	
	private static int MAX_UPLOAD_ADS = 4000; // max ads on website	
	private static int LOG_ADS_EVERY  = 10; // log every x ads

	
	
	public static void main(String[] args) {
	try {
			/* Start of Fix */
	        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
	            public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null; }
	            public void checkClientTrusted(X509Certificate[] certs, String authType) { }
	            public void checkServerTrusted(X509Certificate[] certs, String authType) { }

	        } };

	        SSLContext sc = SSLContext.getInstance("SSL");
	        sc.init(null, trustAllCerts, new java.security.SecureRandom());
	        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

	        // Create all-trusting host name verifier
	        HostnameVerifier allHostsValid = new HostnameVerifier() {
	            public boolean verify(String hostname, SSLSession session) { return true; }
	        };
	        // Install the all-trusting host verifier
	        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
	        /* End of the fix*/
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
			if (a.hasError == false && a.isCommerciale == false && (a.imgs != null && a.imgs.size() > 0)) {
				if (a.uploadedTime == null) {
					Instant startChrono = Instant.now();
					 try {
						UploadManager.uploadAnnonceWithImage(a);
						statTotalUploadAdTime = statTotalUploadAdTime.plus(Duration.between(Instant.now(), startChrono));
						statNbAnnoncesUploadees++;
					} catch (IOException e) {
						a.hasError = true;
		                a.error = "Err upload AD: " + e.getMessage();
		                // FAUT-ilSUPPRIMER L AONNONCE? 
		                //UploadManager.deleteAnnonce(a.url);
		                logger.error("uploadAnnonceWithImage - Up Ad - " + a + "\n" + e);
					}					
				} 
			}
			DBManager.saveAnnonce(a);
			
			// on trace toutes les x annonces
			duration = Duration.between(start, Instant.now());
			avgTimeByAds.set(duration.getSeconds() / statNbNewAnnonces.incrementAndGet());
			if (statNbNewAnnonces.get() % LOG_ADS_EVERY == 0) {
				logStats("");
			}
		});
		logger.info("... goLeech finished!");
	}

    public static void logStats(String m) {		
		  String msg = m + "\nNb annonces total: " + (statNbNewAnnonces.get() + statNbAnnoncesAlreadyInDB) + "\nTemps moyen par annonce: " + avgTimeByAds + " sec";	
		  	msg += "\nTemps total: " + duration.getSeconds() / 60 + " min";
		  	msg += "\ncountstatNbNewAnnonces: " + statNbNewAnnonces;
		  	msg += "\nstatNbAnnoncesUploadees: " + statNbAnnoncesUploadees;
		  	msg += "\nstatNbAnnoncesAlreadyInDB:" + statNbAnnoncesAlreadyInDB;
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