package com.dicoding.covidbot.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties
@Data
public class ListCases {
    private Attributes attributes;
}
