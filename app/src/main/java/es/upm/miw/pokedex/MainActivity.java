package es.upm.miw.pokedex;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import es.upm.miw.pokedex.ui.fragment.EmailPasswordFragment;
import es.upm.miw.pokedex.ui.fragment.LoginMethodFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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