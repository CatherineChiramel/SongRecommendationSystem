

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KnowledgeGraph {

    protected String prefixURI;
    protected PrintWriter writer;
    protected String RDFfilename;

    /**
     * Constructor initialised with the default values
     */
    KnowledgeGraph(){
        this.prefixURI = "http://upb.de/";
        this.RDFfilename = "songRDF.owl";
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
    KnowledgeGraph(String prefixURI, String RDFfilename) {
        this.prefixURI = prefixURI;
        this.RDFfilename = RDFfilename;
        try {
            this.writer = new PrintWriter(new BufferedWriter(new FileWriter(this.RDFfilename, true)));
        } catch (IOException e) {
            if(this.writer!=null)
                this.writer.close();
            e.printStackTrace();
        }
    }

    /**
     * Add individual entry for the feature of the song in the Knowledge Graph
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
            this.writer.println("<!-- " + this.prefixURI + featureName + "/" + rowCSVList.get(index) + " -->");
            this.writer.println("<owl:NamedIndividual rdf:about=\"" + this.prefixURI + featureName + "/" + rowCSVList.get(index) + "\">");
            this.writer.println("\t<rdf:type rdf:resource=\"" + this.prefixURI + featureName + "/\"/>");
            this.writer.println("</owl:NamedIndividual>");
            this.writer.println();
            this.writer.println();
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
            this.writer.println("<!-- " + this.prefixURI + "Song/" + rowCSVList.get(index) + " -->");
            this.writer.println("<owl:NamedIndividual rdf:about=\"" + this.prefixURI + "Song/" + rowCSVList.get(index) + "\">");
            this.writer.println("\t<rdf:type rdf:resource=\"" + this.prefixURI + "Song/\"/>");
            for(int i=0; i<properties.size(); i++) {
                index = columnTitleList.indexOf(properties.get(i));
                this.writer.println("\t<has" + properties.get(i) + " rdf:resource=\"" + this.prefixURI +  properties.get(i) +"/" + rowCSVList.get(index)+ "\"/>");
            }
            this.writer.println("</owl:NamedIndividual>");
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
                for(int i=0; i<properties.size(); i++) {
                    this.addIndividualFeature(columnTitles, row, properties.get(i));
                }
                this.addIndividualSong(columnTitles, row, properties);
            }

            csvReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to close the file writer object
     */
    public void closeFileWriter() {
        this.writer.close();
    }

    @Override
    protected void finalize() throws Throwable {
        this.writer.close();
    }

    public static void main(String[] args) {
        String songDataFile = "preprocessedSongData2.csv";
        List<String> properties = new ArrayList<>();
        properties.add("Artist");
        properties.add("Album");
        properties.add("Popularity");
        properties.add("Duration");
        properties.add("Acousticness");

        properties.add("Danceability");
        properties.add("Energy");
        properties.add("Instrumentalness");
        properties.add("Key");
        properties.add("Liveness");
        properties.add("Loudness");
        properties.add("Audio_mode");
        properties.add("Tempo");
        properties.add("Time_signature");
        properties.add("Audio_valence");

        KnowledgeGraph kg = new KnowledgeGraph();
        kg.createKnowledgeGraph(songDataFile, properties);


        kg.writer.close();
    }
}
