import java.util.*;

public class EvaluateRecommender {
    protected Recommender recommender;
    protected List<String> playlistNames;
    protected HashMap<String, List<String>> playlistSongMap;
    List<String> songIdentifiers;
    List<String> testSongIdentifiers;


    public void evaluateRecommendations (List<String> recommendations) {
        Map<String, Integer> songIndexInRecommendations = new HashMap<>();
        List<String> testSongs = this.testSongIdentifiers;
        //System.out.println(testSongs);
        for(int i=0; i<this.testSongIdentifiers.size(); i++) {
            songIndexInRecommendations.put(testSongs.get(i), -1);
            if(recommendations.contains(testSongs.get(i)))
                songIndexInRecommendations.put(testSongs.get(i), recommendations.indexOf(testSongs.get(i)));
        }

        for (String song: songIndexInRecommendations.keySet()) {
            System.out.println(song + ": " + songIndexInRecommendations.get(song));
        }
    }

    public List<String> getRecommendation(String playlistName) {
        this.recommender = new Recommender();
        this.playlistNames = this.recommender.getPlaylistNames();
        this.playlistSongMap = this.recommender.generatePlaylistSongMap(this.playlistNames);
        this.recommender.createSongVectorMap();
        this.songIdentifiers = this.playlistSongMap.get(playlistName);
        //Collections.shuffle(this.songIdentifiers);
        this.testSongIdentifiers = new ArrayList<>();
        List<String> trainSongs = new ArrayList<>();
        int div = (int) (this.songIdentifiers.size()*.8);


        for(int i=0; i<this.songIdentifiers.size(); i++) {
            if(i<div)
                trainSongs.add(this.songIdentifiers.get(i));
            else {

                this.testSongIdentifiers.add(this.songIdentifiers.get(i));
            }
            }



//        for(int i=0; i<30; i++) {
//            trainSongs.add(this.songIdentifiers.get(i));
//        }
        List<Double> centroidVector = this.recommender.generateCentroidVector(this.playlistSongMap.get(playlistName), 20);
        List<String> recommendations = this.recommender.recommendSongs(trainSongs, centroidVector, 10050);

        return recommendations;
    }

    public static void main(String[] args) {
        EvaluateRecommender evaluateRecommender = new EvaluateRecommender();
        evaluateRecommender.recommender = new Recommender();
        List<String> playlistNames = evaluateRecommender.recommender.getPlaylistNames();
        for(String playlist: playlistNames) {
            List<String> recommendations = evaluateRecommender.getRecommendation(playlist);
            System.out.println("!!!" + playlist + "!!!");
            evaluateRecommender.evaluateRecommendations(recommendations);
        }


    }
}
