package leechies.sites;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import leechies.App;
import leechies.DBManager;
import leechies.model.Annonce;

public abstract class AbstractSite {
    final static Logger logger = LoggerFactory.getLogger("AbstractSite");

	protected abstract String getTitreSelector();

	protected abstract String getTexteSelector();

	protected abstract String getLinkSelector();

	protected abstract String getImageSelector();

	protected abstract String getPrixFromDoc(Document doc);
	
	protected String getVille(Document doc) {
	    return null;
	 }

	public Stream<Annonce> getAnnonces(String rootUrl, String rubUrl, String rub) {
		// liste des docs (cas des pages contenant les liens)  
		Document doc = getDocumentFromUrl(rootUrl + rubUrl);

		// liste des elements (cad liens des annonces)
		Elements elemz = doc.select(getLinkSelector());

		// listes des ids des annones mettre map pour tout
		Stream<String> idz = elemz.stream().map(e -> e.attr("href"));

		// liste des urls
		Stream<String> urlz = idz.map(s -> buildUrl(rootUrl, s));

		// liste des annonces        
		Stream<Annonce> ret = urlz.map(u -> getAnnonceFromUrl(u, rootUrl, rub));
		return ret;
	}

	protected String buildUrl(String rootUrl, String s) {
		return rootUrl + s;
	}

	protected Annonce getAnnonceFromUrl(String url, String rootUrl, String rub) {
		String log = "getAnnonceFromUrl: ";		
		
		boolean annonceExistsInDb = DBManager.annonceExists(url);
		log += " annonceExistsInDb -> " + annonceExistsInDb;
		if (annonceExistsInDb) {
			App.statNbAnnoncesAlreadyInDB++;
			return DBManager.getAnnoncesByUrl(url);
		}
		
		Document doc = getDocumentFromUrl(url);
		log += " doc == null -> " + doc == null;
		if (doc == null) {
			Annonce ret = new Annonce();
			ret.url = url;
			ret.isCommerciale = true;
			ret.hasError = true;
			ret.error = "Impossible de récupérer l'annonce à l'url spécifiée";
			return ret;
		}
		App.statNbNotAlreadyInDB++;
		//logger.info(log);
		return getAnnonce(doc, url, rootUrl, rub);
	}

	protected Annonce getAnnonce(Document doc, String url, String rootUrl, String rub) {
		Annonce ret = new Annonce();
		System.out.println("getAnnonce: " + url);
		// System.out.println("getTitreSelector(): " + getTitreSelector());
		// System.out.println("select titre: " + doc.select(getTitreSelector()).first());
		ret.titre = doc.select(getTitreSelector()).first().text();
		ret.texte = doc.select(getTexteSelector()).first().html();
		ret.category = rub;
		ret.url = url;
		ret.imgs = getImagesFromDoc(doc, rootUrl);
		ret.isCommerciale = ret.texte.contains("Annonce Commerciale");
		ret.prix = getPrixFromDoc(doc);
		ret.ville = getVille(doc);
		return ret;
	}

	protected static synchronized Document getDocumentFromUrl(String url) {
		Document doc = null;
		int i = 0;
		boolean success = false;

		while (i < 3) {
			try {
				doc = Jsoup.connect(url)
						.followRedirects(true)
						.validateTLSCertificates(false)
						.timeout(60*1000)
						.get();
				success = true;
				break;
			} catch (IOException ex) {
				logger.error("GetDocumentFromUrl - " + ex + " -> retry " + i);
			}

			i++;
		}

		if (success) {
			// Selector code ...
			return doc;
		}

		return null;
	}

	protected List<String> getImagesFromDoc(Document doc, String rootUrl) {
		Elements els = doc.select(getImageSelector());
		Stream<String> imgz = els.stream().map(e -> {
			String href = e.attr("href");
			String img = StringUtils.substringBetween(href, "big&src=", "&title");
			return img != null ? img : "";
		});
		return imgz.collect(Collectors.toList());
	}
}