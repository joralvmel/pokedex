package es.upm.miw.pokedex;

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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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

        // Configure buttons
        signOutButton.setOnClickListener(v -> signOut());

        // Check current user and update UI
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);

        return view;
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            statusTextView.setText(getString(R.string.user_status, user.getEmail()));
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

}