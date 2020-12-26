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
    public int fID;
    @JsonProperty("Kode_Provi")
    public int kode_Provi;
    @JsonProperty("Provinsi")
    public String provinsi;
    @JsonProperty("Kasus_Posi")
    public int kasus_Posi;
    @JsonProperty("Kasus_Semb")
    public int kasus_Semb;
    @JsonProperty("Kasus_Meni")
    public int kasus_Meni;
}
