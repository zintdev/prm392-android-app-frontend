package com.example.prm392_android_app_frontend.data.dto;

import java.io.Serializable;

public class ArtistDto implements Serializable {
    public Integer id;
    public String artistType;
    public String artistName;
    public Integer debutYear;

    public String getArtistName(){return artistName; }

    public Integer getId(){return id; }
    public String getArtistType(){return artistType; }
    public Integer getDebutYear(){return debutYear; }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setArtistType(String artistType) {
        this.artistType = artistType;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public void setDebutYear(Integer debutYear) {
        this.debutYear = debutYear;
    }

    public ArtistDto(String artistType, String artistName, Integer debutYear) {
        this.artistType = artistType;
        this.artistName = artistName;
        this.debutYear = debutYear;
    }

    public ArtistDto() {
    }
}
