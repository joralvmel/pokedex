package es.upm.miw.pokedex;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import es.upm.miw.pokedex.api.ApiClient;
import es.upm.miw.pokedex.api.PokeApiService;
import es.upm.miw.pokedex.api.Pokemon;
import es.upm.miw.pokedex.api.PokemonDetail;
import es.upm.miw.pokedex.api.PokemonResponse;
import es.upm.miw.pokedex.db.AppDatabase;
import es.upm.miw.pokedex.db.PokemonEntity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PokemonListFragment extends Fragment {
    private PokeApiService apiService;
    private PokemonAdapter adapter;
    private final List<PokemonDetail> pokemonList = new ArrayList<>();
    private final List<PokemonDetail> filteredPokemonList = new ArrayList<>();
    private final Set<Integer> pokemonIds = new HashSet<>();
    private AppDatabase db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pokemon_list, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PokemonAdapter(filteredPokemonList);
        recyclerView.setAdapter(adapter);

        db = AppDatabase.getDatabase(getContext());
        apiService = ApiClient.getClient().create(PokeApiService.class);

        loadPokemonFromDatabase();

        SearchView searchView = view.findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return true;
            }
        });

        Button reloadButton = view.findViewById(R.id.reload_button);
        reloadButton.setOnClickListener(v -> fetchAllPokemon());

        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadPokemonFromDatabase() {
        new Thread(() -> {
            List<PokemonEntity> pokemonEntities = db.pokemonDao().getAllPokemon();
            if (pokemonEntities.isEmpty()) {
                fetchAllPokemon();
            } else {
                pokemonList.clear();
                for (PokemonEntity entity : pokemonEntities) {
                    PokemonDetail detail = new PokemonDetail();
                    detail.setId(entity.getId());
                    detail.setName(entity.getName());
                    if (detail.getSprites() == null) {
                        detail.setSprites(new PokemonDetail.Sprites());
                    }
                    detail.getSprites().setFrontDefault(entity.getFrontDefault());
                    if (detail.getTypes() == null) {
                        detail.setTypes(new ArrayList<>());
                    }
                    // Parse and set types
                    String[] typesArray = entity.getTypes().split(",");
                    for (String typeName : typesArray) {
                        PokemonDetail.Type type = new PokemonDetail.Type();
                        PokemonDetail.Type.TypeInfo typeInfo = new PokemonDetail.Type.TypeInfo();
                        typeInfo.setName(typeName);
                        type.setTypeInfo(typeInfo);
                        detail.getTypes().add(type);
                    }
                    pokemonList.add(detail);
                    pokemonIds.add(detail.getId());
                }
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        filteredPokemonList.clear();
                        filteredPokemonList.addAll(pokemonList);
                        adapter.notifyDataSetChanged();
                    });
                }
            }
        }).start();
    }

    private void fetchAllPokemon() {
        apiService.getAllPokemon().enqueue(new Callback<PokemonResponse>() {
            @Override
            public void onResponse(@NonNull Call<PokemonResponse> call, @NonNull Response<PokemonResponse> response) {
                if (response.isSuccessful()) {
                    List<Pokemon> pokemonList = Objects.requireNonNull(response.body()).getResults();
                    List<Integer> apiPokemonIds = new ArrayList<>();
                    for (Pokemon pokemon : pokemonList) {
                        String[] urlParts = pokemon.getUrl().split("/");
                        int id = Integer.parseInt(urlParts[urlParts.length - 1]);
                        apiPokemonIds.add(id);
                        fetchPokemonDetail(id);
                    }
                    removeMissingPokemon(apiPokemonIds);
                }
            }

            @Override
            public void onFailure(@NonNull Call<PokemonResponse> call, @NonNull Throwable t) {
                // Handle failure
            }
        });
    }

    private void fetchPokemonDetail(int id) {
        apiService.getPokemonDetail(id).enqueue(new Callback<PokemonDetail>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<PokemonDetail> call, @NonNull Response<PokemonDetail> response) {
                if (response.isSuccessful()) {
                    PokemonDetail detail = response.body();
                    if (detail != null) {
                        if (!pokemonIds.contains(detail.getId()) || isPokemonDifferent(detail)) {
                            savePokemonToDatabase(detail);
                            if (!pokemonIds.contains(detail.getId())) {
                                pokemonList.add(detail);
                                pokemonIds.add(detail.getId());
                            } else {
                                updatePokemonInList(detail);
                            }
                            sortPokemonList();
                            requireActivity().runOnUiThread(() -> {
                                filteredPokemonList.clear();
                                filteredPokemonList.addAll(pokemonList);
                                adapter.notifyDataSetChanged();
                            });
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<PokemonDetail> call, @NonNull Throwable t) {
                // Handle failure
            }
        });
    }

    private boolean isPokemonDifferent(PokemonDetail detail) {
        for (PokemonDetail localDetail : pokemonList) {
            if (localDetail.getId() == detail.getId()) {
                return !localDetail.equals(detail);
            }
        }
        return true;
    }

    private void updatePokemonInList(PokemonDetail detail) {
        for (int i = 0; i < pokemonList.size(); i++) {
            if (pokemonList.get(i).getId() == detail.getId()) {
                pokemonList.set(i, detail);
                break;
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void removeMissingPokemon(List<Integer> apiPokemonIds) {
        new Thread(() -> {
            List<PokemonEntity> localPokemon = db.pokemonDao().getAllPokemon();
            boolean removed = false;
            for (PokemonEntity entity : localPokemon) {
                if (!apiPokemonIds.contains(entity.getId())) {
                    db.pokemonDao().delete(entity);
                    removed = true;
                }
            }
            if (removed && isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    pokemonList.removeIf(pokemon -> !apiPokemonIds.contains(pokemon.getId()));
                    filteredPokemonList.clear();
                    filteredPokemonList.addAll(pokemonList);
                    adapter.notifyDataSetChanged();
                });
            }
        }).start();
    }

    private void savePokemonToDatabase(PokemonDetail detail) {
        new Thread(() -> {
            PokemonEntity entity = new PokemonEntity(
                    detail.getId(),
                    detail.getName(),
                    detail.getSprites().getFrontDefault(),
                    detail.getTypes().stream().map(type -> type.getTypeInfo().getName()).collect(Collectors.joining(","))
            );
            db.pokemonDao().insertAll(Collections.singletonList(entity));
        }).start();
    }

    private void sortPokemonList() {
        pokemonList.sort(Comparator.comparingInt(PokemonDetail::getId));
    }

    @SuppressLint("NotifyDataSetChanged")
    private void filter(String text) {
        filteredPokemonList.clear();
        if (text.isEmpty()) {
            filteredPokemonList.addAll(pokemonList);
        } else {
            for (PokemonDetail detail : pokemonList) {
                if (detail.getName().toLowerCase().contains(text.toLowerCase())) {
                    filteredPokemonList.add(detail);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }
}