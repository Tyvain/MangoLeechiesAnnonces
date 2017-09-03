package leechies;

public class DBManagerOld {
//    final static Logger logger = LoggerFactory.getLogger("DBManager");
//    
//    //private static String DB_URL = "D:\\AllAdsDB";
//    private static String DB_URL = "/projects/db/AllAdsDB";
//	private static File fileDB = new File(DB_URL);
//	private static FileInputStream fis;
//	private static ObjectInputStream ois;
//	private static FileOutputStream fos;
//	private static ObjectOutputStream oos;
//	private static Map<String, Annonce> allAds;
//
//	public static void archiveDB() throws IOException {
//		File f = new File(DB_URL);
//		if(f.exists() && !f.isDirectory()) { 
//			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//			String textDate = LocalDate.now().format(formatter);
//			String fileName = DB_URL+"_BACKUP_"+ textDate;
//			fileName  = fileName.replaceAll("\\s+","");
//			Files.copy(fileDB.toPath(), Paths.get(fileName), StandardCopyOption.REPLACE_EXISTING);
//		} else {
//			resetDB ();
//		}		
//	}
//	
//    public static void resetDB () throws IOException {
//		try {
//			fos = new FileOutputStream(fileDB);
//			oos = new ObjectOutputStream(fos);
//			oos.writeObject(new HashMap<String, Annonce>());
//			oos.flush();
//			fos.close();
//			oos.close();
//		} catch (Exception e) {
//			logger.error("resetDB: " + e);
//		}
//    }
//
//	public static void saveAnnonce(Annonce annonce) {
//		// read from file
//		try {
//			Map<String, Annonce> allAds = getAllAnnoncesMap();
//			//logger.info(" Saving " + annonce.url + "DB Size: " + allAds.size() );
//			allAds.put(annonce.url, annonce);
//			fos = new FileOutputStream(fileDB);
//			oos = new ObjectOutputStream(fos);
//			oos.writeObject(allAds);
//			oos.flush();
//			fos.close();
//			oos.close();
//		} catch (Exception e) {
//			logger.error("saveAnnonce: " + e);
//		}
//	}
//
//	public static Map<String, Annonce> getAllAnnoncesMap() {
//		// read from file
//		try {
//			fis = new FileInputStream(fileDB);
//			ois = new ObjectInputStream(fis);
//			if (allAds == null) {
//				allAds = (Map<String, Annonce>) ois.readObject();
//			}
//			fis.close();
//			ois.close();
//			return allAds;
//		} catch (ClassNotFoundException | IOException e) {			
//			logger.error("getAllAnnoncesMap (Empty DB file?): " + e);
//			return new HashMap<String, Annonce>();
//		}
//
//	}
//
//	public static Stream<Annonce> getAllAnnonces() {
//			return getAllAnnoncesMap().values().stream();		
//	}
//
//    public static Annonce getAnnoncesByUrl(String url) {
//       // return getAllAnnonces().filter(f -> url.equalsIgnoreCase(f.url)).findFirst();
//    	return getAllAnnoncesMap().get(url);
//    }
//	
//    public static Stream<Annonce> getAnnoncesByCriteria(Boolean hasError, Boolean isUploaded, Boolean isCommerciale, Boolean hasImages) {
//        return getAllAnnonces()
//        .filter(f -> isCommerciale !=null? f.isCommerciale == isCommerciale:true)
//        .filter(f -> hasImages!=null? (!hasImages && (f.imgs == null || f.imgs.length == 0)) || (hasImages && f.imgs != null && f.imgs.length > 0):true)
//        .filter(f -> hasError!=null?f.hasError == hasError:true)
//        .filter(f -> isUploaded!=null? (!isUploaded && f.uploadedTime == null) || (isUploaded && f.uploadedTime != null):true);
//    }
//
//	public static boolean annonceExists(String url) {
//		boolean rez = getAllAnnoncesMap() != null ? getAllAnnoncesMap().containsKey(url) : false;
//		return rez;
//	}

}
