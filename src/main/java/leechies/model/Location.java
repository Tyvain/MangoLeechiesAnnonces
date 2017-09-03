package leechies.model;

import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Location {
    final static Logger logger = LoggerFactory.getLogger("Location");
    private static final HashMap<String, String> map    = new HashMap<String, String>();

    static {
        map.put("Bélep", "71");
        map.put("Boulouparis", "73");
        map.put("Bourail", "75");
        map.put("Canala", "77");
        map.put("Dumbéa", "79");
        map.put("Farino", "81");
        map.put("Hienghène", "83");
        map.put("Houaïlou", "85");
        map.put("Île des Pins", "87");
        map.put("Kaala-Gomen", "89");
        map.put("Koné", "91");
        map.put("Kouaoua", "93");
        map.put("Koumac", "95");
        map.put("La Foa", "97");
        map.put("Lifou", "99");
        map.put("Maré", "101");
        map.put("Moindou", "103");
        map.put("Mont-Dore", "105");
        map.put("Nouméa", "107");
        map.put("Ouégoa", "109");
        map.put("Ouvéa", "111");
        map.put("Païta", "113");
        map.put("Poindimié", "115");
        map.put("Ponerihouen", "117");
        map.put("Pouébo", "119");
        map.put("Pouembout", "121");
        map.put("Poum", "123");
        map.put("Poya", "125");
        map.put("Sarraméa", "127");
        map.put("Thio", "129");
        map.put("Touho", "131");
        map.put("Voh", "133");
        map.put("Yaté", "135");
    }

    public static String getIdByLocation(String location) {
        if (StringUtils.isEmpty(location)) {
            return null;
         }
        String key = location.replace("paita", "").trim();
        key = key.replace("le-mont-dore", "Mont-Dore");        

        String id = map.get(key);
        
        if (id == null) {
            logger.error("Impossible de trouver la ville: " + location);
            }
        return id;
    }
}
