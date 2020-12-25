package com.dicoding.covidbot.model;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
public class CoronaDataListResponse implements Serializable {

    private List<CoronaData> coronaDataList;
}
