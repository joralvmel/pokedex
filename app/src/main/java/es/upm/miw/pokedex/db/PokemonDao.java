package es.upm.miw.pokedex.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PokemonDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<PokemonEntity> pokemonList);

    @Query("SELECT * FROM pokemon ORDER BY id")
    List<PokemonEntity> getAllPokemon();
}