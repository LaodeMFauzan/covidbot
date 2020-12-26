package com.dicoding.covidbot.adapter;

import com.dicoding.covidbot.model.Attributes;
import com.dicoding.covidbot.model.CoronaData;
import com.dicoding.covidbot.model.ListCases;
import com.dicoding.covidbot.model.ProvinceCovidData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Component
public class KawalCoronaAdaptor {

    public CoronaData[] getIndonesiaCovidData() {
        CoronaData[] coronaDataListResponse = new CoronaData[0];
        String uri = "https://api.kawalcorona.com/indonesia";
        System.out.println("URI: " + uri);

        String jsonResponse = doApiCall(uri);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            coronaDataListResponse = objectMapper.readValue(jsonResponse, CoronaData[].class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return coronaDataListResponse;
    }

    public ListCases[] getCovidDataOnProvince() {
        ListCases attributes[] = new ListCases[35];
        String uri = "https://api.kawalcorona.com/indonesia/provinsi";
        System.out.println("URI: " + uri);

        String jsonResponse = doApiCall(uri);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            attributes = objectMapper.readValue(jsonResponse, ListCases[].class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return attributes;
    }

    private String doApiCall(String URI) {
        String jsonResponse = null;
        try (CloseableHttpAsyncClient client = HttpAsyncClients.createDefault()) {
            client.start();
            //Use HTTP Get to retrieve data
            HttpGet get = new HttpGet(URI);

            Future<HttpResponse> future = client.execute(get, null);
            HttpResponse responseGet = future.get();
            System.out.println("HTTP executed");
            System.out.println("HTTP Status of response: " + responseGet.getStatusLine().getStatusCode());

            // Get the response from the GET request
            InputStream inputStream = responseGet.getEntity().getContent();
            String encoding = StandardCharsets.UTF_8.name();
            jsonResponse = IOUtils.toString(inputStream, encoding);

            System.out.println("Got result");
            System.out.println(jsonResponse);
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return jsonResponse;
    }
}

