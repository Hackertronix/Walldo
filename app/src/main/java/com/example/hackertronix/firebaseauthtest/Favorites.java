package com.example.hackertronix.firebaseauthtest;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.hackertronix.firebaseauthtest.adapters.FavoritesWallpaperAdapter;
import com.example.hackertronix.firebaseauthtest.database.FavoriteWallpaperContract;

public class Favorites extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {


    public static final int TASK_LOADER_ID = 0;

    private FavoritesWallpaperAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private TextView toolbar_tv;
    private Typeface SFUI;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);


        mRecyclerView = (RecyclerView) findViewById(R.id.favorites_recyclerview);
        toolbar_tv = (TextView) findViewById(R.id.toolbar_title);


        View view = findViewById(R.id.favorites_container);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.WHITE);

        }

        SFUI = Typeface.createFromAsset(getAssets(), "fonts/sftext.otf");

        toolbar_tv.setTypeface(SFUI);

        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        mAdapter = new FavoritesWallpaperAdapter(this);
        mRecyclerView.setAdapter(mAdapter);


        getSupportLoaderManager().initLoader(TASK_LOADER_ID, null, this);

    }

    @Override
    protected void onResume() {
        super.onResume();

        // re-queries for all tasks
        getSupportLoaderManager().restartLoader(TASK_LOADER_ID, null, this);
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<Cursor>(this) {

            Cursor mFavoritesData = null;

            @Override
            protected void onStartLoading() {

                if (mFavoritesData != null) {
                    deliverResult(mFavoritesData);
                } else {
                    forceLoad();
                }
            }

            @Override
            public Cursor loadInBackground() {

                try {
                    return getContentResolver().query(FavoriteWallpaperContract.FavoriteWallpaperEntry.CONTENT_URI,
                            null,
                            null,
                            null,
                            FavoriteWallpaperContract.FavoriteWallpaperEntry._ID);

                } catch (Exception e) {
                    Log.e("TAG", "asynchronously load data.");
                    e.printStackTrace();
                    return null;
                }
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);

    }

}