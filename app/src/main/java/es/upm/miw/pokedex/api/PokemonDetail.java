package es.upm.miw.pokedex.api;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PokemonDetail {
    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("sprites")
    private Sprites sprites;

    @SerializedName("types")
    private List<Type> types;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Sprites getSprites() {
        return sprites;
    }

    public List<Type> getTypes() {
        return types;
    }

    public class Sprites {
        @SerializedName("front_default")
        private String frontDefault;

        public String getFrontDefault() {
            return frontDefault;
        }
    }

    public class Type {
        @SerializedName("type")
        private TypeInfo typeInfo;

        public TypeInfo getTypeInfo() {
            return typeInfo;
        }

        public class TypeInfo {
            @SerializedName("name")
            private String name;

            public String getName() {
                return name;
            }
        }
    }
}
