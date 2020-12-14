import java.io.*;
import java.util.*;

/**
 * Class to generate recommendations for song playlists
 */
public class Recommender {
    static File dataDirectory = new File("../../../../SongData/");
    static File resultDirectory = new File("../../../../Results/");
    public HashMap<String, List<Double>> songVectorMap;
    public int vectorSize = 200;
    protected String playlistsNameFile = "uniquePlaylists.csv";
    protected String SongEmbeddingsFile = dataDirectory.getAbsolutePath() + "Embeddings/SMAEmbeddings.csv";
    protected String playlistSongsFile = dataDirectory.getAbsolutePath() + "playlistSongs.csv";



    public Recommender() {
         this.songVectorMap = new HashMap<>();
    }


    /**
     * Parses a CSV file containing the names of playlists and returns a list of playlist names
     *
     * @return list of playlist names
     */
    public List<String> getPlaylistNames() {
        String line = "";
        List<String> playlistNames = new ArrayList<String>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(this.playlistsNameFile));
            line = br.readLine();
            while((line = br.readLine()) != null) {
                playlistNames.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return playlistNames;
    }

    /**
     *  Sort the playlist based on its size and write them to a csv in sorted order
     * @param playlistSongMap
     */
    public void sortPlaylistSize(HashMap<String, List<String>> playlistSongMap)  {
        Map<String, Integer> playlistSizeMap = new HashMap<>();
        for(String key: playlistSongMap.keySet()) {
            playlistSizeMap.putIfAbsent(key, playlistSongMap.get(key).size());
        }

        LinkedHashMap<String, Integer> sortedMap = new LinkedHashMap<>();
        playlistSizeMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEachOrdered(x -> sortedMap.put(x.getKey(), x.getValue()));
        FileWriter csvWriter = null;
        try {
            csvWriter = new FileWriter(dataDirectory.getAbsolutePath() + "uniquePlaylistsNew.csv");
            for(String key: sortedMap.keySet()) {
                System.out.println(key + ": " + sortedMap.get(key));
                if(sortedMap.get(key) >= 30) {
                    csvWriter.append(key);
                    csvWriter.append("\n");
                }
            }

            csvWriter.flush();
            csvWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     *  Create a file with playlists and their centroid vectors
     * @param playlistSongMap maps playlists to the list of songs contained in it
     */
    public void createPlaylistVectorFile (HashMap<String, List<String>> playlistSongMap) {
        String playlist;
        List<Double> centroidVector = null;
        try {

            BufferedReader br = new BufferedReader(new FileReader("validPlaylists.csv"));
            FileWriter csvWriter = new FileWriter(dataDirectory.getAbsolutePath() + "PlaylistVectors/SMAPlaylistVectors.csv");
            while((playlist = br.readLine()) != null) {
                System.out.println(playlist);
                System.out.println(playlistSongMap.get(playlist));
                centroidVector = generateCentroidVector(playlistSongMap.get(playlist), this.vectorSize);
                String row = playlist + ",";
                List<String> vectorStringList = new ArrayList<>();

                for(int i=0; i<centroidVector.size(); i++) {
                    vectorStringList.add(centroidVector.get(i).toString());
                    //System.out.println(centroidVector.get(i));
                }

                row += String.join(",", vectorStringList);
                csvWriter.append(row);
                csvWriter.append("\n");


            }
            csvWriter.flush();
            csvWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Generates a map between playlist and the list of songs
     * @param playlistNames
     *
     * @return hashmap between playlist and corresponding songs
     */
    public HashMap<String, List<String>> generatePlaylistSongMap(List<String> playlistNames) {
        HashMap<String, List<String>> playlistSongMap = new HashMap<>();
        for(int i=0; i<playlistNames.size(); i++){
            playlistSongMap.put(playlistNames.get(i), new ArrayList<>());
        }
        String line;
        try {
            BufferedReader br = new BufferedReader(new FileReader(this.playlistSongsFile));
            line = br.readLine();
            List<String> songFileColumns;
            while ((line = br.readLine()) != null) {
                songFileColumns = Arrays.asList(line.split(","));
                String songIdentifier =  songFileColumns.get(4);
                //String songIdentifier =  songFileColumns.get(4);
                //if(playlistSongMap.containsKey(songFileColumns.get(2)))
                //System.out.println(songFileColumns.get(3));
                if(!playlistSongMap.keySet().contains(songFileColumns.get(3)))
                    playlistSongMap.put(songFileColumns.get(3), new ArrayList<>());
                if(!playlistSongMap.get(songFileColumns.get(3)).contains(songIdentifier)) {
                    playlistSongMap.get(songFileColumns.get(3)).add(songIdentifier);
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return playlistSongMap;
    }

    /**
     * Generate a map from song identifiers to their respective vectors from the embeddings file.
     *
     * @return Map containing the songs and their corresponding vectors
     */
    public HashMap<String, List<Double>> createSongVectorMap() {

        List<String> vectorFileRow;
        List<Double> vectors;
        String line;
        try {

            BufferedReader br = new BufferedReader(new FileReader(this.SongEmbeddingsFile));
            line = br.readLine();
            while((line = br.readLine()) != null) {
       //         System.out.println(line);
                vectorFileRow = Arrays.asList(line.split(","));
                if(!this.songVectorMap.containsKey(vectorFileRow.get(0))) {
                    vectors = new ArrayList<Double>();
                    for(int i=1; i<vectorFileRow.size(); i++) {
                        vectors.add(Double.parseDouble(vectorFileRow.get(i)));
                    }
                    this.songVectorMap.put(vectorFileRow.get(0), vectors);
                }
            }

//            for (String key: this.songVectorMap.keySet()) {
//                System.out.println(key + ": " + this.songVectorMap.get(key));
//            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return this.songVectorMap;
    }

    /**
     * Create the centroid vector for the playlist
     * @param songIdentifiers
     * @return the centroid vector
     */
    public List<Double> generateCentroidVector(List<String> songIdentifiers, int dimension) {
        List<List<Double>> songVectors = new ArrayList<List<Double>>();
        Double [] centroidVectorArray = new Double[dimension];
        Arrays.fill(centroidVectorArray, 0.0);
        //System.out.println(songIdentifiers.size());
        for(int i=0; i<songIdentifiers.size(); i++) {
            if(this.songVectorMap.keySet().contains(songIdentifiers.get(i))  && this.songVectorMap.get(songIdentifiers.get(i)) != null)
                songVectors.add(this.songVectorMap.get(songIdentifiers.get(i)));
        }
        for(int i=0; i<songVectors.size(); i++) {
            for(int j=0; j<songVectors.get(0).size(); j++) {

                centroidVectorArray[j] += songVectors.get(i).get(j);
            }
        }
        for(int i=0; i<centroidVectorArray.length; i++) {
            centroidVectorArray[i] = centroidVectorArray[i] / songIdentifiers.size();
        }
        List<Double> centroidVector = Arrays.asList(centroidVectorArray);
        return centroidVector;
    }


    /**
     * Generate a list of recommended songs for the given playlist.
     * @param songIdentifiers The songs that are present in the playlist
     * @param centroidVector The centroid vector of the playlist
     * @param numRecommendations The number of recommendations required
     * @return a list of recommended songs
     */
    public List<String> recommendSongs(List<String> songIdentifiers, List<Double> centroidVector, int numRecommendations) {
        HashMap<String, Double> songSimilarityMap = new HashMap<String, Double>();
        List<String> recommendations = new ArrayList<>();
        for(String key: this.songVectorMap.keySet()) {
            if(!songIdentifiers.contains(key)) {
                songSimilarityMap.put(key, this.cosineSimilarity(centroidVector, this.songVectorMap.get(key)));
            }
        }
        LinkedHashMap<String, Double> sortedMap = new LinkedHashMap<>();
        songSimilarityMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEachOrdered(x -> sortedMap.put(x.getKey(), x.getValue()));
        int i=0;
        for(String key: sortedMap.keySet()) {
            if(i == numRecommendations)
                break;
            System.out.println(key + " :" + sortedMap.get(key)) ;
            recommendations.add(key);
            i++;
        }
        return recommendations;
    }



    /**
     * Find cosine similarity between 2 vectors.
     * @param vectorA
     * @param vectorB
     * @return Double value indicating the cosine similarity
     */
    public Double cosineSimilarity(List<Double> vectorA, List<Double> vectorB) {
        Double dotProduct = 0.0;
        Double normA = 0.0;
        Double normB = 0.0;
        for (int i = 0; i < vectorA.size(); i++) {
            dotProduct += vectorA.get(i) * vectorB.get(i);
            normA += Math.pow(vectorA.get(i), 2);
            normB += Math.pow(vectorB.get(i), 2);
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    /**
     * Method to check how many songs in the dataset are more similar to the playlist centroid vector than the least similar
     * test song.
     * @param playlist
     * @param songIdentifiers
     * @return
     */
    protected int evaluateAssumption(String playlist, List<String> songIdentifiers) {
        List<String> testSongs = new ArrayList<>();
        Map<String, Double> songSimilarity = new HashMap<>();
        int count =0;
        for(int i=songIdentifiers.size()-1; i>0; i--){
            if(count == 10) {
                break;
            }
            testSongs.add(songIdentifiers.get(i));
            count ++;
        }
        List<Double> playlistVector = this.generateCentroidVector(testSongs, this.vectorSize);
        for(int i=testSongs.size()-1; i>0; i--) {
            Double similarity = this.cosineSimilarity(playlistVector, this.songVectorMap.get(testSongs.get(i)));
            songSimilarity.put(testSongs.get(i), similarity);
        }
        LinkedHashMap<String, Double> sortedSongSimilarity = new LinkedHashMap<>();
        songSimilarity.entrySet()
                .stream()
//                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .sorted(Map.Entry.comparingByValue())
                .forEachOrdered(x -> sortedSongSimilarity.put(x.getKey(), x.getValue()));
        Double targetSim = 0.0;
        for(String key: sortedSongSimilarity.keySet()) {
            //System.out.println(key + ": " + sortedSongSimilarity.get(key));
            targetSim = sortedSongSimilarity.get(key);
            break;
        }
        int songCount = 0;
        for(String key: this.songVectorMap.keySet()) {
            if(songIdentifiers.contains(key))
                continue;
            Double sim = this.cosineSimilarity(playlistVector, this.songVectorMap.get(key));
            if(sim > targetSim)
                songCount ++;

        }
        System.out.println(playlist +  ": " + songCount);
        return songCount;
    }


    public static void main(String [] args) {
        Recommender recommender = new Recommender();
        List<String> playlistNames = recommender.getPlaylistNames();
        HashMap<String, List<String>> playlistSongMap = recommender.generatePlaylistSongMap(playlistNames);
        recommender.createSongVectorMap();
        //recommender.evaluateAssumption("Piano in the Background", playlistSongMap.get("Piano in the Background"));
//        List<String> songIdentifiers = play
        recommender.createPlaylistVectorFile(playlistSongMap);
//        recommender.sortPlaylistSize(playlistSongMap);
//        recommender.createSongVectorMap();
//        List<String> songIdentifiers = playlistSongMap.get("00s Rock Anthems");
//        Collections.shuffle(songIdentifiers);
//        List<String> testSongIdentifiers = songIdentifiers.subList(songIdentifiers.size()-20, songIdentifiers.size());
//        songIdentifiers.subList(songIdentifiers.size() - 20, songIdentifiers.size()).clear();
//        List<Double> centroidVector = recommender.generateCentroidVector(playlistSongMap.get("00s Rock Anthems"), 20);
//        List<String> recommendations = recommender.recommendSongs(songIdentifiers, centroidVector, 10);
    }

    public int getVectorSize() {
        return this.vectorSize;
    }

    public void setVectorSize(int value) {
        this.vectorSize = value;
    }

    public String getPlaylistsNameFile() {
        return this.playlistsNameFile;
    }

    public void setPlaylistsNameFile(String filename) {
        this.playlistsNameFile = filename;
    }

    public String getSongEmbeddingsFile() {
        return this.SongEmbeddingsFile;
    }

    public void setSongEmbeddingsFile(String filename) {
        this.SongEmbeddingsFile = filename;
    }

    public String getPlaylistSongsFile() {
        return this.playlistSongsFile;
    }

    public void setPlaylistSongsFile(String filename) {
        this.playlistSongsFile = filename;
    }
}
