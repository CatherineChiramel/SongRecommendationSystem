import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class to query the MusicBrainz API
 */
public class QueryMusicBrainz {
    protected List<String> existingSongs = new ArrayList<>();
    static File dataDirectory = new File("../../../../SongData/");
    static File resultDirectory = new File("../../../../Results/");

    /**
     * Get the songs for which data is already retrieved.
     */
    public void getExistingSongs() {
        try {
            BufferedReader csvReader = new BufferedReader(new FileReader("MusicBrainzEncoded.csv"));
            String columnHeadings = csvReader.readLine();
            String row;

            while((row = csvReader.readLine()) != null) {
                String[] rowElements = row.split(",");
                if(rowElements.length == 10)
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
            //String columnHeadings = csvReader.readLine();
            String row;
            while((row = csvReader.readLine()) != null) {
                //System.out.println(row);
                String[] songInfoArray = row.split(",");
                if(!this.existingSongs.contains(songInfoArray[2]))
                    this.getYear(row);



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
        String uri = "https://musicbrainz.org/ws/2/recording?query=" + songInfo.get(0) + "&fmt=json";
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .setHeader("Content-type", "application/json")
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JSONObject json = new JSONObject(response.body());
        //System.out.println(json);
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
                    //if(songInfo.get(1).toLowerCase().contains(artist.get("name").toString().toLowerCase())) {
                    if(artist.get("name").toString().toLowerCase().contains(songInfo.get(1).toLowerCase())) {
                        //System.out.println(songInfo.get(2) + ": " + artist.get("name") + ", " + (String) new JSONObject(object.toString()).get("id") );

                        songInfo.add((String) new JSONObject(object.toString()).get("id"));
                        break;
                    }
                }

            }
            if(songInfo.size() == 4)
                this.writeToCSV(songInfo);
        }


    }

    /**
     * method to get year information from the api
     * @param row
     * @throws Exception
     */
    public void getYear(String row) throws  Exception {
        String[] songInfoArray = row.split(",");
        List<String> songInfo = new ArrayList<String>(Arrays.asList(songInfoArray));
        String uri = "https://musicbrainz.org/ws/2/recording?query=" + songInfo.get(3) + "&fmt=json";
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .setHeader("Content-type", "application/json")
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JSONObject json = new JSONObject(response.body());
        //System.out.println(json);
        if(json.has("recordings")) {
            JSONArray recordings = json.getJSONArray("recordings");

            for(Object object: recordings) {
                //System.out.println(object.toString());
                JSONObject recording = new JSONObject(object.toString());
                System.out.println(recording);

                if(recording.has("date")) {
                    songInfo.add(recording.get("date").toString().split("-")[0]);
                    System.out.println(recording.get("date"));
                    //if(songInfo.get(1).toLowerCase().contains(artist.get("name").toString().toLowerCase())) {
                    //if(artist.get("name").toString().toLowerCase().contains(songInfo.get(1).toLowerCase())) {
                    //System.out.println(songInfo.get(2) + ": " + artist.get("name") + ", " + (String) new JSONObject(object.toString()).get("id") );

                    //songInfo.add((String) new JSONObject(object.toString()).get("id"));
                    break;
                    //}
                }

            }
            if(songInfo.size() == 11)
                this.writeToCSV(songInfo);
        }


    }

    /**
     * method to get the mbid of a record
     * @param row
     * @throws Exception
     */
    public void getRecordInfo(String row) throws  Exception {
        String[] rowElements = row.split(",");
        List<String> songInfo = new ArrayList<String>(Arrays.asList(rowElements));
       // System.out.println(songInfo.get(0));
        //String uri = "http://musicbrainz.org/ws/2/recording/?query=mbid:" + "a2cc114a-787c-4dcf-a563-b4549ffc2a48" + "&fmt=json";
        String uri = "https://musicbrainz.org/ws/2/recording/" + songInfo.get(3) + "?inc=artist-credits+isrcs+releases&fmt=json";
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .setHeader("Content-type", "application/json")
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JSONObject json = new JSONObject(response.body());
        //System.out.println(json);
        if(json.has("artist-credit")) {
            JSONArray artistCredits = (JSONArray) json.get("artist-credit");
            JSONObject artist;
            if (artistCredits.length() >0) {
                artist = (JSONObject) new JSONObject(artistCredits.get(0).toString()).get("artist");
                String artistMBID = artist.get("id").toString();
                songInfo.add(artistMBID);
            }
            else {
                songInfo.add("NA");
            }
        }
        if(json.has("releases")) {
            JSONArray releases = (JSONArray) json.get("releases");
            JSONObject validRelease1, validRelease2;
            if(releases.length() >0)
            {
                validRelease1 = (JSONObject) new JSONObject(releases.get(0).toString()).get("text-representation");
                String language = validRelease1.get("language").toString();
                songInfo.add(language);
                validRelease2 = (JSONObject) new JSONObject(releases.get(0).toString());
                String releaseId = validRelease2.get("id").toString();
                songInfo.add(releaseId);
            }

        }
        if(songInfo.size()== 9)
            this.writeToCSV(songInfo);
    }

