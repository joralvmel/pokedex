package es.upm.miw.pokedex.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.util.ArrayList;
import java.util.List;

import es.upm.miw.pokedex.api.PokemonDetail;
import es.upm.miw.pokedex.repository.PokemonRepository;

public class PokemonViewModel extends AndroidViewModel {
    private final PokemonRepository repository;
    private final LiveData<List<PokemonDetail>> pokemonList;
    private final MutableLiveData<String> currentSearchText = new MutableLiveData<>("");
    private final MutableLiveData<String> currentType = new MutableLiveData<>("All");
    private final MutableLiveData<String> currentGeneration = new MutableLiveData<>("National");

    public PokemonViewModel(@NonNull Application application) {
        super(application);
        repository = new PokemonRepository(application);
        pokemonList = repository.getPokemonList();
    }

    public void setCurrentSearchText(String searchText) {
        currentSearchText.setValue(searchText);
    }

    public void setCurrentType(String type) {
        currentType.setValue(type);
    }

    public void setCurrentGeneration(String generation) {
        currentGeneration.setValue(generation);
    }

    public LiveData<List<PokemonDetail>> getFilteredPokemonList() {
        return Transformations.switchMap(currentSearchText, searchText ->
                Transformations.switchMap(currentType, type ->
                        Transformations.switchMap(currentGeneration, generation ->
                                Transformations.map(pokemonList, pokemonDetails -> {
                                    List<PokemonDetail> filtered = new ArrayList<>();
                                    for (PokemonDetail detail : pokemonDetails) {
                                        if (matchesSearchText(detail, searchText) && matchesType(detail, type) && matchesGeneration(detail, generation)) {
                                            detail.setName(capitalizeFirstLetter(detail.getName()));
                                            filtered.add(detail);
                                        }
                                    }
                                    return filtered;
                                })
                        )
                )
        );
    }

    private boolean matchesSearchText(PokemonDetail detail, String searchText) {
        return searchText == null || searchText.isEmpty() || detail.getName().toLowerCase().contains(searchText.toLowerCase());
    }

    private boolean matchesType(PokemonDetail detail, String type) {
        if (type == null || type.equals("All")) {
            return true;
        }
        for (PokemonDetail.Type pokemonType : detail.getTypes()) {
            if (pokemonType.getTypeInfo().getName().equalsIgnoreCase(type)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesGeneration(PokemonDetail detail, String generation) {
        int startId = 1, endId = 1025;
        if (generation != null) {
            switch (generation) {
                case "Generation 1":
                    endId = 151;
                    break;
                case "Generation 2":
                    startId = 152;
                    endId = 251;
                    break;
                case "Generation 3":
                    startId = 252;
                    endId = 386;
                    break;
                case "Generation 4":
                    startId = 387;
                    endId = 493;
                    break;
                case "Generation 5":
                    startId = 494;
                    endId = 649;
                    break;
                case "Generation 6":
                    startId = 650;
                    endId = 721;
                    break;
                case "Generation 7":
                    startId = 722;
                    endId = 809;
                    break;
                case "Generation 8":
                    startId = 810;
                    endId = 905;
                    break;
                case "Generation 9":
                    startId = 906;
                    break;
            }
        }
        return detail.getId() >= startId && detail.getId() <= endId;
    }

    private String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    public void fetchAllPokemon() {
        repository.fetchAllPokemon();
    }
}