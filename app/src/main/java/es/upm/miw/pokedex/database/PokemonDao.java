package es.upm.miw.pokedex.database;

import androidx.room.Dao;
import androidx.room.Delete;
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

    @Delete
    void delete(PokemonEntity pokemonEntity);
}