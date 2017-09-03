package leechies.model;

import java.util.List;

public class Rubrique {
    public Category    category;
    public List<String> subUrls;
    
    public void setSubUrls(List<String> l) {
        this.subUrls = l;
        }
        
        public List<String> getSubUrls() {
            return this.subUrls;
            }
}
