import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class EvaluateRecommender {
    protected Recommender recommender;
    protected List<String> playlistNames;
    protected HashMap<String, List<String>> playlistSongMap;
    List<String> songIdentifiers;
    List<String> testSongIdentifiers;


    public void evaluateRecommendations (List<String> recommendations, String playlist, FileWriter textWriter) {
        Map<String, Integer> songIndexInRecommendations = new HashMap<>();
        List<String> testSongs = this.testSongIdentifiers;
        System.out.println(testSongs);
        for(int i=0; i<this.testSongIdentifiers.size(); i++) {
            songIndexInRecommendations.put(testSongs.get(i), -1);
            if(recommendations.contains(testSongs.get(i)))
                songIndexInRecommendations.put(testSongs.get(i), recommendations.indexOf(testSongs.get(i)));
        }
        System.out.println(songIndexInRecommendations.keySet());


        try {
            textWriter.append(playlist + "\n");
            for (String song: songIndexInRecommendations.keySet()) {
                System.out.println(song + ": " + songIndexInRecommendations.get(song));
                textWriter.append(song + ": " + songIndexInRecommendations.get(song));
                textWriter.append("\n");
            }


        } catch (IOException e) {
            e.printStackTrace();
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
        //this.testSongIdentifiers = new ArrayList<>();
       // int div = (int) (this.songIdentifiers.size()*.8);
        int div = 0;

        for(int i=this.songIdentifiers.size()-1; i>=0; i--) {
            if(div<10) {
                this.testSongIdentifiers.add(this.songIdentifiers.get(i));
                div++;
            }


            else {
                trainSongs.add(this.songIdentifiers.get(i));
            }
            }



//        for(int i=0; i<30; i++) {
//            trainSongs.add(this.songIdentifiers.get(i));
//        }
        List<Double> centroidVector = this.recommender.generateCentroidVector(this.playlistSongMap.get(playlistName), 200);
        List<String> recommendations = this.recommender.recommendSongs(trainSongs, centroidVector, 10050);

        return recommendations;
    }

    public List<String> getValidPlaylists() {
        String line = "";
        List<String> playlistNames = new ArrayList<String>();
        try {
            BufferedReader br = new BufferedReader(new FileReader("validPlaylists.csv"));

            while((line = br.readLine()) != null) {
                playlistNames.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return playlistNames;
    }

    public static void main(String[] args) {
        System.out.println("Initital time: " + System.currentTimeMillis());
        long initialTime = System.currentTimeMillis();
        EvaluateRecommender evaluateRecommender = new EvaluateRecommender();
        evaluateRecommender.recommender = new Recommender();
        List<String> playlistNames = evaluateRecommender.recommender.getPlaylistNames();
        List<String> validPlaylists = evaluateRecommender.getValidPlaylists();
        FileWriter textWriter = null;
        try {
            textWriter = new FileWriter("SLH200v8d500wResults1.txt");
            for(String playlist: validPlaylists) {
                List<String> recommendations = evaluateRecommender.getRecommendation(playlist);
                //System.out.println(recommendations);
                System.out.println("!!!" + playlist + "!!!");
                evaluateRecommender.evaluateRecommendations(recommendations, playlist, textWriter);
            }
            textWriter.flush();
            textWriter.close();
            long finalTime = System.currentTimeMillis();
            System.out.println("Time elapsed: " + (finalTime-initialTime) + "ms");
            System.out.println("Final time: " + System.currentTimeMillis());
        } catch (IOException e) {
            e.printStackTrace();
        }




    }
}