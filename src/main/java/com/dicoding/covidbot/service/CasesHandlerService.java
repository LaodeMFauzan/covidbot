package com.dicoding.covidbot.service;

import com.dicoding.covidbot.adapter.KawalCoronaAdaptor;
import com.dicoding.covidbot.model.CoronaData;
import com.dicoding.covidbot.model.ProvinceCovidData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
        List<ProvinceCovidData> coronaData = kawalCoronaAdaptor.getCovidDataOnProvince();
        coronaData.forEach(provinceCovidData -> {
            System.out.println(provinceCovidData.getAttributes().getProvinsi());
        });
    }
}
