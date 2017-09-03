package leechies.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;

public class Annonce implements Serializable {
	private static final long serialVersionUID = 1L;

	public Date created=new Date();// date de création (récupération de l'annonce)
	public String url; // url de l'annonce (clef unique)
	public List<String> imgs;
	public String category;
	public String titre;
	public String texte;
	public String prix;
	public boolean isCommerciale;
	public Date uploadedTime=null; // date d'envoi sur finvalab
	public boolean hasError=false; // annonce en erreur
	public String error="";
	public String ville=null;

	public Document toDocument () {
		return new Document("_id", url)
				.append("created", created)
				.append("url", url)
				.append("imgs", imgs)
				.append("category", category)
		        .append("titre", titre)
		        .append("texte", texte)
		        .append("prix", prix)
		        .append("isCommerciale", isCommerciale)
		        .append("uploadedTime", uploadedTime)
		        .append("hasError", hasError)
		        .append("error", error)
		        .append("ville", ville);
	}
	
	public static Annonce toAnnonce (Document doc) {
		Annonce rez = new Annonce ();
		rez.url = doc.getString("url");
		rez.uploadedTime = doc.getDate("uploadedTime");
		return rez;
	}
	
	@Override
	public boolean equals(Object o) {
		Annonce a = (Annonce) o;
		return texte != null && a.texte != null && texte.equalsIgnoreCase(a.texte);
	}

	@Override
	public int hashCode() {
		return Objects.hash(texte);
	}

	@Override
	public String toString() {
		String ret = "\nurl : "+ url;
		ret += "\ncategory : "+ category;
		ret += "\ntitre : "+ titre;
		ret += "\ntexte : "+ StringUtils.substring(texte, 0, 20);
		ret += "\nprix : "+ prix;
		ret += "\nisCommerciale : "+ isCommerciale;
		ret += "\nuploadedTime : "+ uploadedTime;
		ret += "\nuhasError : "+ hasError;
		String wtf = StringUtils.isEmpty(error) ? "none":error;
		ret += "\nerror : " + wtf;
		ret += "\ndisplayImages () : "+ displayImages ();
		return ret;
	}
	
	public String displayImages () {
		String rez = "";
		for (String i : imgs) {
			rez += i + " # ";
		}
		return rez;
	}
}
