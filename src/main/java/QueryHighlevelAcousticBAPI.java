

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class to retrieve song information from the AcousticBrainz API
 */
public class QueryHighlevelAcousticBAPI {

    List<String> existingSongs = new ArrayList<>();

    /**
     * Get the songs for which data is already retrieved.
     */
    public void getExistingSongs() {
        try {
            BufferedReader csvReader = new BufferedReader(new FileReader("HighlevelDataset5.csv"));
            String columnHeadings = csvReader.readLine();
            String row;

            while((row = csvReader.readLine()) != null) {
                String[] rowElements = row.split(",");
                existingSongs.add(rowElements[2]);

            }
            System.out.println("Number of exsisting songs" + existingSongs.size());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Read the file containing all the song names
     * @param filename
     */
    public void readCSV(String filename) {
        int count = 0;
        try {
            BufferedReader csvReader = new BufferedReader(new FileReader(filename));
            String columnHeadings = csvReader.readLine();
            String row;
            while((row = csvReader.readLine()) != null) {
                String[] songInfoArray = row.split(",");
                if(!existingSongs.contains(songInfoArray[2])) {
                    //if( count>=9000 && count <10105) {
                    try {
                        getMBID(row);
                    } catch (Exception e) {
                        continue;
                    }
                }
                count++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to get the valid MBID of a song from MusicBraiz API and write it to a file
     * @param row
     * @throws Exception
     */
    public void getMBID(String row) throws Exception {
        String[] songInfoArray = row.split(",");
        List<String> songInfo = new ArrayList<String>(Arrays.asList(songInfoArray));
        String uri = "https://musicbrainz.org/ws/2/recording?query=%22" + songInfo.get(0) + "%22&fmt=json";
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .setHeader("Content-type", "application/json")
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JSONObject json = new JSONObject(response.body());
        List<String> mbids = new ArrayList<>();
        if(json.has("recordings")) {
            JSONArray recordings = json.getJSONArray("recordings");

            for(Object object: recordings) {
                //System.out.println(object.toString());
                JSONObject recording = new JSONObject(object.toString());
                //System.out.println(recording);

                if(recording.has("artist-credit")) {
                    JSONArray artistCredits = (JSONArray) recording.get("artist-credit");
                    JSONObject artist = new JSONObject(artistCredits.get(0).toString());
                    //System.out.println(artist);
                    if(artist.get("name").equals(songInfo.get(1))) {
                        System.out.println(songInfo.get(2) + ": " + artist.get("name") + ", " + (String) new JSONObject(object.toString()).get("id") );
                        mbids.add((String) new JSONObject(object.toString()).get("id"));

                    }


                }

            }
        }

        selectValidMBID(songInfo, mbids);

    }

    /**
     * Method to select the valid MBID that points to the song in our dataset
     * @param songInfo
     * @param mbids
     * @return
     * @throws Exception
     */
    public List<String> selectValidMBID(List<String> songInfo, List<String> mbids) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        List<String>  highlevelElements = null;
        String validMBID;
        for(String mbid: mbids) {
            HttpRequest request = HttpRequest.newBuilder()
                   .uri(URI.create("http://acousticbrainz.org/" + mbid + "/high-level"))
                   .setHeader("Content-type", "application/json")
                   .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject highlevelData = new JSONObject(response.body());
            if(highlevelData.has("highlevel")) {
                validMBID = mbid;

                songInfo.add(validMBID);
                highlevelElements = getHighlevelData(validMBID, highlevelData);

                break;

            }
        }
        if(highlevelElements != null) {
            songInfo.addAll(highlevelElements);
            System.out.println("songName: " + songInfo.get(0) + " artist: " + songInfo.get(1) );
            writeToCSV(songInfo);
        }


        return songInfo;
    }

    /**
     * Method to get the high-level information of songs from acousticbrainz api
     * @param mbid
     * @param highlevelData
     * @return
     * @throws Exception
     */
    public List<String> getHighlevelData(String mbid, JSONObject highlevelData) throws Exception {
        highlevelData = highlevelData.getJSONObject("highlevel");
        List<String> rowElements = new ArrayList<>();
        String gender = highlevelData.getJSONObject("gender").getString("value");
        rowElements.add(gender);
        String acoustic = highlevelData.getJSONObject("mood_acoustic").getString("value");
        rowElements.add(acoustic);
        String aggressive = highlevelData.getJSONObject("mood_aggressive").getString("value");
        rowElements.add(aggressive);
        String electronic = highlevelData.getJSONObject("mood_electronic").getString("value");
        rowElements.add(electronic);
        String happy = highlevelData.getJSONObject("mood_happy").getString("value");
        rowElements.add(happy);
        String party = highlevelData.getJSONObject("mood_party").getString("value");
        rowElements.add(party);
        String relaxed = highlevelData.getJSONObject("mood_relaxed").getString("value");
        rowElements.add(relaxed);
        String sad = highlevelData.getJSONObject("mood_sad").getString("value");
        rowElements.add(sad);
        String timbre = highlevelData.getJSONObject("timbre").getString("value");
        rowElements.add(timbre);
        String tonal = highlevelData.getJSONObject("tonal_atonal").getString("value");
        rowElements.add(tonal);

        return rowElements;
    }

    /**
     * Method to write the collected data to a csv file
     * @param songInfo
     * @throws Exception
     */
    public void writeToCSV(List<String> songInfo) throws Exception {
        FileWriter csvWriter = new FileWriter("HighlevelDataset5.csv", true);
        csvWriter.append(String.join(",", songInfo));
        csvWriter.append("\n");
        csvWriter.flush();
        csvWriter.close();

    }

    public void queryAcousticBrainz() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://acousticbrainz.org/7eab5af9-f283-466e-8ebf-df0bbcfecba9/high-level"))
                .setHeader("Content-type", "application/json")
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.body());
    }


    public static void main(String[] args) throws Exception {
        QueryHighlevelAcousticBAPI queryAPI = new QueryHighlevelAcousticBAPI();
        queryAPI.getExistingSongs();
        queryAPI.readCSV("SongDataset2.csv");

   }
}
