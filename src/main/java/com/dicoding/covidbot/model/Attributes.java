package com.dicoding.covidbot.model;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class Attributes implements Serializable {
    private String FID;
    private String Kode_Provi;
    private String Provinsi;
    private String Kasus_Posi;
    private String Kasus_Semb;
    private String Kasus_Meni;

}
