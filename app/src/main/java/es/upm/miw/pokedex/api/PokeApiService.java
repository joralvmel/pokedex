package es.upm.miw.pokedex.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface PokeApiService {
    @GET("pokemon?limit=150")
    Call<PokemonResponse> getFirst150Pokemon();

    @GET("pokemon/{id}")
    Call<PokemonDetail> getPokemonDetail(@Path("id") int id);
}