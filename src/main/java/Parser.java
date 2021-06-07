import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.nio.file.*;

public class Parser {
    private static final int CHANNEL_NAME = 0;
    private static final int CHANNEL_LINK = 1;
    private static final int CHANNEL_ID = 2;

    private Iterable<CSVRecord> csvRecords;
    private CSVPrinter csvPrinter;

    public Parser(){
        csvRecords = null;
        csvPrinter = null;
    }

    public void parse(String path, String updatedPath){
        try {
            Reader in = new FileReader(path);
            csvRecords = CSVFormat.EXCEL.parse(in);

//            Path update = Path.of(updatedPath);
//            BufferedWriter stringWriter = new BufferedWriter(Files.newBufferedWriter(update));
//            csvPrinter = new CSVPrinter(stringWriter, CSVFormat.DEFAULT.withHeader("CHANNEL_NAME", "CHANNEL_LINK", "CHANNEL_ID", "Latest Video Description Links"));


            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(updatedPath));
            csvPrinter = new CSVPrinter(bufferedWriter,CSVFormat.DEFAULT.withHeader("CHANNEL_NAME", "CHANNEL_LINK", "CHANNEL_ID", "Latest Video Description Links"));
        } catch (FileNotFoundException e) {
            System.out.println("FILE NOT FOUND");
        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.out.println("IOEXCEPTION");
        }
    }



    public Iterable<CSVRecord> getCsvRecords(){
        return csvRecords;
    }

    public CSVPrinter getCsvPrinter(){
        return csvPrinter;
    }

    public static int getChannelName(){
        return CHANNEL_NAME;
    }

    public static int getChannelLink(){
        return CHANNEL_LINK;
    }

    public static int getChannelID(){
        return CHANNEL_ID;
    }


    //Used to generate the YouTube channel IDs from links
    public void generateIDs(){
        int number = 1;
        for(CSVRecord record : csvRecords){
            String link = record.get(CHANNEL_LINK);

            if(link.contains("/channel")){
                link = link.replace("https://www.youtube.com/channel/","");
                if(link.contains("/")) link = link.replaceFirst("/[a-zA-Z0-9/]*","");
            } else {
                link = "";
            }

            try{
                csvPrinter.printRecord(record.get(CHANNEL_NAME),record.get(CHANNEL_LINK),record.get(CHANNEL_ID),link);
                System.out.println(number);

                csvPrinter.flush();
            } catch (IOException e) {
                System.out.println("FAILED: " + record.get(number));
            }

            number++;
        }
    }
}
