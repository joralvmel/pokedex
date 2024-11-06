package es.upm.miw.pokedex.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import es.upm.miw.pokedex.ui.viewmodel.PokemonViewModel;
import es.upm.miw.pokedex.R;
import es.upm.miw.pokedex.adapter.PokemonAdapter;

public class PokemonListFragment extends Fragment {
    private PokemonViewModel viewModel;
    private PokemonAdapter adapter;
    private Spinner typeSpinner;
    private Spinner generationSpinner;
    private Spinner favoriteSpinner;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pokemon_list, container, false);

        viewModel = new ViewModelProvider(this).get(PokemonViewModel.class);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PokemonAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        progressBar = view.findViewById(R.id.progress_bar);

        viewModel.getFilteredPokemonList().observe(getViewLifecycleOwner(), pokemonDetails -> {
            if (Boolean.FALSE.equals(viewModel.getIsFetching().getValue())) {
                adapter.setPokemonList(pokemonDetails);
                recyclerView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }
        });

        viewModel.getIsFetching().observe(getViewLifecycleOwner(), isFetching -> {
            if (isFetching) {
                progressBar.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                if (viewModel.getFilteredPokemonList().getValue() != null) {
                    recyclerView.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        SearchView searchView = view.findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                viewModel.setCurrentSearchText(newText);
                return true;
            }
        });

        Button reloadButton = view.findViewById(R.id.reload_button);
        reloadButton.setOnClickListener(v -> {
            viewModel.resetFilters();
            typeSpinner.setSelection(0);
            generationSpinner.setSelection(0);
            favoriteSpinner.setSelection(0);
            searchView.setQuery("", false);
            Toast.makeText(getContext(), R.string.reset_filters, Toast.LENGTH_SHORT).show();
        });

        typeSpinner = view.findViewById(R.id.type_spinner);
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.pokemon_types, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(typeAdapter);
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                viewModel.setCurrentType(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                viewModel.setCurrentType("All");
            }
        });

        generationSpinner = view.findViewById(R.id.generation_spinner);
        ArrayAdapter<CharSequence> generationAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.pokemon_generations, android.R.layout.simple_spinner_item);
        generationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        generationSpinner.setAdapter(generationAdapter);
        generationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                viewModel.setCurrentGeneration(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                viewModel.setCurrentGeneration("National");
            }
        });

        favoriteSpinner = view.findViewById(R.id.favorite_spinner);
        ArrayAdapter<CharSequence> favoriteAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.favorite_filter_options, android.R.layout.simple_spinner_item);
        favoriteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        favoriteSpinner.setAdapter(favoriteAdapter);
        favoriteSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                viewModel.setCurrentFavoriteSelection(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                viewModel.setCurrentFavoriteSelection("All");
            }
        });

        return view;
    }
}