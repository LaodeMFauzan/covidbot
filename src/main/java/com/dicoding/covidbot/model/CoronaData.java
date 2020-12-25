package com.dicoding.covidbot.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class CoronaData implements Serializable {
    private String name;

    private String positif;

    private String sembuh;

    private String meninggal;

    private String dirawat;

}
