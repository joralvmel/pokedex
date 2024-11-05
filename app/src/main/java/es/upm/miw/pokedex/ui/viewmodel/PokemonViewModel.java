package es.upm.miw.pokedex.ui.viewmodel;

import android.app.Application;
import android.util.SparseBooleanArray;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
    private final MutableLiveData<String> currentFavoriteSelection = new MutableLiveData<>("All");
    private final SparseBooleanArray favoriteStatusArray = new SparseBooleanArray();

    public PokemonViewModel(@NonNull Application application) {
        super(application);
        repository = new PokemonRepository(application);
        pokemonList = repository.getPokemonList();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("favorites");
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            databaseReference.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    favoriteStatusArray.clear();
                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                        int pokemonId = Integer.parseInt(childSnapshot.getKey());
                        boolean isFavorite = Boolean.TRUE.equals(childSnapshot.getValue(Boolean.class));
                        favoriteStatusArray.put(pokemonId, isFavorite);
                    }
                    filterPokemonList();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle possible errors.
                }
            });
        }
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

    public void setCurrentFavoriteSelection(String favoriteSelection) {
        currentFavoriteSelection.setValue(favoriteSelection);
        filterPokemonList();
    }

    public LiveData<List<PokemonDetail>> getFilteredPokemonList() {
        return Transformations.switchMap(currentSearchText, searchText ->
                Transformations.switchMap(currentType, type ->
                        Transformations.switchMap(currentGeneration, generation ->
                                Transformations.switchMap(currentFavoriteSelection, favoriteSelection ->
                                        Transformations.map(pokemonList, pokemonDetails -> {
                                            List<PokemonDetail> filtered = new ArrayList<>();
                                            for (PokemonDetail detail : pokemonDetails) {
                                                if (matchesSearchText(detail, searchText) && matchesType(detail, type) && matchesGeneration(detail, generation) && matchesFavoriteSelection(detail, favoriteSelection)) {
                                                    detail.setName(capitalizeFirstLetter(detail.getName()));
                                                    filtered.add(detail);
                                                }
                                            }
                                            return filtered;
                                        })
                                )
                        )
                )
        );
    }

    public LiveData<Boolean> getIsFetching() {
        return repository.getIsFetching();
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
                case "Gen 1":
                    endId = 151;
                    break;
                case "Gen 2":
                    startId = 152;
                    endId = 251;
                    break;
                case "Gen 3":
                    startId = 252;
                    endId = 386;
                    break;
                case "Gen 4":
                    startId = 387;
                    endId = 493;
                    break;
                case "Gen 5":
                    startId = 494;
                    endId = 649;
                    break;
                case "Gen 6":
                    startId = 650;
                    endId = 721;
                    break;
                case "Gen 7":
                    startId = 722;
                    endId = 809;
                    break;
                case "Gen 8":
                    startId = 810;
                    endId = 905;
                    break;
                case "Gen 9":
                    startId = 906;
                    break;
            }
        }
        return detail.getId() >= startId && detail.getId() <= endId;
    }

    private boolean matchesFavoriteSelection(PokemonDetail detail, String favoriteSelection) {
        if (favoriteSelection == null || favoriteSelection.equals("All")) {
            return true;
        }
        boolean isFavorite = favoriteStatusArray.get(detail.getId(), false);
        return favoriteSelection.equals("Favorites") == isFavorite;
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

    private void filterPokemonList() {
        List<PokemonDetail> allPokemon = repository.getPokemonList().getValue();
        if (allPokemon == null) {
            return;
        }
        List<PokemonDetail> filteredList = new ArrayList<>();
        String favoriteSelection = currentFavoriteSelection.getValue();
        for (PokemonDetail pokemon : allPokemon) {
            boolean isFavorite = favoriteStatusArray.get(pokemon.getId(), false);
            if ("Favorites".equals(favoriteSelection)) {
                if (isFavorite) {
                    filteredList.add(pokemon);
                }
            } else if ("All".equals(favoriteSelection)) {
                filteredList.add(pokemon);
            }
        }
        ((MutableLiveData<List<PokemonDetail>>) pokemonList).setValue(filteredList);
    }

    public void resetFilters() {
        setCurrentSearchText("");
        setCurrentType("All");
        setCurrentGeneration("National");
        setCurrentFavoriteSelection("All");
    }
}