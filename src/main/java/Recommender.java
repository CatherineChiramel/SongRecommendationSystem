import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import java.util.stream.Stream;

public class Recommender {

    public HashMap<String, List<Double>> songVectorMap;
    public int vectorSize;

    public Recommender() {
         this.songVectorMap = new HashMap<String, List<Double>>();
         this.vectorSize = 20;
    }

    /**
     * Parses a CSV file containing the names of playlists and returns a list of playlist names
     * @param filename
     * @return list of playlist names
     */
    public List<String> getPlaylistNames(String filename) {
        String line = "";
        List<String> playlistNames = new ArrayList<String>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
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
     * Generates a map between playlist and the list of songs
     * @param playlistNames
     * @param songFilename
     * @return hashmap between playlist and corresponding songs
     */
    public HashMap<String, List<String>> generatePlaylistSongMap(List<String> playlistNames, String songFilename) {
        HashMap<String, List<String>> playlistSongMap = new HashMap<String, List<String>>();
        for(int i=0; i<playlistNames.size(); i++){
            playlistSongMap.put(playlistNames.get(i), new ArrayList<String>());
        }
        String line = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(songFilename));
            line = br.readLine();
            List<String> songFileColumns = new ArrayList<String>();
            while ((line = br.readLine()) != null) {
                songFileColumns = Arrays.asList(line.split(","));
                String songIdentifier = "http://upb.de/song/" + songFileColumns.get(4);
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
     * @param songVectorFile file that contains the song embeddings
     * @return
     */
    public HashMap<String, List<Double>> createSongVectorMap(String songVectorFile) {
        //String songVectorFile = "PYKE_50_embd.csv";
        //HashMap<String, List<Double>> songVectorMap = new HashMap<String, List<Double>>();

        List<String> vectorFileRow;
        List<Double> vectors;
        String line;
        try {
            BufferedReader br = new BufferedReader(new FileReader(songVectorFile));
            line = br.readLine();
            while((line = br.readLine()) != null) {
                vectorFileRow = Arrays.asList(line.split(","));
                if(!this.songVectorMap.containsKey(vectorFileRow.get(0))) {
                    vectors = new ArrayList<Double>();
                    for(int i=1; i<vectorFileRow.size(); i++) {
                        vectors.add(Double.parseDouble(vectorFileRow.get(i)));
                    }
                    this.songVectorMap.put(vectorFileRow.get(0), vectors);
                }
            }

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
    public List<Double> generateCentroidVector(List<String> songIdentifiers) {
        List<List<Double>> songVectors = new ArrayList<List<Double>>();

        Double [] centroidVectorArray = new Double[20];
        Arrays.fill(centroidVectorArray, 0.0);
        for(int i=0; i<songIdentifiers.size(); i++) {
            if(this.songVectorMap.get(songIdentifiers.get(i)) != null)
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
            if(i == 9)
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

    public static void main(String [] args) {
        Recommender recommender = new Recommender();
        List<String> playlistNames = recommender.getPlaylistNames("uniquePlaylists.csv");
        HashMap<String, List<String>> playlistSongMap = recommender.generatePlaylistSongMap(playlistNames,"playlistSongIdentifier.csv");
        System.out.println(playlistSongMap.get("00s Rock Anthems"));
        recommender.createSongVectorMap("processedPYKE.csv");
        List<String> songIdentifiers = playlistSongMap.get("00s Rock Anthems");

        List<String> testSongIdentifiers = songIdentifiers.subList(songIdentifiers.size()-10, songIdentifiers.size());

        songIdentifiers.subList(songIdentifiers.size() - 10, songIdentifiers.size()).clear();

        List<Double> centroidVector = recommender.generateCentroidVector(playlistSongMap.get("00s Rock Anthems"));
        List<String> recommendations = recommender.recommendSongs(songIdentifiers, centroidVector, 10);
        System.out.println(recommendations);

    }
}
