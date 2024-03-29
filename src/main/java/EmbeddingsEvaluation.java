import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Class to evaluate the embeddings generated by various embeddings algorithms
 */
public class EmbeddingsEvaluation {

    /**
     * Method to calculate the centroid vector of all the songs in the dataset
     * @param recommender
     * @param songVectorMap
     * @return
     */
    public List<Double> getCentroidVector(Recommender recommender, HashMap<String, List<Double>> songVectorMap) {

        List<String> songIdentifiers = new ArrayList<>();
        String key1 = "";
        for(String key: songVectorMap.keySet()) {
            songIdentifiers.add(key);
            key1 = key;
        }
        List<Double> centroidVector1 = recommender.generateCentroidVector(songIdentifiers, songVectorMap.get(key1).size());
        return centroidVector1;
    }

    /**
     * Method to calculate the average cosine similarity of each song to the centroid vector
     * @param centroidVector
     * @param recommender
     * @param songVectorMap
     * @return
     */
    public double getAverageCosineSimilarity(List<Double> centroidVector, Recommender recommender, HashMap<String, List<Double>> songVectorMap) {

        double cosineSimilarity = 0.0;
        for(String key: songVectorMap.keySet()) {
            cosineSimilarity += recommender.cosineSimilarity(centroidVector, songVectorMap.get(key));
        }
        double averageCosineSimilarity = cosineSimilarity / songVectorMap.size();

        return averageCosineSimilarity;
    }

    /**
     * Calculate the standard deviation of the vectors of each song to the centroid vector
     * @param cosineSimilarity
     * @param recommender
     * @param songVectorMap
     * @param centroidVector
     * @return
     */

    public double getStandardDeviation(Double cosineSimilarity, Recommender recommender, HashMap<String, List<Double>> songVectorMap, List<Double> centroidVector) {
        double standardDeviation = 0.0, indCosineSim;
        for(String key: songVectorMap.keySet()) {
            indCosineSim = recommender.cosineSimilarity(centroidVector, songVectorMap.get(key));
            standardDeviation += Math.pow((indCosineSim - cosineSimilarity), 2);
        }
        standardDeviation = standardDeviation / songVectorMap.size();
        standardDeviation = Math.sqrt(standardDeviation);
        return standardDeviation;
    }


    public static void main(String[] args) {

        EmbeddingsEvaluation e = new EmbeddingsEvaluation();

        Recommender recommender = new Recommender();
        recommender.setSongEmbeddingsFile("SMAEmbeddings.csv");
        HashMap<String, List<Double>> songVectorMap = recommender.createSongVectorMap();
        List<Double> centroidVector1 =  e.getCentroidVector(recommender, songVectorMap);
        double avgCosSim1 = e.getAverageCosineSimilarity(centroidVector1, recommender, songVectorMap);
        double standardDev1 = e.getStandardDeviation(avgCosSim1, recommender, songVectorMap, centroidVector1);
        System.out.println("RDF2Vec");
        System.out.println("Average cosine simialrity: " + avgCosSim1);
        System.out.println("Standard Deviation:" + standardDev1);

        Recommender recommender2 = new Recommender();
        recommender2.setSongEmbeddingsFile("PYKE45Embeddings.csv");
        HashMap<String, List<Double>> songVectorMap2 = recommender2.createSongVectorMap();
        List<Double> centroidVector2 =  e.getCentroidVector(recommender2, songVectorMap2);
        double avgCosSim2 = e.getAverageCosineSimilarity(centroidVector2, recommender2, songVectorMap2);
        double standardDev2 = e.getStandardDeviation(avgCosSim2, recommender2, songVectorMap2, centroidVector2);
        System.out.println("PYKE");
        System.out.println("Average cosine simialrity: " + avgCosSim2);
        System.out.println("Standard Deviation:" + standardDev2);






    }
}
