package com.dicoding.covidbot.model;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class ProvinceCovidData implements Serializable {
    private Attributes attributes;
}
