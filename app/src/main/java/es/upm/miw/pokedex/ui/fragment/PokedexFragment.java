package es.upm.miw.pokedex.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.material.navigation.NavigationView;

import es.upm.miw.pokedex.R;
import es.upm.miw.pokedex.ui.viewmodel.PokemonViewModel;

public class PokedexFragment extends Fragment {

    private FirebaseAuth mAuth;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pokedex, container, false);

        // Initialize FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Initialize DrawerLayout and NavigationView
        drawerLayout = view.findViewById(R.id.drawer_layout);
        navigationView = view.findViewById(R.id.navigation_view);

        // Initialize Toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(getActivity(), drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Configure NavigationView
        navigationView.setNavigationItemSelectedListener(this::onNavigationItemSelected);

        // Check current user and update UI
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);

        // Load PokemonListFragment
        loadPokemonListFragment();

        return view;
    }

    private void updateUI(FirebaseUser user) {
        Menu menu = navigationView.getMenu();
        MenuItem currentUserItem = menu.findItem(R.id.menu_current_user);
        if (user != null) {
            String email = user.getEmail();
            currentUserItem.setTitle(email);
        } else {
            currentUserItem.setTitle(R.string.current_user);
        }
    }

    private void signOut() {
        mAuth.signOut();
        Toast.makeText(getContext(), R.string.signed_out, Toast.LENGTH_SHORT).show();

        // Navigate back to LoginMethodFragment
        LoginMethodFragment loginMethodFragment = new LoginMethodFragment();
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, loginMethodFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void loadPokemonListFragment() {
        PokemonListFragment pokemonListFragment = new PokemonListFragment();
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.pokemon_list_container, pokemonListFragment);
        transaction.commit();
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_sign_out) {
            signOut();
        } else if (id == R.id.menu_reload) {
            PokemonViewModel viewModel = new ViewModelProvider(this).get(PokemonViewModel.class);
            viewModel.fetchAllPokemon();
        }
        drawerLayout.closeDrawer(navigationView);
        return true;
    }
}