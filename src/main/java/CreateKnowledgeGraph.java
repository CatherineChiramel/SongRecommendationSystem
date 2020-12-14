import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class to convert the song data in CSV file to RDF format.
 */
public class CreateKnowledgeGraph {

    protected String prefixURI;
    protected PrintWriter writer;
    protected String RDFfilename;
    protected List<String> addedFeatures;
    static File dataDirectory = new File("../../../../SongData/");
    static File resultDirectory = new File("../../../../Results/");

    /**
     * Constructor initialised with the default values
     */
    CreateKnowledgeGraph(){
        this.prefixURI = "http://upb.de/Music#";
        this.RDFfilename = dataDirectory.getAbsolutePath() + "KG/MAKG.owl";
        this.addedFeatures = new ArrayList<>();
//        try {
//            this.writer = new PrintWriter(new BufferedWriter(new FileWriter(this.RDFfilename, true)));
//        } catch (IOException e) {
//            if(this.writer!=null)
//                this.writer.close();
//            e.printStackTrace();
//        }
    }

    /**
     * Parametrized constructor
     * @param prefixURI
     * @param RDFfilename
     */
    CreateKnowledgeGraph(String prefixURI, String RDFfilename) {
        this.prefixURI = prefixURI;
        this.RDFfilename = RDFfilename;
//        try {
//            this.writer = new PrintWriter(new BufferedWriter(new FileWriter(this.RDFfilename, true)));
//        } catch (IOException e) {
//            if(this.writer!=null)
//                this.writer.close();
//            e.printStackTrace();
//        }
    }

