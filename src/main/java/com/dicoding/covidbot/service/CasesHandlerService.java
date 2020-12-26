package com.dicoding.covidbot.service;

import com.dicoding.covidbot.adapter.KawalCoronaAdaptor;
import com.dicoding.covidbot.model.CoronaData;
import com.dicoding.covidbot.model.ProvinceCovidData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CasesHandlerService {

    @Autowired
    private KawalCoronaAdaptor kawalCoronaAdaptor;

    @Autowired
    private BotService botService;

    public void handleCovidCasesRequest(String replyToken){
        String askRegionText ="Silahkan ketik nama provinsi atau ketik Indonesia untuk kasus seluruh Indonesia";
        botService.replyText(replyToken, askRegionText);
    }

    public String getIndonesianAllCovidCases(){
        CoronaData[] indonesianCoronaData = kawalCoronaAdaptor.getIndonesiaCovidData();
        return  "Total Kasus Covid19 di Indonesia \n" +
                "\nPositif: " +indonesianCoronaData[0].getPositif() +
                "\nMeninggal: "+ indonesianCoronaData[0].getMeninggal() +
                "\nSembuh: "+ indonesianCoronaData[0].getSembuh() +
                "\nDirawat: "+ indonesianCoronaData[0].getDirawat();
    }

    public void getProvinceCovidCases(){
        LinkedHashMap<String, ProvinceCovidData>[] coronaData = kawalCoronaAdaptor.getCovidDataOnProvince();
        coronaData[0].forEach((s, provinceCovidData) -> {
            System.out.println("THIS IS PROVINCE"+ provinceCovidData.getAttributes().getProvinsi());
        });
    }
}
