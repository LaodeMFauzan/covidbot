package com.dicoding.covidbot.service;

import com.dicoding.covidbot.adapter.KawalCoronaAdaptor;
import com.dicoding.covidbot.model.CoronaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public String showIndonesianAllCovidCasesData(){
        CoronaData[] indonesianCoronaData = kawalCoronaAdaptor.getIndonesiaCovidData();
        return  "Total Kasus Covid19 di Indonesia \n" +
                "\nPositif: " +indonesianCoronaData[0].getPositif() +
                "\nMeninggal: "+ indonesianCoronaData[0].getMeninggal() +
                "\nSembuh: "+ indonesianCoronaData[0].getSembuh() +
                "\nDirawat: "+ indonesianCoronaData[0].getDirawat();
    }
}
