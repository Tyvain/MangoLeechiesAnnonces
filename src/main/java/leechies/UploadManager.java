package leechies;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import leechies.model.Annonce;
import leechies.model.Category;
import leechies.model.Location;

public class UploadManager {
    final static Logger logger = LoggerFactory.getLogger("UploadManager");

    private static String UT      = "39924a52f759ee5de2b10285f8daaadf12a59d4d";
    private static String IDU     = "1"; // id user
    private static String URL_ADS = "http://finvalab.com/api/v1/ads";
    private static String URL_ADS_IMAGE = "http://finvalab.com/api/v1/ads/image/";

    private static Connection getConnectionAdService(String url) {
        return Jsoup.connect(url)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .header("User-Agent", "Mozilla/5.0")
                    .ignoreContentType(true);
    }

    public static int countAnnonces () {
           return getLastAnnonces(Integer.MAX_VALUE).length();
    }

    private static HttpURLConnection getFVConnection (int nbLastAnnonces) {
            try {
            URL myUrl = new URL(URL_ADS+"?user_token=39924a52f759ee5de2b10285f8daaadf12a59d4d&items_per_page="+nbLastAnnonces+"&sort=created&status!=50");
            //System.out.println("Calling : " + myUrl);
            HttpURLConnection myURLConnection = (HttpURLConnection)myUrl.openConnection();      
            myURLConnection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            myURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0");
            myURLConnection.setRequestMethod("GET");
            return myURLConnection;
            } catch (IOException e) {
                logger.error("GetFVConnection - " + e);
            }
            return null;
         }

        public static void removeLastAnnonces (int nbLastAnnonces) {             
           try {
            getLastAnnonces (nbLastAnnonces).forEach(item -> {
                JSONObject obj = (JSONObject) item;
                deleteAnnonce(""+obj.get("id_ad"));
            });
            }
            catch (JSONException e) {
             System.err.println("Erreur removeLastAnnonces : " + e);
            }
        }

      public static JSONArray getLastAnnonces(int nbLastAnnonces) {
         try {           
            HttpURLConnection myURLConnection = getFVConnection(nbLastAnnonces); 
            myURLConnection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            myURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0");
            myURLConnection.setRequestMethod("GET");           
            BufferedReader rd = new BufferedReader(new InputStreamReader(myURLConnection.getInputStream()));
            String jsonText = readAll(rd);
            JSONObject json = new JSONObject(jsonText);
           myURLConnection.getInputStream().close(); 
           return json.getJSONArray("ads");
        } catch (IOException | JSONException e) {
            logger.error("GetLastAnnonces - " + e);
          }
          return null;
      }
			
    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
          sb.append((char) cp);
        }
        return sb.toString();
    }

    public static boolean uploadAnnonceWithImage(Annonce annonce) {
            AnnonceCleaner.cleanAnnonce(annonce);
        
            String idAd=null;
            annonce.hasError = false;
            annonce.error = "";
            annonce.uploadedTime = null;
             try {
                idAd = uploadAnnonce(annonce); 
            } catch (IOException e) {
                annonce.hasError = true;
                annonce.error = "Err upload AD: " + e.getMessage();
                deleteAnnonce(idAd);
                DBManager.saveAnnonce(annonce);
                logger.error("uploadAnnonceWithImage - Up Ad - " + annonce + "\n" + e);
                return false;
            }
      
            int imageSucceedUpload=0;            
                for (String img : annonce.imgs) {
                   try {
                    uploadImage(img, idAd);
                    imageSucceedUpload++;
                      } catch (IOException e) {
                        annonce.hasError = true;
                        annonce.error += "Err image: " + img + " - " + e;
                        }                  
                }
      
            
            // on a réussit à uploader au moins une image
            if (imageSucceedUpload > 0) {            
                annonce.uploadedTime = new Date();                
            } else { // aucune image -> on delete                 
                    deleteAnnonce(idAd);
                    return false;
            }
            DBManager.saveAnnonce(annonce);  
            return true;
        }

    public static String uploadAnnonce(Annonce annonce) throws IOException {
        int cat = Category.getCategoryFromLibelle(annonce.category).id;
        String location =  Location.getIdByLocation(annonce.ville);
        Document doc = getConnectionAdService(URL_ADS)
                .data("id_user", IDU)
                .data("id_category", ""+cat)
                .data("title", annonce.titre)
                .data("description", annonce.texte) 
                .data("user_token", UT)
                .data("website", annonce.url)
                .data("price", annonce.prix)
                .data("id_location",location!=null?location:"")
                .timeout(60000)
                .post();

        String myJSONString = doc.text();
        JsonObject jobj = new Gson().fromJson(myJSONString, JsonObject.class);
        return "" + jobj.get("ad").getAsJsonObject().get("id_ad");
    }
    
    public static void uploadImage(String u, String adId) throws IOException {
        // create file
        URL url = null;
		try {
			url = new URL(u.replaceAll(" ", "%20"));
		} catch (MalformedURLException e1) {
			logger.error("UploadImage - URL" + "\nu: " + u + "\nadId:" + adId + "\nurl: " + url  + "\nerr:" + e1);
			throw e1;
		}
        File file = new File("./temp.jpg");
        try {
			FileUtils.copyURLToFile(url, file);
		} catch (IOException e) {
			logger.error("UploadImage - copyURLToFile - " + u + " - " + adId + "\n" + e);
			throw e;
		}        
        
        // upload image
        try {
			getConnectionAdService(URL_ADS_IMAGE+adId) 
			.data("image", file.getName(), new FileInputStream(file))
			.data("user_token", UT)
			.timeout(60000)
			.post();
		} catch (IOException e) {
			logger.error("UploadImage - getConnectionAdService - " + u + " - " + adId + "\n" + e);
			throw e;
	    }
    }
    
    
	public static void deleteAnnonce(String idAd) {
		if (!StringUtils.isEmpty(idAd)) {
			try {
				getConnectionAdService(URL_ADS + "/delete/" + idAd).data("user_token", UT).post();
			} catch (IOException e) {
				logger.error("DeleteAnnonce - " + e);
			}
		}
	}
}
