package com.dicoding.covidbot.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("FID")
    private String FID;
    private String Kode_Provi;
    private String Provinsi;
    private String Kasus_Posi;
    private String Kasus_Semb;
    private String Kasus_Meni;

}
