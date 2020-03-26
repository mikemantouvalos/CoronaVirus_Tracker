package io.javabrains.coronavirustracker.services;

import io.javabrains.coronavirustracker.models.LocationStats;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


@Service
public class CoronaVirusDataService {

    private static String VIRUS_DATA_URL_CONFIRMED = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv";
    private static String VIRUS_DATA_URL_DEATHS = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_deaths_global.csv";

    private List<LocationStats> allStats = new ArrayList<>();
    private int finalDeaths ;
    private int finalCases;

    public List<LocationStats> getAllStats() {
        return allStats;
    }

    public int getFinalDeaths() {
        return finalDeaths;
    }

    public int getFinalCases() {
        return finalCases;
    }

    @PostConstruct
    @Scheduled(cron = "* * 1 * * *")

    public void fetchVirusData() throws IOException, InterruptedException {
        StringReader csvConfirmedBodyReader = getStringReaderFromURL(VIRUS_DATA_URL_CONFIRMED);
        StringReader csvDeathsBodyReader = getStringReaderFromURL(VIRUS_DATA_URL_DEATHS);
        ArrayList<CSVRecord> recordsConfirmedArrayList = getArrayListFromStringReader(csvConfirmedBodyReader);
        ArrayList<CSVRecord> recordsDeathsArrayList = getArrayListFromStringReader(csvDeathsBodyReader);
        int numberOfCountries = recordsConfirmedArrayList.size();
        printAndSaveStats(numberOfCountries, recordsConfirmedArrayList, recordsDeathsArrayList);

    }





    private StringReader getStringReaderFromURL(String request) throws IOException, InterruptedException {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(request))
                .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> httpResponse = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        return new StringReader(httpResponse.body());
    }


    private ArrayList<CSVRecord> getArrayListFromStringReader (StringReader stringReader) throws IOException {
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(stringReader);
        Collection<CSVRecord> recordsCollection = getCollectionFromIteralbe(records);
        return new ArrayList<>(recordsCollection);
    }

    private  <T> Collection<T> getCollectionFromIteralbe(Iterable<T> itr)
    {
        // Create an empty Collection to hold the result
        Collection<T> cltn = new ArrayList<T>();

        // Use iterable.forEach() to
        // Iterate through the iterable and
        // add each element into the collection
        itr.forEach(cltn::add);

        // Return the converted collection
        return cltn;
    }

    private void printAndSaveStats(int numberOfCountries, ArrayList<CSVRecord> confirmed, ArrayList<CSVRecord> deaths ){
        List<LocationStats> newStats = new ArrayList<>();
        for (int i = 0 ; i<numberOfCountries ; i++){
            LocationStats locationStat = new LocationStats();
            locationStat.setCountry(confirmed.get(i).get("Country/Region"));
            if(confirmed.get(i).get("Province/State").equals("")){
                locationStat.setState("-------");
            }else {
                locationStat.setState(confirmed.get(i).get("Province/State"));
            }
            if((confirmed.get(i).get(confirmed.get(i).size() -1).equals(""))){
                locationStat.setLatestTotalCases(0);
            }else{
                locationStat.setLatestTotalCases(Integer.parseInt(confirmed.get(i).get(confirmed.get(i).size() -1)));
            }
            if((deaths.get(i).get(deaths.get(i).size() -1).equals(""))){
                locationStat.setTotalDeaths(0);
            }else{
                locationStat.setTotalDeaths(Integer.parseInt(deaths.get(i).get(deaths.get(i).size() -1)));
            }
//            System.out.println(locationStat);
            newStats.add(locationStat);
        }
        this.allStats = newStats;
        this.finalCases= getTotalConfirmed();
        this.finalDeaths=getTotalDeaths();
    }

    private int getTotalConfirmed (){
        return this.allStats.stream()
                .mapToInt(LocationStats::getLatestTotalCases)
                .sum();
    }

    private int getTotalDeaths (){
        return this.allStats.stream()
                .mapToInt(LocationStats::getTotalDeaths)
                .sum();
    }


}


