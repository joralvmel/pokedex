package es.upm.miw.pokedex;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.FragmentTransaction;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import es.upm.miw.pokedex.ui.fragment.EmailPasswordFragment;
import es.upm.miw.pokedex.ui.fragment.LoginMethodFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Retrieve theme preference from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("settings", 0);
        boolean isDarkMode = sharedPreferences.getBoolean("dark_mode", false);
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase
        FirebaseApp.initializeApp(this);

        // Initialize FirebaseAuth
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        if (savedInstanceState == null) {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            if (currentUser != null) {
                // User is signed in, navigate to EmailPasswordFragment
                EmailPasswordFragment emailPasswordFragment = new EmailPasswordFragment();
                transaction.replace(R.id.fragment_container, emailPasswordFragment);
            } else {
                // User is not signed in, navigate to LoginMethodFragment
                LoginMethodFragment loginMethodFragment = new LoginMethodFragment();
                transaction.replace(R.id.fragment_container, loginMethodFragment);
            }

            transaction.commit();
        }
    }
}