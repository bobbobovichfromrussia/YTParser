import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyTest {
    //Google code from: https://github.com/youtube/api-samples/tree/master/java
    private static final String CLIENT_SECRETS= "client_secret.json";
    private static final Collection<String> SCOPES =
            Arrays.asList("https://www.googleapis.com/auth/youtube.readonly");

    private static final String APPLICATION_NAME = "API code samples";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    public static Credential authorize(final NetHttpTransport httpTransport) throws IOException {
        // Load client secrets.
        InputStream in = MyTest.class.getResourceAsStream(CLIENT_SECRETS);
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                        .build();
        Credential credential =
                new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
        return credential;
    }

    public static YouTube getService() throws GeneralSecurityException, IOException {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        Credential credential = authorize(httpTransport);
        return new YouTube.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    //My Code:
    public static void execute(String path, String updatedPath)
            throws GeneralSecurityException, IOException, GoogleJsonResponseException {
        Parser parser = new Parser();
        parser.parse(path,updatedPath);
        Iterable<CSVRecord> csvRecords = parser.getCsvRecords();
        CSVPrinter csvPrinter = parser.getCsvPrinter();
        YouTube youtubeService = getService();

        int i=1;
        for(CSVRecord record : csvRecords){
            try{
                String channel_id = record.get(Parser.getChannelID());
                String description = getLatestVideoDescription(channel_id,youtubeService);
                List<String> links = getLinks(description);

                String conc_links = "";
                for(String link : links){
                    conc_links += link + "\n";
                }

                csvPrinter.printRecord(record.get(Parser.getChannelName()),record.get(Parser.getChannelLink()),record.get(Parser.getChannelID()),conc_links);
                csvPrinter.flush();
            }catch (IOException e){
                System.out.println("FAIL: " + i + " " + record.get(Parser.getChannelName()));
            }
            System.out.println("Done: " + i);
            i++;
        }
        //"CHANNEL_NAME", "CHANNEL_LINK", "CHANNEL_ID", "Latest Video Description Links"
    }

    private static String getLatestVideoDescription(String channel_id, YouTube youtubeService) throws GeneralSecurityException, IOException {

        YouTube.Channels.List request = youtubeService.channels()
                .list("snippet,contentDetails");
        ChannelListResponse response = request.setId(channel_id).execute();

        //Get channel
        List<Channel> channels = response.getItems();
        Channel channel = channels.get(0);

        //Get "uploads" playlist ID
        ChannelContentDetails contentDetails = channel.getContentDetails();
        ChannelContentDetails.RelatedPlaylists relatedPlaylists = contentDetails.getRelatedPlaylists();
        String uploadsID = relatedPlaylists.getUploads();

        //Get contentDetails and snippet of latest upload
        YouTube.PlaylistItems.List playListRequest = youtubeService.playlistItems()
                .list("contentDetails,snippet");
        PlaylistItemListResponse playlistItemListResponse = playListRequest
                .setMaxResults(1L)
                .setPlaylistId(uploadsID)
                .execute();

        //Get description from latest upload
        List<PlaylistItem> latestVideos = playlistItemListResponse.getItems();
        PlaylistItem latestVideo = latestVideos.get(0);
        String description = latestVideo.getSnippet().getDescription();
        return description;
    }

    private static List<String> getLinks(String description){
        List<String> allMatches = new ArrayList<String>();
        Matcher m = Pattern.compile("(http|ftp|https)://([\\w_-]+(?:(?:\\.[\\w_-]+)+))([\\w.,@?^=%&:/~+#-]*[\\w@?^=%&/~+#-])?")
                .matcher(description);
        while (m.find()) {
            allMatches.add(m.group());
        }
        return allMatches;
    }
}


/*
public static void main(String[] args)
            throws GeneralSecurityException, IOException, GoogleJsonResponseException {
        String path = "C:/Users/druma/Downloads/list.csv";
        String updatedPath = "C:/Users/druma/Downloads/list-updated.csv";

        Parser parser = new Parser();
        parser.parse(path,updatedPath);
        Iterable<CSVRecord> csvRecords = parser.getCsvRecords();
        CSVPrinter csvPrinter = parser.getCsvPrinter();
        YouTube youtubeService = getService();

        int i=1;
        for(CSVRecord record : csvRecords){
            try{
                String channel_id = record.get(Parser.getChannelID());
                String description = getLatestVideoDescription(channel_id,youtubeService);
                List<String> links = getLinks(description);

                String conc_links = "";
                for(String link : links){
                    conc_links += link + "\n";
                }

                csvPrinter.printRecord(record.get(Parser.getChannelName()),record.get(Parser.getChannelLink()),record.get(Parser.getChannelID()),conc_links);
                csvPrinter.flush();
            }catch (IOException e){
                System.out.println("FAIL: " + i + " " + record.get(Parser.getChannelName()));
            }
            i++;
        }

        //"CHANNEL_NAME", "CHANNEL_LINK", "CHANNEL_ID", "Latest Video Description Links"

    }
 */


/*
NB:

https://www.googleapis.com/youtube/v3/search?key={your_key_here}&channelId={channel_id_here}&part=snippet,id&order=date&maxResults=20

 */