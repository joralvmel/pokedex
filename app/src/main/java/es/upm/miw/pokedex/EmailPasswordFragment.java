package es.upm.miw.pokedex;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EmailPasswordFragment extends BaseFragment {

    private FirebaseAuth mAuth;
    private EditText fieldEmail, fieldPassword;
    private TextView statusTextView, detailTextView, emailVerificationStatus;
    private Button verifyEmailButton;
    private Group emailPasswordButtons, signedInButtons, emailPasswordFields;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_emailpassword, container, false);

        // Initialize FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI elements
        fieldEmail = view.findViewById(R.id.fieldEmail);
        fieldPassword = view.findViewById(R.id.fieldPassword);
        statusTextView = view.findViewById(R.id.status);
        detailTextView = view.findViewById(R.id.detail);
        emailVerificationStatus = view.findViewById(R.id.emailVerificationStatus);

        Button emailSignInButton = view.findViewById(R.id.emailSignInButton);
        Button emailCreateAccountButton = view.findViewById(R.id.emailCreateAccountButton);
        Button signOutButton = view.findViewById(R.id.signOutButton);
        verifyEmailButton = view.findViewById(R.id.verifyEmailButton);
        Button reloadButton = view.findViewById(R.id.reloadButton);

        emailPasswordButtons = view.findViewById(R.id.emailPasswordButtons);
        signedInButtons = view.findViewById(R.id.signedInButtons);
        emailPasswordFields = view.findViewById(R.id.emailPasswordFields);

        // Configure buttons
        emailCreateAccountButton.setOnClickListener(v -> createAccount());
        emailSignInButton.setOnClickListener(v -> signIn());
        signOutButton.setOnClickListener(v -> signOut());
        verifyEmailButton.setOnClickListener(v -> sendEmailVerification());
        reloadButton.setOnClickListener(v -> reload());

        // Check current user and update UI
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser != null);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        reload();
    }

    private void reload() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.reload().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    updateUI(true);
                    Toast.makeText(getContext(), R.string.reload_successful, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateUI(Boolean isLoggedIn) {
        if (isLoggedIn != null && isLoggedIn) {
            signedInButtons.setVisibility(View.VISIBLE);
            emailPasswordButtons.setVisibility(View.GONE);
            emailPasswordFields.setVisibility(View.INVISIBLE);

            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                statusTextView.setText(getString(R.string.user_status, user.getEmail()));
                detailTextView.setText(getString(R.string.firebase_id, user.getUid()));

                if (user.isEmailVerified()) {
                    verifyEmailButton.setVisibility(View.GONE);
                    emailVerificationStatus.setText(R.string.email_verified);

                    // Navigate to PokedexFragment
                    PokedexFragment pokedexFragment = new PokedexFragment();
                    FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_container, pokedexFragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                } else {
                    verifyEmailButton.setVisibility(View.VISIBLE);
                    emailVerificationStatus.setText(R.string.email_not_verified);
                }
            }
        } else {
            signedInButtons.setVisibility(View.GONE);
            emailPasswordButtons.setVisibility(View.VISIBLE);
            emailPasswordFields.setVisibility(View.VISIBLE);

            statusTextView.setText(R.string.not_authenticated);
            detailTextView.setText(null);
            emailVerificationStatus.setText(null);
        }
    }

    private void createAccount() {
        String email = fieldEmail.getText().toString();
        String password = fieldPassword.getText().toString();

        if (validateForm(email, password)) return;

        showProgressBar();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity(), task -> {
                    hideProgressBar();
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            sendEmailVerification(); // Ensure email verification is sent
                            updateUI(true);
                            Toast.makeText(getContext(), R.string.account_created_success, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        updateUI(false);
                        Toast.makeText(getContext(), R.string.account_creation_error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void signIn() {
        String email = fieldEmail.getText().toString();
        String password = fieldPassword.getText().toString();

        if (validateForm(email, password)) return;

        showProgressBar();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity(), task -> {
                    hideProgressBar();
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                            updateUI(true);
                            Toast.makeText(getContext(), R.string.signed_in, Toast.LENGTH_SHORT).show();
                        } else {
                            mAuth.signOut();
                            updateUI(false);
                            Toast.makeText(getContext(), R.string.email_not_verified, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        updateUI(false);
                        Toast.makeText(getContext(), R.string.auth_failed, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void signOut() {
        mAuth.signOut();
        updateUI(false);
        Toast.makeText(getContext(), R.string.signed_out, Toast.LENGTH_SHORT).show();

        // Navigate back to LoginMethodFragment
        LoginMethodFragment loginMethodFragment = new LoginMethodFragment();
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, loginMethodFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void sendEmailVerification() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(requireActivity(), task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), R.string.email_verification_sent, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), R.string.email_verification_failed, Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private boolean validateForm(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            fieldEmail.setError(getString(R.string.required));
            return true;
        }
        if (TextUtils.isEmpty(password)) {
            fieldPassword.setError(getString(R.string.required));
            return true;
        }
        return false;
    }
}