    /**
     * method to get the release id of a song and the country in which it was released in
     * @param row
     * @throws Exception
     */
    public void getReleaseInfo (String row) throws Exception {
        String[] rowElements = row.split(",");
        List<String> songInfo = new ArrayList<String>(Arrays.asList(rowElements));
        // System.out.println(songInfo.get(0));
        //String uri = "http://musicbrainz.org/ws/2/recording/?query=mbid:" + "a2cc114a-787c-4dcf-a563-b4549ffc2a48" + "&fmt=json";
        String uri = "https://musicbrainz.org/ws/2/release/" + "987f3e2d-22a6-4a4f-b840-c80c26b8b91a" + "?inc=recording-level-rels+work-rels+work-level-rels+artist-rels&fmt=json";
        uri = "https://musicbrainz.org/ws/2/release/" + songInfo.get(6) + "?inc=recordings+recording-level-rels+work-level-rels+artist-rels&fmt=json";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .setHeader("Content-type", "application/json")
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JSONObject json = new JSONObject(response.body());
        //System.out.println(json);
        if(json.toString().length() < 3)
            return;
        if(json.has("country")) {
            songInfo.add(json.get("country").toString());
        }
        else {
            songInfo.add("NA");
        }
        if(json.has("media")) {
            String relatedRecords = "";

            JSONArray media = (JSONArray) json.get("media");
            JSONArray tracks = (JSONArray) new JSONObject(media.get(0).toString()).get("tracks");
            for(Object object: tracks) {
                JSONObject track = new JSONObject(object.toString());
                relatedRecords += track.get("title").toString() + "###";
            }
            relatedRecords = relatedRecords.replace(",", " ");
            if(relatedRecords.length() >0 )
                songInfo.add(relatedRecords);

        }
        else {
            songInfo.add("NA");
        }


        if(songInfo.size() == 9)
            this.writeToCSV(songInfo);




    }

    /**
     * method to get information about the artist of a song
     * @param row
     * @throws Exception
     */
    public void getArtistInfo(String row) throws Exception {
        String[] rowElements = row.split(",");
        List<String> songInfo = new ArrayList<String>(Arrays.asList(rowElements));
        // System.out.println(songInfo.get(0));
        //String uri = "http://musicbrainz.org/ws/2/recording/?query=mbid:" + "a2cc114a-787c-4dcf-a563-b4549ffc2a48" + "&fmt=json";
        String uri = "https://musicbrainz.org/ws/2/release/" + "987f3e2d-22a6-4a4f-b840-c80c26b8b91a" + "?inc=recording-level-rels+work-rels+work-level-rels+artist-rels&fmt=json";
        uri = "http://musicbrainz.org/ws/2/artist/" +  songInfo.get(4) +"?inc=aliases&fmt=json";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .setHeader("Content-type", "application/json")
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JSONObject json = new JSONObject(response.body());
        if(json.has("gender"))
            songInfo.add(json.get("gender").toString());

        if(songInfo.size() == 10)
            this.writeToCSV(songInfo);

    }

    public void writeToCSV(List<String> songInfo) throws Exception {
        FileWriter csvWriter = new FileWriter("Dataset/MusicBrainzEncodedFinal.csv", true);
        System.out.println(songInfo.get(0));
        csvWriter.append(String.join(",", songInfo));
        csvWriter.append("\n");
        csvWriter.flush();
        csvWriter.close();

    }

    public static void main(String[] args) throws Exception {
        QueryMusicBrainz musicBrainz = new QueryMusicBrainz();
        //musicBrainz.getExistingSongs();
        musicBrainz.readCSV("MusicBrainzEncoded.csv");

    }
}
