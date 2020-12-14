import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;


/**
 * Class to calculate percision@k, recall@k amd map@k for the recommendations
 */
public class EvaluateResults {
    Map<String, List<Integer>> playlistResults = new HashMap<>();
    List<Integer> k_Values = new ArrayList<>();
    List<String> existingPlaylists = new ArrayList<>();

    EvaluateResults(){
        this.k_Values.add(10);
        this.k_Values.add(20);
        this.k_Values.add(50);
        this.k_Values.add(100);
    }

    protected void getPrecision(String playlist) {
        Double precision;
        String csvRow = playlist + ",";
        int songCount;
        for(Integer k: this.k_Values) {
            songCount = 0;
            if(this.playlistResults.keySet().contains(playlist))
                for(Integer index: this.playlistResults.get(playlist)){
                    if(index < k){
                        songCount++;
                    }
                }
            precision = (double) songCount / k;
            csvRow += precision + ",";
        }
        try{
            FileWriter csvWriter = new FileWriter("B-SpotifyPrecision@K.csv", true);
            csvWriter.append(csvRow);
            csvWriter.append("\n");
            csvWriter.flush();
            csvWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    protected void getRecall(String playlist) {
        System.out.println(playlist);
        Double recall;
        String csvRow = playlist + ",";
        int songCount;
        for(Integer k: this.k_Values) {
            songCount = 0;
            if(this.playlistResults.keySet().contains(playlist))
                for(Integer index: this.playlistResults.get(playlist)){
                    if(index < k){
                        songCount++;
                    }
                }
            recall = (double) songCount / 10;
            csvRow += recall + ",";
        }
        try{
            FileWriter csvWriter = new FileWriter("B-SLHRecall@K.csv", true);
            csvWriter.append(csvRow);
            csvWriter.append("\n");
            csvWriter.flush();
            csvWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    protected void readResultsFile() {
        String line = "";
        int count =0;
        String currentPlaylist = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader("resultsSpotify#3.txt"));
            while((line = br.readLine()) != null) {
                if(count % 11 == 0) {
                   // System.out.println(line);
                    this.playlistResults.put(line, new ArrayList<>());
                    currentPlaylist = line;
                } else {
                    String[] lineSplit = line.split(": ");
                    this.playlistResults.get(currentPlaylist).add(Integer.valueOf(lineSplit[1]));
                }
                count++;

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void readClassExpressionResults(String filename) {
        String line = "";
        int count =0;
        String csvRow = "";
        String currentPlaylist = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            FileWriter csvWriter = new FileWriter("SpotifyExpressionAccuracy.csv", true);
            while((line = br.readLine()) != null) {
                csvRow = "";
                String[] lineSplit = line.split("[:#]");
                List<String> lineSplitList = Arrays.asList(lineSplit);
                currentPlaylist = lineSplit[0];
                csvRow += currentPlaylist + "," + lineSplit[2].split(" ")[1] + "," + lineSplit[3].split(" ")[1];
                System.out.println(csvRow);
                csvWriter.append(csvRow);
                csvWriter.append("\n");
                //System.out.println(lineSplitList);
                count++;

            }
            csvWriter.flush();
            csvWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void averagePrecision() {
        String line = "";
        int count =0;
        Double precision = 0.0;
        String currentPlaylist = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader("DL-SLHRecall@K.csv"));
            while((line = br.readLine()) != null) {
                String[] lineSplit = line.split(",");
                if(this.existingPlaylists.contains(lineSplit[0]))
                    precision += Double.parseDouble(lineSplit[1]);

            }

            precision = precision / 230;
            System.out.println(precision);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void existingPlaylists(String filename) {
        String line = "";

        String currentPlaylist = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            while((line = br.readLine()) != null) {
                String[] rowSplit = line.split(",");

                this.existingPlaylists.add(rowSplit[0]);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

//    protected void MRR(String playlist) {
//        Double mrr = 0.0;
//        String csvRow = playlist + ",";
//        int songCount;
//
//        for(Integer index: this.playlistResults.get(playlist)){
//           mrr += (1/(double) (index + 1));
//        }
//
//        csvRow += mrr;
//
//        try{
//            FileWriter csvWriter = new FileWriter("DLSHLMRR.csv", true);
//            csvWriter.append(csvRow);
//            csvWriter.append("\n");
//            csvWriter.flush();
//            csvWriter.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    public void MAP(String playlist) {
        int numRelevantSongs;
        double precisionAtJ, averagePrecision;
        String csvRow = playlist + ",";
        if(this.playlistResults.keySet().contains(playlist))
            for(int k: this.k_Values) {
             //   System.out.println("K: " + k);
                numRelevantSongs = 0;
                precisionAtJ = 0.0;
                Collections.sort(this.playlistResults.get(playlist));
                for(int pos: this.playlistResults.get(playlist)) {
                    if(pos <= k) {
           //             System.out.println("Position: " + (pos+1));
                        numRelevantSongs ++;

                        precisionAtJ +=  (numRelevantSongs/ (double)(pos+1));
         //               System.out.println("numrelevant songs" + numRelevantSongs + ": " + precisionAtJ);
                    }
                }
                averagePrecision = (double) precisionAtJ / 10;
                csvRow += averagePrecision + ",";



            }

        try{
            FileWriter csvWriter = new FileWriter("DL-SMAP@K.csv", true);
            csvWriter.append(csvRow);
            csvWriter.append("\n");
            csvWriter.flush();
            csvWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }





    public static void main(String[] args) {
        EvaluateResults evaluateResults = new EvaluateResults();
//        evaluateResults.existingPlaylists("DL-S200v8d500wRecall@K.csv");
//        evaluateResults.readResultsFile();
//        System.out.println(evaluateResults.playlistResults.keySet().size());
//
//        System.out.println(evaluateResults.existingPlaylists.size());
//        for(String playlist: evaluateResults.existingPlaylists) {
//            evaluateResults.MAP(playlist);
//   //         evaluateResults.getPrecision(playlist);
//   //         evaluateResults.getRecall(playlist);
//  //          evaluateResults.MRR(playlist);
//        }
       // evaluateResults.readClassExpressionResults("SAccuracy#1.txt");
 //       evaluateResults.averagePrecision();
        EvaluateRecommender evalRecommender = new EvaluateRecommender();
        List<String> existingPlaylists = evalRecommender.getValidPlaylists();
    }
}
