package com.example.hackertronix.firebaseauthtest;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.example.hackertronix.firebaseauthtest.model.Wallpaper;
import com.example.hackertronix.firebaseauthtest.utils.API;
import com.example.hackertronix.firebaseauthtest.database.FavoriteWallpaperContract.FavoriteWallpaperEntry;

public class FullScreenImage extends AppCompatActivity {

    private TextView artistTextView;
    private ImageView fullImage;
    private ProgressBar mProgressbar;
    private FloatingActionButton favouriteButton;
    private String width;
    private String height;
    private Typeface Signalist;
    private int h;
    private int w;
    private Wallpaper wallpaper;
    public static final int TASK_LOADER_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        wallpaper = getIntent().getExtras().getParcelable("PARCEL");


        artistTextView=(TextView)findViewById(R.id.artist_tv);
        fullImage=(ImageView)findViewById(R.id.full_image_view);
        mProgressbar=(ProgressBar)findViewById(R.id.progressbar);


        favouriteButton=(FloatingActionButton)findViewById(R.id.fab);


        Signalist=Typeface.createFromAsset(getAssets(),"fonts/Signalist.otf");
        artistTextView.setTypeface(Signalist);

        getScreenDimensions();


        checkFav();


        if(Build.VERSION.SDK_INT>=21) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

            getWindow().setStatusBarColor(Color.TRANSPARENT);
            getWindow().setNavigationBarColor(Color.TRANSPARENT);
        }


        artistTextView.setText(wallpaper.getAuthor());

        //TODO REMOVE 500 and 750 from glide request and add width and height instead


        Glide.with(this).load(API.FULL_RES_IMAGE_ENDPOINT+"500"+"/"+"750"+"?image="+String.valueOf(wallpaper.getId()))
                .asBitmap()
                .listener(new RequestListener<String, Bitmap>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        mProgressbar.setVisibility(View.GONE);
                        favouriteButton.show();
                        return false;
                    }
                })
                .into(new SimpleTarget<Bitmap>(500, 750) {
            @Override
            public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {
                // Do something with bitmap here.

                fullImage.setImageBitmap(bitmap);
            }
        });

        favouriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleFavorite();
            }
        });

        //getSupportLoaderManager().initLoader(TASK_LOADER_ID,null,this);

    }


    private void checkFav() {



        Uri singleItemUri = ContentUris.withAppendedId(FavoriteWallpaperEntry.CONTENT_URI, wallpaper.getId());

        Cursor cursor = getContentResolver().query(singleItemUri,null,null,null,null);

        if(cursor.getCount() > 0)
        {
            favouriteButton.setImageResource(R.drawable.ic_favorite_black_24dp);
            cursor.close();
        }

        else{
            favouriteButton.setImageResource(R.drawable.ic_favorite_border_black_24dp);
            cursor.close();
        }
    }

    private boolean isWallFav(Cursor cursor) {

        if(cursor.getCount()>0)
        {
            return true;
        }
        else {
            return false;
        }
    }


    private void handleFavorite() {

        Uri singleItemUri = ContentUris.withAppendedId(FavoriteWallpaperEntry.CONTENT_URI, wallpaper.getId());
        Cursor cursor = getContentResolver().query(singleItemUri,null,null,null,null);
        boolean isFav = isWallFav(cursor);
        if(isFav == false)
        {
            ContentValues contentValues = new ContentValues();

            String format = wallpaper.getFormat();
            String filename = wallpaper.getFilename();
            String author = wallpaper.getAuthor();
            String author_url = wallpaper.getAuthor_url();
            String post_url = wallpaper.getPost_url();

            int width = wallpaper.getWidth();
            int height = wallpaper.getHeight();
            int id = wallpaper.getId();

            contentValues.put(FavoriteWallpaperEntry._ID,id);
            contentValues.put(FavoriteWallpaperEntry.COLUMN_FORMAT,format);
            contentValues.put(FavoriteWallpaperEntry.COLUMN_FILENAME,filename);
            contentValues.put(FavoriteWallpaperEntry.COLUMN_AUTHOR,author);
            contentValues.put(FavoriteWallpaperEntry.COLUMN_AUTHOR_URL,author_url);
            contentValues.put(FavoriteWallpaperEntry.COLUMN_POST_URL,post_url);
            contentValues.put(FavoriteWallpaperEntry.COLUMN_WIDTH,width);
            contentValues.put(FavoriteWallpaperEntry.COLUMN_HEIGHT,height);


            Uri uri = getContentResolver().insert(FavoriteWallpaperEntry.CONTENT_URI,contentValues);
            favouriteButton.setImageResource(R.drawable.ic_favorite_black_24dp);

            updateWidgets(getApplicationContext());
        }
        else
        {


            int deleted= getContentResolver().delete(singleItemUri,null,null);

            //Toast.makeText(this, deleted+" item deleted", Toast.LENGTH_SHORT).show();
            favouriteButton.setImageResource(R.drawable.ic_favorite_border_black_24dp);

            updateWidgets(this);
            //finish();
        }


    }

    private void updateWidgets(Context context) {

            Intent dbUpdateIntent = new Intent(API.ACTION_DATABASE_UPDATED).setPackage(context.getPackageName());
            context.sendBroadcast(dbUpdateIntent);

    }


    //// TODO: Implement this feature
    private void setImageAsWallpaper() {

    }



    private void getScreenDimensions() {

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        w=size.x;
        h=size.y;

        width= String.valueOf(size.x);
        height=String.valueOf(size.y);


    }


}


