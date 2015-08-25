package com.lionel.claudon.android.app.popularmovies;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.lionel.claudon.android.app.popularmovies.data.MoviesContract;

/**
 * Created by lionel on 25/08/15.
 */
public class MoviesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int MOVIES_LOADER_ID = 0;
    private static final String LOG_TAG = MoviesFragment.class.getSimpleName();
    private MoviesAdapter  moviesAdapter;
    private GridView moviesGridView;

    private static final String[] MOVIES_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            MoviesContract.MoviesEntry.TABLE_NAME + "." + MoviesContract.MoviesEntry._ID,
            MoviesContract.MoviesEntry.COLUMN_POSTER_URL,
    };

    public static final int COL_MOVIES_ID = 0;
    public static final int COL_MOVIES_POSTER_URL = 1;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        moviesAdapter = new MoviesAdapter(getActivity(), null, 0);

        moviesGridView = (GridView) rootView.findViewById(R.id.gridview_movies);
        moviesGridView.setAdapter(moviesAdapter);

        moviesGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                if (cursor != null && cursor.moveToPosition(position)) {
                    //TODO
                }
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVIES_LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.movies_fragment, menu);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String orderByPref = prefs.getString(getString(R.string.pref_orderby_key), getString(R.string.pref_orderby_value_popularity));


        String sortOrder;

        if(orderByPref.equals(getString(R.string.pref_orderby_value_popularity))) {
            sortOrder = MoviesContract.MoviesEntry.COLUMN_POPULARITY + " DESC";
        } else {
            sortOrder = MoviesContract.MoviesEntry.COLUMN_RATE + " DESC";
        }


        return new CursorLoader(getActivity(),
                MoviesContract.MoviesEntry.CONTENT_URI,
                MOVIES_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        moviesAdapter.swapCursor(data);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        moviesAdapter.swapCursor(null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            onSettingsChanged();
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    public void onSettingsChanged() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String orderByPref = prefs.getString(getString(R.string.pref_orderby_key), getString(R.string.pref_orderby_value_popularity));

        if(orderByPref.equals(getString(R.string.pref_orderby_value_popularity))) {
            new FecthMoviesTask(getActivity()).execute(FecthMoviesTask.SORT_BY_POPULARITY);
        } else {
            new FecthMoviesTask(getActivity()).execute(FecthMoviesTask.SORT_BY_RATING);
        }

        getLoaderManager().restartLoader(MOVIES_LOADER_ID, null, this);
    }

}
