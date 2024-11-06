package es.upm.miw.pokedex.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import es.upm.miw.pokedex.R;

public class PokedexFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pokedex, container, false);

        // Initialize DrawerLayout
        DrawerLayout drawerLayout = view.findViewById(R.id.drawer_layout);

        // Initialize Toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(getActivity(), drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Load the NavigationDrawerFragment
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.navigation_drawer_container, new NavigationDrawerFragment());
        transaction.commit();

        // Load PokemonListFragment
        loadPokemonListFragment();

        return view;
    }

    private void loadPokemonListFragment() {
        PokemonListFragment pokemonListFragment = new PokemonListFragment();
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.pokemon_list_container, pokemonListFragment);
        transaction.commit();
    }
}