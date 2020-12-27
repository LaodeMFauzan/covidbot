package com.dicoding.covidbot.service;

import com.dicoding.covidbot.adapter.KawalCoronaAdaptor;
import com.dicoding.covidbot.model.Attributes;
import com.dicoding.covidbot.model.CoronaData;
import com.dicoding.covidbot.model.ListCases;
import com.dicoding.covidbot.model.ProvinceCovidData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.util.*;
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

    public Map<String, Attributes> getProvinceCovidCases(){
       ListCases[] coronaData = kawalCoronaAdaptor.getCovidDataOnProvince();
       Map<String, Attributes> provinceCaseMap = new HashMap<>();
        Arrays.stream(coronaData).forEach(cases -> {
            provinceCaseMap.put(cases.getAttributes().getProvinsi().toLowerCase(), cases.getAttributes());
        });
        return provinceCaseMap;
    }

    public String getProvinceCovidCases(Map<String, Attributes> provinceCaseMap, String province){
        NumberFormat formatter = NumberFormat.getCurrencyInstance();

        return  "Total Kasus Covid19 di "+province +"\n"+
                "\nPositif: " +formatter.format(provinceCaseMap.get(province).getKasus_Posi()) +
                "\nMeninggal: "+ formatter.format(provinceCaseMap.get(province).getKasus_Meni()) +
                "\nSembuh: "+ formatter.format(provinceCaseMap.get(province).getKasus_Semb());
    }
}