    /**
     * Add individual entry for each feature of the song in the Knowledge Graph
     * @param columnTitles title of the columns in the CSV dataset file to identify the feature name
     * @param rowCSV the row in the CSV file from which the data has to be taken
     * @param featureName the name of hte feature whose entry is to be added
     */
    public void addIndividualFeature(String columnTitles, String rowCSV, String featureName) {
        try {
            this.writer = new PrintWriter(new BufferedWriter(new FileWriter(this.RDFfilename, true)));
            List<String>  columnTitleList = Arrays.asList(columnTitles.split(","));
            List<String>  rowCSVList = Arrays.asList(rowCSV.split(","));

            int index = columnTitleList.indexOf(featureName);

            if(featureName.equals("RelatedTo")) {
                String[] relatedItems = rowCSVList.get(8).split("###");
                for(String item: relatedItems) {
                    if(!this.addedFeatures.contains(this.prefixURI + item)) {
                        this.addedFeatures.add(this.prefixURI + item);
                        this.writer.println("<!-- " + this.prefixURI + item + " -->");
                        this.writer.println("<prefix:Song rdf:about=\"" + this.prefixURI + item + "\">");
                        this.writer.println("\t<rdf:type rdf:resource=\"" + this.prefixURI + "Song\"/>");
                        this.writer.println("\t<rdf:type rdf:resource=\"http://www.w3.org/2002/07/owl#Thing\"/>");
                        this.writer.println("</prefix:Song>");
                        this.writer.println();
                        this.writer.println();
                    }
                }
            }
            else {
                if(!this.addedFeatures.contains(this.prefixURI + featureName + rowCSVList.get(index) ) && (!rowCSVList.get(index).equals("NA"))) {
                    this.addedFeatures.add(this.prefixURI + featureName + rowCSVList.get(index) );
                    this.writer.println("<!-- " + this.prefixURI + featureName + "_"+ rowCSVList.get(index) + " -->");
                    this.writer.println("<prefix:" + featureName + " rdf:about=\"" + this.prefixURI + featureName + "_" + rowCSVList.get(index) + "\">");
                    this.writer.println("\t<rdf:type rdf:resource=\"" + this.prefixURI + featureName + "\"/>");
                    this.writer.println("\t<rdf:type rdf:resource=\"http://www.w3.org/2002/07/owl#Thing\"/>");
                    this.writer.println("</prefix:" +  featureName + ">");
                    this.writer.println();
                    this.writer.println();
                }

            }

            this.writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Add indiviual  entry for a song in the Knowledge graph
     * @param columnTitles title of the columns in the CSV dataset file to identify the feature name
     * @param rowCSV the row in the CSV file from which the data has to be taken
     * @param properties the properties of the song that needs to be added
     */
    public void addIndividualSong(String columnTitles, String rowCSV, List<String> properties) {
        List<String>  columnTitleList = Arrays.asList(columnTitles.split(","));
        List<String>  rowCSVList = Arrays.asList(rowCSV.split(","));
        int index = columnTitleList.indexOf("Song");
        try {
            this.writer = new PrintWriter(new BufferedWriter(new FileWriter(this.RDFfilename, true)));
            this.writer.println("<!-- " + this.prefixURI + rowCSVList.get(index) + " -->");
            this.writer.println("<prefix:Song rdf:about=\"" + this.prefixURI + rowCSVList.get(index) + "\">");
            this.writer.println("\t<rdf:type rdf:resource=\"" + this.prefixURI + "Song\"/>");
            this.writer.println("\t<rdf:type rdf:resource=\"http://www.w3.org/2002/07/owl#Thing\"/>");
            for(int i=0; i<properties.size(); i++) {
                index = columnTitleList.indexOf(properties.get(i));
                if(index<rowCSVList.size())
                    if(!rowCSVList.get(index).equals("NA")) {
                        if(properties.get(i).equals("RelatedTo")) {
                            String[] relatedItems = rowCSVList.get(index).split("###");
                            for(String item: relatedItems) {
                                this.writer.println("\t<prefix:isRelatedTo" + " rdf:resource=\"" + this.prefixURI + item+ "\"/>");
                            }
                        }
                        else
                            this.writer.println("\t<prefix:has" + properties.get(i) + " rdf:resource=\"" + this.prefixURI + properties.get(i) + "_" + rowCSVList.get(index)+ "\"/>");
                    }
            }
            this.writer.println("</prefix:Song>");
            this.writer.println();
            this.writer.println();
            this.writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to read the song data file and create knowledge graph
     * @param songDataFile CSV file containing the song meta data
     * @param properties list of properties of the song that need to be added to the knowledge graph
     */
    public void createKnowledgeGraph(String songDataFile, List<String> properties) {
        try {
            BufferedReader csvReader = new BufferedReader(new FileReader(songDataFile));
            String columnTitles = csvReader.readLine();
            String row;
            while((row = csvReader.readLine()) != null) {
                String[] rowList = row.split(",");
                for(int i=0; i<properties.size(); i++) {
                    if(i<rowList.length)
                        this.addIndividualFeature(columnTitles, row, properties.get(i));
                }
                this.addIndividualSong(columnTitles, row, properties);
                //System.out.println("");
            }

            csvReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        String songDataFile =  dataDirectory.getAbsolutePath() + "Dataset/MusicBrainzAcousticBrain.csv";
        List<String> properties = new ArrayList<>();
        properties.add("Artist");
        properties.add("Language");
        properties.add("Country");
        properties.add("ArtistGender");
        properties.add("RelatedTo");
//        properties.add("Popularity");
//        properties.add("Duration");
//        properties.add("Acousticness");
//        properties.add("Danceability");
//        properties.add("Energy");
//        properties.add("Instrumentalness");
//        properties.add("Key");
//        properties.add("Liveness");
//        properties.add("Loudness");
//        properties.add("Audio_mode");
//        properties.add("Tempo");
//        properties.add("Time_signature");
//        properties.add("Audio_valence");
//        properties.add("Gender");
        properties.add("Aggressive");
        properties.add("Electronic");
        properties.add("Happy");
        properties.add("Party");
        properties.add("Relaxed");
        properties.add("Sad");
        properties.add("Dark");
        properties.add("Tone");
        properties.add("DynamicComplexity");
        properties.add("SpectralFlux");
        properties.add("SpectralSkewness");
        properties.add("SpectralEnergy");
        properties.add("Dissonance");
        properties.add("SpectralEntropy");
        properties.add("PitchSalience");
        properties.add("SpectralComplexity");
        properties.add("BeatsCount");
        properties.add("BeatsLoudness");

        CreateKnowledgeGraph kg = new CreateKnowledgeGraph();
        kg.createKnowledgeGraph(songDataFile, properties);
        kg.writer.close();
    }
}
