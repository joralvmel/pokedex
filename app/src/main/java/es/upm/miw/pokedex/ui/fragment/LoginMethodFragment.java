package es.upm.miw.pokedex.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;


import es.upm.miw.pokedex.R;

public class LoginMethodFragment extends Fragment {

    private static final String TAG = "LoginMethodFragment";
    @SuppressWarnings("deprecation")
    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth firebaseAuth;


    private final ActivityResultLauncher<Intent> googleSignInLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                requireActivity();
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);                        try {
                            GoogleSignInAccount account = task.getResult();
                            if (account != null) {
                                firebaseAuthWithGoogle(account);
                            }
                        } catch (Exception e) {
                            Log.w(TAG, String.valueOf(R.string.google_sign_in_fail), e);
                        }
                    }
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login_method, container, false);

        firebaseAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso);

        Button emailPasswordButton = view.findViewById(R.id.emailPasswordButton);
        emailPasswordButton.setOnClickListener(v -> {
            EmailPasswordFragment emailPasswordFragment = new EmailPasswordFragment();
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, emailPasswordFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        SignInButton googleSignInButton = view.findViewById(R.id.googleSignInButton);
        googleSignInButton.setOnClickListener(v -> signInWithGoogle());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        reload();
    }

    private void reload() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            Toast.makeText(getContext(), R.string.reload_successful, Toast.LENGTH_SHORT).show();
        } else {
            Log.d(TAG, getString(R.string.not_authenticated));
        }
    }

    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(requireContext(), R.string.signed_in, Toast.LENGTH_SHORT).show();
                        navigateToPokedexFragment();
                    } else {
                        Log.w(TAG, String.valueOf(R.string.auth_failed), task.getException());
                    }
                });
    }

    private void navigateToPokedexFragment() {
        PokedexFragment pokedexFragment = new PokedexFragment();
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, pokedexFragment);
        transaction.commit();
        getParentFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }
}