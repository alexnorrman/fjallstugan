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
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.io.IOException;
import java.io.InputStream;


public class MainActivity extends AppCompatActivity {

    //Codes for savedInstanceState.
    public static final String FJALLSTATIONER_KEY = "fjallstationer";
    public static final String CURRENT_FRAGMENT_KEY = "current_fragment";

    //TODO: Insert your JSON file name here
    private static final String JSON_FILE = "fjallstationer.json";

    // Codes for premissions.
    public static final int PERMISSION_LOCATION = 1;
    public static final int PERMISSION_READ = 2;
    public static final int PERMISSION_WRITE = 3;

    private final MapFragment fragment1 = new MapFragment();
    private final StationListFragment fragment2 = new StationListFragment();
    private final SavedMapsGridFragment fragment3 = new SavedMapsGridFragment();
    private final FragmentManager fm = getSupportFragmentManager();
    private Fragment active;
    private Activity activity;
    private Fjallstationer fjallstationer;
    private Menu toolbar;

    /**
     * Listener for bottom navigation bar.
     * Changes the active fragment.
     */
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.nav_fragment1:
                    fm.beginTransaction().hide(active).show(fragment1).commit();
                    active = fragment1;
                    return true;
                case R.id.nav_fragment2:
                    fm.beginTransaction().hide(active).show(fragment2).commit();
                    active = fragment2;
                    return true;

                case R.id.nav_fragment3:
                    fm.beginTransaction().detach(fragment3).attach(fragment3).hide(active).show(fragment3).commit();
                    active = fragment3;
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activity = this;

        if(savedInstanceState != null){
            fjallstationer = savedInstanceState.getParcelable(FJALLSTATIONER_KEY);
            active = getSupportFragmentManager().getFragment(savedInstanceState, CURRENT_FRAGMENT_KEY);
        }
        else{
            active = fragment1;
            fjallstationer = new Fjallstationer(getJson());
        }
        fm.beginTransaction().add(R.id.fragment, fragment3, "3").hide(fragment3).commit();
        fm.beginTransaction().add(R.id.fragment, fragment2, "2").hide(fragment2).commit();
        fm.beginTransaction().add(R.id.fragment, fragment1, "1").hide(fragment1).commit();
        fm.beginTransaction().show(active).commit();

        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    /**
     * Returns the list of Fjallstationer.
     * @return
     */
    public Fjallstationer getFjallstationer() {
        return fjallstationer;
    }

    /**
     * Returns the JSON string from the JSON file.
     * @return JSON string.
     */
    public String getJson(){
        String json;
        try{
            InputStream inputStream = getAssets().open(JSON_FILE);
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            json = new String(buffer, "UTF-8");
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }
        return json;
    }

    /**
     * Set toolbar-settings.
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);

        toolbar = menu;
        MenuItem save = menu.findItem(R.id.save_map);
        Drawable d = save.getIcon();
        if(d != null){
            d.mutate();
            d.setColorFilter(getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_ATOP);
        }
        save.setVisible(false);
        save.setEnabled(false);

        return true;
    }

    /**
     * Starts the new activity when menu item is pressed.
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.settings:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.information:
                intent = new Intent(this, InformationActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Saves Fjallstationer and
     * the current fragment to the
     * @param saveInstanceState
     */
    @Override
    public void onSaveInstanceState(Bundle saveInstanceState) {
        super.onSaveInstanceState(saveInstanceState);
        saveInstanceState.putParcelable(FJALLSTATIONER_KEY, fjallstationer);
        getSupportFragmentManager().putFragment(saveInstanceState, CURRENT_FRAGMENT_KEY, active);
    }
    /**
     * Adds alert dialog if the user presses the back button
     * to close the application.
     */
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                //.setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Avslutar Fjällstugan")
                .setMessage("Är du säker på att du vill avsluta?")
                .setPositiveButton("Ja", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }

                })
                .setNegativeButton("Nej", null)
                .show();
    }

    /**
     * Check permission to read from the device.
     * @return
     */
    public boolean isReadStoragePermissionGranted() {
        if (ContextCompat.checkSelfPermission(activity,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                /*new AlertDialog.Builder(activity)
                        .setTitle("Applikationens funktionallitet har begränsats!")
                        .setMessage("Tillåt att hämta sparade kartor?").setNegativeButton("Nej", null)
                        .setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(activity, new String[]
                                        {Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_READ);
                            }
                        })
                        .create()
                        .show();*/
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_READ);
            }
            return false;
        } else {
            return true;
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
     * Check permission for the users location.
     * @return
     */
    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(activity)
                        .setTitle("För att kunna se din position behöver applikationen tillåtelse!")
                        .setMessage("Tillåt platsåtkomst?").setNegativeButton("Nej",null)
                        .setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(activity,new String[]
                                        {Manifest.permission.ACCESS_FINE_LOCATION},PERMISSION_LOCATION);
                            }
                        })
                        .create()
                        .show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSION_LOCATION);
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
            case PERMISSION_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    if (checkLocationPermission()) {
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                        SharedPreferences.Editor edt = prefs.edit();
                        edt.putBoolean("location",true);
                        edt.commit();
                    }
                } else {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                    SharedPreferences.Editor edt = prefs.edit();
                    edt.putBoolean("location",false);
                    edt.commit();
                }
                return;
            }
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
}
