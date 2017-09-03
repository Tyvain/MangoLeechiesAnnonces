package leechies.sites;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class ImmoncSite extends AbstractSite {


    @Override
    protected String getImageSelector() {
        return "#mycarousel > li > a";
    }

    @Override
    protected String getLinkSelector() {
        return "#filter-data > tbody > tr > td.content-post > strong > a";
    }

    @Override
    protected String getTitreSelector() {
        return "#announce_detail > tbody > tr > td > table.wht-header > tbody > tr > td > table.table2 > tbody > tr > td > table > tbody > tr > td.property-title > span:nth-child(1)";
    }

    @Override
    protected String getTexteSelector() {    	
        return "table.table2.table-res > tbody > tr:nth-child(1) > td:nth-child(2) > table > tbody > tr:nth-child(2) > td";
    }

    @Override
    protected String buildUrl(String rootUrl, String s) {
		return s;
	}
       
    @Override
     protected List<String> getImagesFromDoc(Document doc, String rootUrl) {
        Elements els = doc.select(getImageSelector());
        Stream<String> imgz = els.stream().map(e -> {            
            String img = e.attr("href");            
            String rez = rootUrl + img;
            return rez;
            });        
        return imgz.collect(Collectors.toList());
    }

    @Override
    protected String getVille(Document doc) {
        //.property-title > span:nth-child(2)
		String lieu=doc.select(".property-title > span:nth-child(2)").text();		
        System.out.println("1. lieu: " + lieu);
        
        if (lieu != null && lieu.length() > 0){
            lieu = lieu.substring(0, lieu.indexOf('>'));
        }
        System.out.println("2. lieu: " + lieu);		
		return lieu;        
    };
    
	@Override
	protected String getPrixFromDoc(Document doc) {
		//<input type="hidden" name="moneyInfo" id="moneyInfo" value="16 700 000 XPF(139 946 €)"> 
		
		
		// 16 700 000 XPF(139 946 €)
		String prix =doc.select("input#moneyInfo").first().attr("value");		
				
		Pattern regex = Pattern.compile("(.*) XPF");
		Matcher m = regex.matcher(prix);
		if (m.find()) {
			return m.group(1);
		}
		
		return "";
	}
}
