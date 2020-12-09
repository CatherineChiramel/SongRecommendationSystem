import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class CountSongs {
    public static void main(String[] args) {
        List<String> songs = new ArrayList<>();
        BufferedReader csvReader;
        FileWriter csvWriter;
        try {
            csvWriter  = new FileWriter("BaseSongs.csv");
            csvReader = new BufferedReader(new FileReader("HighlevelDataset5.csv"));
            String columnTitles = csvReader.readLine();
            String row;
            int count = 0;
            while((row = csvReader.readLine()) != null) {
                String[] rowSplit = row.split(",");
                if(!songs.contains(rowSplit[2])) {
                    songs.add(rowSplit[2]);
                    csvWriter.append(rowSplit[2]);
                    csvWriter.append("\n");
                }
            }
            csvReader.close();
            csvReader = new BufferedReader(new FileReader("LowlevelDatasetNew.csv"));
            columnTitles = csvReader.readLine();


            while((row = csvReader.readLine()) != null) {
                String[] rowSplit = row.split(",");
                if(!songs.contains(rowSplit[2])) {
                    songs.add(rowSplit[2]);
                    csvWriter.append(rowSplit[2]);
                    csvWriter.append("\n");
                }
            }
            csvReader.close();
            csvWriter.flush();
            csvWriter.close();

            System.out.println(songs.size());


            csvReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
