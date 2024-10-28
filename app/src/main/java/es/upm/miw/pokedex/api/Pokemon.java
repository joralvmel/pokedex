package es.upm.miw.pokedex.api;

import com.google.gson.annotations.SerializedName;

public class Pokemon {
    @SerializedName("name")
    private String name;

    @SerializedName("url")
    private String url;

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }
}
