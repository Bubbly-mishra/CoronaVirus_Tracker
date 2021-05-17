package com.Bubbly.CoronaVirus_Tracker.services;

import com.Bubbly.CoronaVirus_Tracker.models.LocationsStats;
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
import java.util.List;

@Service
public class CoronaVirusDataService {

    private static String Virus_Data_URl = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv";

    private List<LocationsStats> allStats = new ArrayList<>();

    public List<LocationsStats> getAllStats() {
        return allStats;
    }

    @PostConstruct
    @Scheduled(cron = "* * 1 * * *")
    public void fetchVirusData() throws IOException, InterruptedException
    {
        List<LocationsStats> newStats = new ArrayList<>();

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Virus_Data_URl))
                .build();
      HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
        StringReader csvBodyReader = new StringReader(httpResponse.body());
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvBodyReader);

        for (CSVRecord record : records) {

            LocationsStats locationsStat = new LocationsStats();
            locationsStat.setState(record.get("Province/State"));
            locationsStat.setCountry(record.get("Country/Region"));
            int latestCases = Integer.parseInt(record.get(record.size() - 1));
            int preDayCases = Integer.parseInt(record.get(record.size() - 2));
            locationsStat.setLatestTotalCases(latestCases);
            locationsStat.setDiffFromPreDay(latestCases - preDayCases);
            newStats.add(locationsStat);

        }
       this.allStats = newStats;
    }
}
