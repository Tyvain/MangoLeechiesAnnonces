package leechies.sites;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class GenericWamSite extends AbstractSite {

	@Override
	protected String getImageSelector() {
		return "#gallery > ul > li > a";
	}

	@Override
	protected String getLinkSelector() {
		return "#div_centre_0 > span > table";
	}

	@Override
	protected String getTitreSelector() {
		return "#div_centre_0 > table > tbody > tr > td:nth-child(2) > table > tbody > tr:nth-child(1) > td > table > tbody > tr > td:nth-child(3) > b";
	}

	@Override
	protected String getTexteSelector() {
		return "#div_centre_0 > table > tbody > tr > td:nth-child(2) > table > tbody > tr:nth-child(3) > td > table > tbody > tr > td.andmm > div";
	}

	@Override
	protected String getPrixFromDoc(Document doc) {
		Pattern regex = Pattern.compile(".*:(.*) cfp");
		Matcher m = regex.matcher(doc.text());
		if (m.find()) {
			return m.group(1);
		}
		
		return "";
	}

	@Override
    protected List<String> getImagesFromDoc(Document doc, String rootUrl) {
        Elements els = doc.select(getImageSelector());
        Stream<String> imgz = els.stream().map(e -> {            
            String href = e.attr("href");            
            String img = StringUtils.substringBetween(href, "big&src=", "&title");
            //System.out.println("img: " + rootUrl+img);
            return img!=null?rootUrl+img:"";
            });        
        return imgz.collect(Collectors.toList());
}
	
}
