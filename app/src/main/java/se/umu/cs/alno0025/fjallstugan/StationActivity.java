/**
 * Course:  Development for mobile applications.
 *          Umeå University
 *          Summer 2019
 * @author Alex Norrman
 */

package se.umu.cs.alno0025.fjallstugan;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class StationActivity extends AppCompatActivity {

    private static final String FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() +
            "/fjallstugan/fjallstugor/";

    public static final int PERMISSION_LOCATION = 1;
    public static final int PERMISSION_READ = 2;
    public static final int PERMISSION_WRITE = 3;

    private Fjallstation fjallstation;
    private Bitmap imgBitmap = null;
    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_station);

        activity = this;
        Intent intent = getIntent();
        fjallstation = intent.getParcelableExtra("station");

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setTitle(fjallstation.getName());
        }

        ImageView stationImageView = findViewById(R.id.station_page_image);

        // If the premission to read & the file is not downloaded, download the file
        // If the file is already downloaded, use that one.
        // Else, load the image from the website.
        if(isWriteToStoragePermissionGranted()){
            if(!isImgAlreadyDownloaded(fjallstation.getName()))
                new DownloadImageTask(fjallstation.getName()).execute(fjallstation.getImgUrl());

            if(isImgAlreadyDownloaded(fjallstation.getName())){
                String file_path = FILE_PATH + fjallstation.getName().replaceAll("[^a-zA-Z]+", "")+".png";
                stationImageView.setImageBitmap(BitmapFactory.decodeFile(file_path));
            }
        }
        else{
            new LoadImageTask().execute(fjallstation.getImgUrl());
            stationImageView.setImageBitmap(imgBitmap);
        }


        TextView name = findViewById(R.id.station_title);
        name.setText(fjallstation.getName());

        TextView adress = findViewById(R.id.address);
        adress.setText(fjallstation.getAdress());

        TextView phone = findViewById(R.id.phone_nr);
        phone.setText(fjallstation.getPhoneNr());

        TextView email = findViewById(R.id.email);
        email.setText(fjallstation.getEmail());

        TextView url = findViewById(R.id.homepage);
        url.setText(fjallstation.getUrl());
        url.setMovementMethod(LinkMovementMethod.getInstance());
    }

    /**
     * Gets the images bitmap to later save to the device.
     * Doing it Async to not freeze the activity.
     */
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        private String name;

        public DownloadImageTask(String fjallstation) {
            name = fjallstation;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            downloadImg(name, result);
        }
    }


    /**
     * Saves a image to the device.
     * @param name image name
     * @param bitmap image bitmap
     */
    private void downloadImg(String name, Bitmap bitmap){
        String file_path = FILE_PATH;
        File dir = new File(file_path);
        if (!dir.exists())
            dir.mkdirs();

        File file = new File(dir,  name.replaceAll("[^a-zA-Z]+", "")+".png");

        if(!file.exists()){
            FileOutputStream fOut = null;
            try {
                fOut = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            bitmap.compress(Bitmap.CompressFormat.PNG, 85, fOut);
            try {
                fOut.flush();
                fOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Checks if the image already exists or not.
     * @param name
     * @return
     */
    private boolean isImgAlreadyDownloaded(String name){
        String file_path = FILE_PATH;
        File dir = new File(file_path);
        if (!dir.exists())
            dir.mkdirs();
        File file = new File(dir,  name.replaceAll("[^a-zA-Z]+", "")+".png");
        if(file.exists()){
            return true;
        }
        else
            return false;
    }

    /**
     * Load the images from URL
     * if read the files from device is not allowed.
     * Doing it Async to not freeze the activity, might take a while to
     * get all the images showing.
     */
    private class LoadImageTask extends AsyncTask<String, Void, Bitmap> {
        public LoadImageTask() {
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            imgBitmap = result;
        }
    }

    /**
     * Check permission to write to the device.
     * @return
     */
    public boolean isWriteToStoragePermissionGranted() {
        if (ContextCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(activity)
                        .setTitle("För att spara kartor behöver applikationen tillåtelse!")
                        .setMessage("Tillåt att kunna spara kartor på din enhet?").setNegativeButton("Nej",null)
                        .setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(activity,new String[]
                                        {Manifest.permission.WRITE_EXTERNAL_STORAGE},PERMISSION_WRITE);
                            }
                        })
                        .create()
                        .show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSION_WRITE);
            }
            return false;
        } else {
            return true;
        }
    }

    /**
     * Handles the premissions request.
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_READ: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request.
            case PERMISSION_WRITE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    /**
     * When the back-arrow is pressed in the toolbar,
     * handle it as a onBackPressed.
     * @return
     */
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
