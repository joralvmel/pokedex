package es.upm.miw.pokedex;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class LoginMethodFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login_method, container, false);

        Button emailPasswordButton = view.findViewById(R.id.emailPasswordButton);
        emailPasswordButton.setOnClickListener(v -> {
            EmailPasswordFragment emailPasswordFragment = new EmailPasswordFragment();
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, emailPasswordFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        return view;
    }
}