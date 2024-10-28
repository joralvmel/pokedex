package es.upm.miw.pokedex;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import es.upm.miw.pokedex.api.ApiClient;
import es.upm.miw.pokedex.api.PokeApiService;
import es.upm.miw.pokedex.api.Pokemon;
import es.upm.miw.pokedex.api.PokemonDetail;
import es.upm.miw.pokedex.api.PokemonResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private PokeApiService apiService;
    private PokemonAdapter adapter;
    private List<PokemonDetail> pokemonList = new ArrayList<>();
    private int fetchedPokemonCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PokemonAdapter(pokemonList);
        recyclerView.setAdapter(adapter);

        apiService = ApiClient.getClient().create(PokeApiService.class);
        fetchFirst150Pokemon();
    }

    private void fetchFirst150Pokemon() {
        apiService.getFirst150Pokemon().enqueue(new Callback<PokemonResponse>() {
            @Override
            public void onResponse(Call<PokemonResponse> call, Response<PokemonResponse> response) {
                if (response.isSuccessful()) {
                    List<Pokemon> pokemonList = response.body().getResults();
                    for (Pokemon pokemon : pokemonList) {
                        String[] urlParts = pokemon.getUrl().split("/");
                        int id = Integer.parseInt(urlParts[urlParts.length - 1]);
                        fetchPokemonDetail(id);
                    }
                }
            }

            @Override
            public void onFailure(Call<PokemonResponse> call, Throwable t) {
                // Handle failure
            }
        });
    }

    private void fetchPokemonDetail(int id) {
        apiService.getPokemonDetail(id).enqueue(new Callback<PokemonDetail>() {
            @Override
            public void onResponse(Call<PokemonDetail> call, Response<PokemonDetail> response) {
                if (response.isSuccessful()) {
                    PokemonDetail detail = response.body();
                    pokemonList.add(detail);
                    fetchedPokemonCount++;
                    if (fetchedPokemonCount == 150) {
                        sortPokemonList();
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onFailure(Call<PokemonDetail> call, Throwable t) {
                // Handle failure
            }
        });
    }

    private void sortPokemonList() {
        Collections.sort(pokemonList, new Comparator<PokemonDetail>() {
            @Override
            public int compare(PokemonDetail p1, PokemonDetail p2) {
                return Integer.compare(p1.getId(), p2.getId());
            }
        });
    }
}