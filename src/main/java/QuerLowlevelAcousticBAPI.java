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


public class QuerLowlevelAcousticBAPI {
    List<String> existingSongs = new ArrayList<>();
    /**
     * Get the songs for which data is already retrieved.
     */
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
                    //if(artist.get("name").equals(songInfo.get(1))) {
                        //System.out.println(songInfo.get(2) + ": " + artist.get("name") + ", " + (String) new JSONObject(object.toString()).get("id") );
                        mbids.add((String) new JSONObject(object.toString()).get("id"));
                    //}
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

    /**
     * Method to get the low-level information of songs from acousticbrainz api
     * @param mbid
     * @param data
     * @return
     * @throws Exception
     */

    public List<String> getlowlevelData(String mbid, JSONObject data)  {
        JSONObject lowlevelData = data.getJSONObject("lowlevel");
        //System.out.println(lowlevelData);
        List<String> rowElements = new ArrayList<>();

        Object dynamicComplexity =  lowlevelData.get("dynamic_complexity");

        rowElements.add(dynamicComplexity.toString());
        Object spectralFlux = lowlevelData.getJSONObject("spectral_flux").get("mean");
        //System.out.println("dynamic complexity" + lowlevelData.getJSONObject("spectral_flux").get("mean"));
        rowElements.add(spectralFlux.toString());
        Object spectralSkewness = lowlevelData.getJSONObject("spectral_skewness").get("mean");
        rowElements.add(spectralSkewness.toString());
        Object silenceRate = lowlevelData.getJSONObject("silence_rate_20db").get("mean");
        rowElements.add(spectralSkewness.toString());
        Object spectralDecrease = lowlevelData.getJSONObject("spectral_decrease").get("mean");
        rowElements.add(spectralDecrease.toString());
        Object spectralEnergy = lowlevelData.getJSONObject("spectral_energy").get("mean");
        rowElements.add(spectralEnergy.toString());
        Object hcf = lowlevelData.getJSONObject("hcf").get("mean");
        rowElements.add(hcf.toString());
        Object dissonance = lowlevelData.getJSONObject("dissonance").get("mean");
        rowElements.add(dissonance.toString());
        Object spectralEntropy = lowlevelData.getJSONObject("spectral_entropy").get("mean");
        rowElements.add(spectralEntropy.toString());
        Object pitchSalience = lowlevelData.getJSONObject("pitch_salience").get("mean");
        rowElements.add(pitchSalience.toString());
        Object spectralComplexity = lowlevelData.getJSONObject("spectral_complexity").get("mean");
        rowElements.add(spectralComplexity.toString());
        //System.out.println("hello1");
        JSONObject rhythmData = data.getJSONObject("rhythm");
        if(rhythmData != null) {
            Object beatCount = rhythmData.get("beats_count");
            rowElements.add(beatCount.toString());
            Object beatLoudness = rhythmData.getJSONObject("beats_loudness").get("mean");
            rowElements.add(beatLoudness.toString());
        }

        return rowElements;
    }

    /**
     * Method to write the collected data to a csv file
     * @param songInfo
     * @throws Exception
     */

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
        QuerLowlevelAcousticBAPI queryAPI = new QuerLowlevelAcousticBAPI();
        queryAPI.getExistingSongs();
        queryAPI.readCSV("SongDataset2.csv");

    }
}
