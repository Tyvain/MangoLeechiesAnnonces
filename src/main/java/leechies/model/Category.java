package leechies.model;

public enum Category {
    // VEHICULE
    VEHICULE_4x4("4x4", 183),
    VOITURES("Voitures", 175),
    MOTOS("Motos & scooters", 177),
    PIECES_DETACHEES("Pièces détachées", 179),
    VEHICULE_DIVERS("Divers (VEHICULES)", 181),
    
    // NAUTISME
    VOILIERS("Voiliers", 59),
    BATEAUX_MOTEUR("Bateaux moteur", 61),
    NAUTISME_SPORT_LOISIRS("Sport & loisirs", 63),
    PECHE("Pêche", 65),
    NAUTISME_DIVERS("Divers (NAUTISME)", 67),

    // MULTIMEDIA
    INFORMATIQUE("Informatique", 71),
    CONSOLE_JEUX("Consoles & Jeux vidéo", 73),
    IMAGE_SON("Image & Son", 75),
    TELEPHONIE("Téléphonie", 77),
    MULTIMEDIA_DIVERS("Divers (MULTIMEDIA)", 79),

    // LOISIRS
    DVD_LIVRES("DVD / CD / Livres", 143),
    SPORT_HOBBIES("Sports & Hobbies", 85),
    MUSIQUE("Musique", 87),
    COLLECTION("Collection", 89),
    JEUX_JOUETS("Jeux & Jouets", 91),
    VIN_GASTRONOMIE("Vins & Gastronomie", 93),
    LOISIRS_DIVERS("Divers (LOISIRS)", 95) ,

    // MAISON
    AMEUBLEMENT("Ameublement", 99),
    ELECTROMENAGER("Electroménager", 101),
    BRICOLAGE("Bricolage", 103),
    JARDINAGE("Jardinage", 105),
    ANIMAUX("Animaux", 107),
    MAISON_DIVERS("Divers (MAISON)", 109),
    
    // TROC
    TROC("Troc", 111),
    
    //PERDU TROUVE
    PERDU_TROUVE("Perdu trouvé", 113),
    
    // MODE
    FEMMES("Femmes", 163),
    HOMMES("Hommes", 165),
    BEBES("Bébés", 167),
    ACCESSOIRES_BIJOUX("Accessoires & bijoux", 169),
    MODE_DIVERS("Divers (MODE)", 171),
    
    // EMPLOI
    OFFRES("Offres", 129),
    DEMANDES("Demandes", 131),
    EMPLOI_DIVERS("Divers (EMPLOI)", 133),
    
    // IMMOBILIER
    VENTES("Ventes", 161),
    LOCATIONS("Locations", 139),
    IMMOBILIER_DIVERS("Divers (IMMOBILIER)", 141);
    
    

    public String libelle;
    public int id;

    Category(String l, int i) {
        this.libelle = l;
        this.id = i;
    }
    
    public static Category getCategoryFromLibelle (String l) {
       for (Category cat: Category.values()) {           
           if (cat.libelle.equals(l)) {
               return cat;
           }          
        }
        return null;
    }
}
