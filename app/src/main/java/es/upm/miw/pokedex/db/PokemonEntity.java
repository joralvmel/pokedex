package es.upm.miw.pokedex.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "pokemon")
public class PokemonEntity {
    @PrimaryKey
    private int id;
    private String name;
    private final String frontDefault;
    private final String types;

    public PokemonEntity(int id, String name, String frontDefault, String types) {
        this.id = id;
        this.name = name;
        this.frontDefault = frontDefault;
        this.types = types;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getFrontDefault() {
        return frontDefault;
    }

    public String getTypes() {
        return types;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    @NonNull
    @Override
    public String toString() {
        return "PokemonEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", frontDefault='" + frontDefault + '\'' +
                ", types='" + types + '\'' +
                '}';
    }
}