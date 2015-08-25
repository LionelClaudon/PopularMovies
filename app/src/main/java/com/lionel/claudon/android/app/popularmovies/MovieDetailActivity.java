package com.lionel.claudon.android.app.popularmovies;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

/**
 * Created by lionel on 25/08/15.
 */
public class MovieDetailActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.

            Bundle args = new Bundle();
            args.putParcelable(MovieDetailFragment.DETAIL_URI, getIntent().getData());
            MovieDetailFragment fragment = new MovieDetailFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.movie_detail_container, fragment)
                    .commit();
        }
    }

}
