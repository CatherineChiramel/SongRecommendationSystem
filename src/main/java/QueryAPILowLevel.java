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


public class QueryAPILowLevel {
    List<String> existingSongs = new ArrayList<>();
    public void getExistingSongs() {
        try {
            BufferedReader csvReader = new BufferedReader(new FileReader("LowlevelDataset.csv"));
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

    public void readCSV(String filename) {
        int count = 0;
        try {
            BufferedReader csvReader = new BufferedReader(new FileReader(filename));
            String columnHeadings = csvReader.readLine();
            String row;
            while((row = csvReader.readLine()) != null) {
                String[] songInfoArray = row.split(",");
                if(!existingSongs.contains(songInfoArray[2])) {
          //          if( count>=0 && count <10) {
                    try {
                        getMBID(row);
                    } catch (Exception e) {
                        continue;
                    }
            //        } else if(count >= 10) {
              //        break;
                //    }
                }
                count++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
                    //if(artist.get("name").equals(songInfo.get(1))) {
                        //System.out.println(songInfo.get(2) + ": " + artist.get("name") + ", " + (String) new JSONObject(object.toString()).get("id") );
                        mbids.add((String) new JSONObject(object.toString()).get("id"));
                    //}
                }
            }
        }
        selectValidMBID(songInfo, mbids);
    }

    public List<String> selectValidMBID(List<String> songInfo, List<String> mbids) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        List<String>  lowlevelElements = null;
        List<String> rhythmElements = null;
        String validMBID;
        for(String mbid: mbids) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://acousticbrainz.org/" + mbid + "/low-level"))
                    .setHeader("Content-type", "application/json")
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject lowlevelData = new JSONObject(response.body());
            if(lowlevelData.has("lowlevel")) {
                validMBID = mbid;

                songInfo.add(validMBID);
                lowlevelElements = getlowlevelData(validMBID, lowlevelData);
                break;
            }
        }
        if(lowlevelElements != null) {
            songInfo.addAll(lowlevelElements);
            System.out.println("songName: " + songInfo.get(0) + " artist: " + songInfo.get(1) );
            writeToCSV(songInfo);
        }
        return songInfo;
    }

    public List<String> getlowlevelData(String mbid, JSONObject data)  {
        JSONObject lowlevelData = data.getJSONObject("lowlevel");
        //System.out.println(lowlevelData);
        List<String> rowElements = new ArrayList<>();

        Object gender =  lowlevelData.get("dynamic_complexity");

        rowElements.add(gender.toString());
        Object acoustic = lowlevelData.getJSONObject("spectral_flux").get("mean");
        //System.out.println("dynamic complexity" + lowlevelData.getJSONObject("spectral_flux").get("mean"));
        rowElements.add(acoustic.toString());
        Object aggressive = lowlevelData.getJSONObject("spectral_skewness").get("mean");
        rowElements.add(aggressive.toString());
        Object electronic = lowlevelData.getJSONObject("spectral_decrease").get("mean");
        rowElements.add(electronic.toString());
        Object happy = lowlevelData.getJSONObject("spectral_energy").get("mean");
        rowElements.add(happy.toString());
        Object party = lowlevelData.getJSONObject("dissonance").get("mean");
        rowElements.add(party.toString());
        Object relaxed = lowlevelData.getJSONObject("spectral_entropy").get("mean");
        rowElements.add(relaxed.toString());
        Object sad = lowlevelData.getJSONObject("pitch_salience").get("mean");
        rowElements.add(sad.toString());
        Object timbre = lowlevelData.getJSONObject("spectral_complexity").get("mean");
        rowElements.add(timbre.toString());
        //System.out.println("hello1");
        JSONObject rhythmData = data.getJSONObject("rhythm");
        if(rhythmData != null) {
            Object beatCount = rhythmData.get("beats_count");
            rowElements.add(beatCount.toString());
            Object beatLoudness = rhythmData.getJSONObject("beats_loudness").get("mean");
            rowElements.add(beatLoudness.toString());
        }
        //System.out.println("hello2");

        //System.out.println(rowElements);
        return rowElements;
    }

    public void writeToCSV(List<String> songInfo) throws Exception {
        FileWriter csvWriter = new FileWriter("LowlevelDataset.csv", true);
        csvWriter.append(String.join(",", songInfo));
        csvWriter.append("\n");
        csvWriter.flush();
        csvWriter.close();

    }

    public void queryAcousticBrainz() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://acousticbrainz.org/8f8c186f-5a58-4a94-82a0-1d87d7d7ae2d/low-level"))
                .setHeader("Content-type", "application/json")
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.body());
    }


    public static void main(String[] args) throws Exception {
        QueryAPILowLevel queryAPI = new QueryAPILowLevel();
        queryAPI.getExistingSongs();
        queryAPI.readCSV("SongDataset2.csv");
        //queryAPI.queryAcousticBrainz();



    }
}
