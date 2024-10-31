package es.upm.miw.pokedex;

import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.VisibleForTesting;
import androidx.fragment.app.Fragment;

public class BaseFragment extends Fragment {

    @VisibleForTesting
    public ProgressBar mProgressBar;

    public void showProgressBar() {
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.VISIBLE);
        }
    }

    public void hideProgressBar() {
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        hideProgressBar();
    }

}
