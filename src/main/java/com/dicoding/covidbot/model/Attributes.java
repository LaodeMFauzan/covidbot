package com.dicoding.covidbot.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties
public class Attributes implements Serializable {
    private String FID;
    private String Kode_Provi;
    private String Provinsi;
    private String Kasus_Posi;
    private String Kasus_Semb;
    private String Kasus_Meni;

}
