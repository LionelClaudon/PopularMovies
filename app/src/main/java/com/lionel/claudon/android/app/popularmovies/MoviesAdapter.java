package com.lionel.claudon.android.app.popularmovies;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

/**
 * Created by lionel on 25/08/15.
 */
public class MoviesAdapter extends CursorAdapter {
    private Context context;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public MoviesAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        this.context = context;
    }

    public MoviesAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return new ImageView(context);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Picasso.with(context).load("http://image.tmdb.org/t/p/w185" + cursor.getString(MoviesFragment.COL_MOVIES_POSTER_URL)).into((ImageView) view);

    }
}
