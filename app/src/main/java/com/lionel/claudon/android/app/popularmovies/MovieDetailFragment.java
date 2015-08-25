package com.lionel.claudon.android.app.popularmovies;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import static com.lionel.claudon.android.app.popularmovies.data.MoviesContract.MoviesEntry;

/**
 * Created by lionel on 25/08/15.
 */
public class MovieDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = MovieDetailFragment.class.getSimpleName();

    static final String DETAIL_URI = "URI";

    private static final int DETAIL_LOADER = 0;

    private Uri uri;

    private static final String[] DETAIL_COLUMNS = {
            MoviesEntry.TABLE_NAME + "." + MoviesEntry._ID,
            MoviesEntry.COLUMN_TITLE,
            MoviesEntry.COLUMN_RELEASE_DATE,
            MoviesEntry.COLUMN_RATE,
            MoviesEntry.COLUMN_SYNOPSYS,
            MoviesEntry.COLUMN_POSTER_URL
    };

    // These indices are tied to DETAIL_COLUMNS.  If DETAIL_COLUMNS changes, these
    // must change.
    public static final int COL_ID = 0;
    public static final int COL_TITLE = 1;
    public static final int COL_RELEASE_DATE = 2;
    public static final int COL_RATING = 3;
    public static final int COL_SYNOPSYS = 4;
    public static final int COL_MOVIES_POSTER_URL = 5;

    private ImageView mIconView;
    private TextView mTitleView;
    private TextView mRatingView;
    private TextView mSynopsysView;
    private TextView mReleaseDateView;


    public MovieDetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle args = getArguments();
        if(args != null) {
            uri = args.getParcelable(DETAIL_URI);
        }

        View rootView = inflater.inflate(R.layout.fragment_detail_movie, container, false);
        mIconView = (ImageView) rootView.findViewById(R.id.detail_icon);
        mRatingView = (TextView) rootView.findViewById(R.id.detail_average_rating);
        mSynopsysView = (TextView) rootView.findViewById(R.id.detail_movie_description);
        mTitleView = (TextView) rootView.findViewById(R.id.detail_movie_title);
        mReleaseDateView = (TextView) rootView.findViewById(R.id.detail_release_date);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.v(LOG_TAG, "In onCreateLoader");
        if ( null != uri ) {
            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(
                    getActivity(),
                    uri,
                    DETAIL_COLUMNS,
                    null,
                    null,
                    null
            );
        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            // Read weather condition ID from cursor

            Picasso.with(getActivity()).load("http://image.tmdb.org/t/p/w185" + data.getString(COL_MOVIES_POSTER_URL)).into(mIconView);


            // Read date from cursor and update views for day of week and date
            String date = data.getString(COL_RELEASE_DATE);
            mReleaseDateView.setText(date);

            // Read description from cursor and update view
            String description = data.getString(COL_SYNOPSYS);
            mSynopsysView.setText(description);

            // For accessibility, add a content description to the icon field
            mIconView.setContentDescription(description);

            String averageRating = data.getString(COL_RATING);
            mRatingView.setText(averageRating + "/10");

            String movieTitle = data.getString(COL_TITLE);
            mTitleView.setText(movieTitle);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}