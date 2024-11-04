package es.upm.miw.pokedex.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import es.upm.miw.pokedex.R;
import es.upm.miw.pokedex.ui.viewmodel.PokemonViewModel;

public class PokedexFragment extends Fragment {

    private FirebaseAuth mAuth;
    private TextView statusTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pokedex, container, false);

        // Initialize FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI elements
        statusTextView = view.findViewById(R.id.statusTextView);
        Button signOutButton = view.findViewById(R.id.signOutButton);
        Button reloadButton = view.findViewById(R.id.reload_button);

        // Configure buttons
        signOutButton.setOnClickListener(v -> signOut());
        reloadButton.setOnClickListener(v -> {
            PokemonViewModel viewModel = new ViewModelProvider(this).get(PokemonViewModel.class);
            viewModel.fetchAllPokemon();
        });

        // Check current user and update UI
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);

        // Load PokemonListFragment
        loadPokemonListFragment();

        return view;
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            String email = user.getEmail();
            statusTextView.setText(email);
        } else {
            statusTextView.setText(R.string.not_authenticated);
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
}