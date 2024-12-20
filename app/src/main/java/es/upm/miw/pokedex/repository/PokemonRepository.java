package es.upm.miw.pokedex.repository;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import es.upm.miw.pokedex.R;
import es.upm.miw.pokedex.api.ApiClient;
import es.upm.miw.pokedex.api.PokeApiService;
import es.upm.miw.pokedex.api.Pokemon;
import es.upm.miw.pokedex.api.PokemonDetail;
import es.upm.miw.pokedex.api.PokemonResponse;
import es.upm.miw.pokedex.database.AppDatabase;
import es.upm.miw.pokedex.database.PokemonEntity;
import es.upm.miw.pokedex.MainActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PokemonRepository {
    private final PokeApiService apiService;
    private final AppDatabase db;
    private final MutableLiveData<List<PokemonDetail>> pokemonListLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isFetching = new MutableLiveData<>(false);
    private final Set<Integer> pokemonIds = new HashSet<>();
    private final Context context;
    private final AtomicInteger pendingFetches = new AtomicInteger(0);

    public PokemonRepository(Context context) {
        this.context = context;
        db = AppDatabase.getDatabase(context);
        apiService = ApiClient.getClient().create(PokeApiService.class);
    }

    public LiveData<List<PokemonDetail>> getPokemonList() {
        loadPokemonFromDatabase();
        return pokemonListLiveData;
    }

    public LiveData<Boolean> getIsFetching() {
        return isFetching;
    }

    private void loadPokemonFromDatabase() {
        new Thread(() -> {
            List<PokemonEntity> pokemonEntities = db.pokemonDao().getAllPokemon();
            if (pokemonEntities.isEmpty()) {
                fetchAllPokemon();
            } else {
                List<PokemonDetail> pokemonList = new ArrayList<>();
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
                pokemonListLiveData.postValue(pokemonList);
            }
        }).start();
    }

    public void fetchAllPokemon() {
        if (Boolean.TRUE.equals(isFetching.getValue())) {
            return;
        }
        isFetching.postValue(true);
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(context, R.string.fetching_pokemon, Toast.LENGTH_SHORT).show()
        );
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
                        pendingFetches.incrementAndGet();
                        fetchPokemonDetail(id);
                    }
                    removeMissingPokemon(apiPokemonIds);
                }
            }

            @Override
            public void onFailure(@NonNull Call<PokemonResponse> call, @NonNull Throwable t) {
                isFetching.postValue(false);
                // Handle failure
            }
        });
    }

    private void fetchPokemonDetail(int id) {
        apiService.getPokemonDetail(id).enqueue(new Callback<PokemonDetail>() {
            @Override
            public void onResponse(@NonNull Call<PokemonDetail> call, @NonNull Response<PokemonDetail> response) {
                if (response.isSuccessful()) {
                    PokemonDetail detail = response.body();
                    if (detail != null) {
                        if (!pokemonIds.contains(detail.getId()) || isPokemonDifferent(detail)) {
                            savePokemonToDatabase(detail);
                            if (!pokemonIds.contains(detail.getId())) {
                                List<PokemonDetail> currentList = pokemonListLiveData.getValue();
                                if (currentList == null) {
                                    currentList = new ArrayList<>();
                                }
                                currentList.add(detail);
                                pokemonIds.add(detail.getId());
                                sortPokemonList(currentList);
                                pokemonListLiveData.postValue(currentList);
                            } else {
                                updatePokemonInList(detail);
                            }
                        }
                    }
                }
                if (pendingFetches.decrementAndGet() == 0) {
                    isFetching.postValue(false);
                    restartApp();
                }
            }

            @Override
            public void onFailure(@NonNull Call<PokemonDetail> call, @NonNull Throwable t) {
                if (pendingFetches.decrementAndGet() == 0) {
                    isFetching.postValue(false);
                }
                // Handle failure
            }
        });
    }

    private boolean isPokemonDifferent(PokemonDetail detail) {
        List<PokemonDetail> currentList = pokemonListLiveData.getValue();
        if (currentList != null) {
            for (PokemonDetail localDetail : currentList) {
                if (localDetail.getId() == detail.getId()) {
                    return !localDetail.equals(detail);
                }
            }
        }
        return true;
    }

    private void updatePokemonInList(PokemonDetail detail) {
        List<PokemonDetail> currentList = pokemonListLiveData.getValue();
        if (currentList != null) {
            for (int i = 0; i < currentList.size(); i++) {
                if (currentList.get(i).getId() == detail.getId()) {
                    currentList.set(i, detail);
                    break;
                }
            }
            pokemonListLiveData.postValue(currentList);
        }
    }

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
            if (removed) {
                List<PokemonDetail> currentList = pokemonListLiveData.getValue();
                if (currentList != null) {
                    currentList.removeIf(pokemon -> !apiPokemonIds.contains(pokemon.getId()));
                    pokemonListLiveData.postValue(currentList);
                }
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

    private void sortPokemonList(List<PokemonDetail> pokemonList) {
        pokemonList.sort(Comparator.comparingInt(PokemonDetail::getId));
    }

    private void restartApp() {
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(context, R.string.pokemon_data_loaded, Toast.LENGTH_SHORT).show()
        );
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }
}