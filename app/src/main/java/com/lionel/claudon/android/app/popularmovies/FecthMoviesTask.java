package com.lionel.claudon.android.app.popularmovies;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

import static com.lionel.claudon.android.app.popularmovies.data.MoviesContract.MoviesEntry;

/**
 * Created by lionel on 25/08/15.
 */
public class FecthMoviesTask extends AsyncTask<String, Void, Void> {

    private final String LOG_TAG = FecthMoviesTask.class.getSimpleName();

    public static final String SORT_BY_POPULARITY = "popularity.desc";
    public static final String SORT_BY_RATING = "vote_average.desc";


    private final Context mContext;

    public FecthMoviesTask(Context context) {
        mContext = context;
    }

    /**
     * Helper method to handle insertion of a new movie in the movie database.
     *
     */
    long addMovie(long id, String posterUrl, String title, String overview, long popularity, long rate) {
        long movieId;

        // First, check if the movie with this id exists in the db
        Cursor moviesCursor = mContext.getContentResolver().query(
                MoviesEntry.CONTENT_URI,
                new String[]{MoviesEntry._ID},
                MoviesEntry.COLUMN_MOVIE_ID + " = ?",
                new String[]{String.valueOf(id)},
                null);

        if (moviesCursor.moveToFirst()) {
            int movieIdIndex = moviesCursor.getColumnIndex(MoviesEntry._ID);
            movieId = moviesCursor.getLong(movieIdIndex);
        } else {
            // Now that the content provider is set up, inserting rows of data is pretty simple.
            // First create a ContentValues object to hold the data you want to insert.
            ContentValues movieValues = new ContentValues();

            // Then add the data, along with the corresponding name of the data type,
            // so the content provider knows what kind of value is being inserted.
            movieValues.put(MoviesEntry.COLUMN_MOVIE_ID, id);
            movieValues.put(MoviesEntry.COLUMN_POSTER_URL, posterUrl);
            movieValues.put(MoviesEntry.COLUMN_SYNOPSYS, overview);
            movieValues.put(MoviesEntry.COLUMN_TITLE, title);
            movieValues.put(MoviesEntry.COLUMN_POPULARITY, popularity);
            movieValues.put(MoviesEntry.COLUMN_RATE, rate);

            // Finally, insert location data into the database.
            Uri insertedUri = mContext.getContentResolver().insert(
                    MoviesEntry.CONTENT_URI,
                    movieValues
            );

            // The resulting URI contains the ID for the row.  Extract the locationId from the Uri.
            movieId = ContentUris.parseId(insertedUri);
        }

        moviesCursor.close();

        // Wait, that worked?  Yes!
        return movieId;
    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private void getMovieDataFromJson(String forecastJsonStr)
            throws JSONException {

        final String OWM_RESULTS = "results";
        final String OWM_ID = "id";
        final String OWM_POSTER_URL = "poster_path";
        final String OWM_TITLE = "original_title";
        final String OWM_SYNOPSYS = "overview";
        final String OWM_POPULARITY = "popularity";
        final String OWM_RATE = "vote_average";
        final String OWM_RELEASE_DATE = "release_date";


        try {
            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray movieArray = forecastJson.getJSONArray(OWM_RESULTS);

            // Insert the new weather information into the database
            Vector<ContentValues> cVVector = new Vector<ContentValues>(movieArray.length());

            for(int i = 0; i < movieArray.length(); i++) {
                // These are the values that will be collected.
                String title;
                int id;
                String description;
                double popularity;
                double rate;
                String posterPath;
                String releaseDate;

                // Get the JSON object representing the day
                JSONObject movie = movieArray.getJSONObject(i);

                title = movie.getString(OWM_TITLE);
                id = movie.getInt(OWM_ID);
                description = movie.getString(OWM_SYNOPSYS);
                popularity = movie.getDouble(OWM_POPULARITY);
                rate = movie.getDouble(OWM_RATE);
                posterPath = movie.getString(OWM_POSTER_URL);
                releaseDate = movie.getString(OWM_RELEASE_DATE);

                ContentValues weatherValues = new ContentValues();

                weatherValues.put(MoviesEntry.COLUMN_MOVIE_ID, id);
                weatherValues.put(MoviesEntry.COLUMN_POPULARITY, popularity);
                weatherValues.put(MoviesEntry.COLUMN_POSTER_URL, posterPath);
                weatherValues.put(MoviesEntry.COLUMN_TITLE, title);
                weatherValues.put(MoviesEntry.COLUMN_SYNOPSYS, description);
                weatherValues.put(MoviesEntry.COLUMN_RATE, rate);
                weatherValues.put(MoviesEntry.COLUMN_RELEASE_DATE, releaseDate);

                cVVector.add(weatherValues);
            }

            // add to database
            if ( cVVector.size() > 0 ) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                mContext.getContentResolver().bulkInsert(MoviesEntry.CONTENT_URI, cvArray);
            }

            // Students: Uncomment the next lines to display what what you stored in the bulkInsert
            Cursor cur = mContext.getContentResolver().query(MoviesEntry.CONTENT_URI,
                    null, null, null, null);

            cVVector = new Vector<ContentValues>(cur.getCount());
            if ( cur.moveToFirst() ) {
                do {
                    ContentValues cv = new ContentValues();
                    DatabaseUtils.cursorRowToContentValues(cur, cv);
                    cVVector.add(cv);
                } while (cur.moveToNext());
            }

            Log.d(LOG_TAG, "FetchWeatherTask Complete. " + cVVector.size() + " Inserted");

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    @Override
    protected Void doInBackground(String... params) {
        // If there's no zip code, there's nothing to look up.  Verify size of params.
        if (params.length == 0) {
            return null;
        }

        String sortBy = params[0];

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String moviesJson = null;



        try {

            final String FORECAST_BASE_URL =
                    "http://api.themoviedb.org/3/discover/movie?";

            Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                    .appendQueryParameter("sort_by", sortBy)
                    .appendQueryParameter("api_key", mContext.getString(R.string.tmdb_api_key))
                    .build();

            URL url = new URL(builtUri.toString());

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            moviesJson = buffer.toString();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attemping
            // to parse it.
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        try {
            getMovieDataFromJson(moviesJson);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        // This will only happen if there was an error getting or parsing the forecast.
        return null;
    }
}