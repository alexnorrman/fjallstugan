/**
 * Course:  Development for mobile applications.
 *          Umeå University
 *          Summer 2019
 * @author Alex Norrman
 */
package se.umu.cs.alno0025.fjallstugan;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.android.gms.maps.model.UrlTileProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MapFragment extends Fragment {

    //TODO: Insert your Lantmateriet Opendata API key here
    private static final String API_KEY = "YOUR API KEY";

    public static final String SHARED_PREFS_MAPTYPE = "maptype";
    public static final String SHARED_PREFS_MARKERS = "markers";
    public static final String SHARED_PREFS_LOCATION = "location";

    private static final String FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() +
            "/fjallstugan/kartor/";


    private MainActivity activity;
    private Fjallstationer fjallstationer;
    private MapView mMapView;
    private GoogleMap googleMap;
    private TileOverlay tileOverlay;
    private int mapType = 0;
    private CameraPosition currentCP = null;
    private MenuItem save;
    private boolean showMarkers = true;
    private boolean showLocation = true;

    /**
     * Listener for if something in the settings (SharedPreferences)
     * is changed.
     */
    SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
            // listener implementation

            if(key.equals(SHARED_PREFS_MAPTYPE)){
                mapType = Integer.parseInt(prefs.getString(SHARED_PREFS_MAPTYPE,"0"));
            }
            if(key.equals(SHARED_PREFS_MARKERS)){
                showMarkers = prefs.getBoolean(SHARED_PREFS_MARKERS, true);
            }
            if(key.equals(SHARED_PREFS_LOCATION)){
                showLocation = prefs.getBoolean(SHARED_PREFS_LOCATION, true);
                if(showLocation && !activity.checkLocationPermission()){
                    SharedPreferences.Editor edt = prefs.edit();
                    edt.putBoolean(SHARED_PREFS_LOCATION,false);
                    edt.commit();
                    showLocation = false;
                }
            }
            currentCP = googleMap.getCameraPosition();
            updateMap();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_map, container, false);

        activity = (MainActivity)getActivity();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        // Don't try to show users location if the
        // user don't allow it.
        if(!activity.checkLocationPermission()){
            SharedPreferences.Editor edt = prefs.edit();
            edt.putBoolean(SHARED_PREFS_LOCATION,false);
            edt.commit();
            showLocation = false;
        }
        prefs.registerOnSharedPreferenceChangeListener(listener);
        mapType = Integer.parseInt(prefs.getString(SHARED_PREFS_MAPTYPE,"0"));
        showMarkers = prefs.getBoolean(SHARED_PREFS_MARKERS, true);
        showLocation = prefs.getBoolean(SHARED_PREFS_LOCATION, true);
        setHasOptionsMenu(true);

        if (savedInstanceState != null) {
            LatLng latLng = new LatLng(savedInstanceState.getDouble("latitude"), savedInstanceState.getDouble("longitude"));
            currentCP = new CameraPosition(latLng,
                    savedInstanceState.getFloat("zoom"),
                    savedInstanceState.getFloat("tilt"),
                    savedInstanceState.getFloat("bearing"));

        }
        MainActivity activity = (MainActivity)getActivity();
        fjallstationer = activity.getFjallstationer();


        mMapView = rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume();

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        updateMap();
        return rootView;
    }

    /**
     * Updates the maps with
     * Location, Markers and Map Settings.
     */
    private void updateMap(){
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;
                googleMap.clear();
                googleMap.setPadding(0,dpToPx(60),0,0);
                addLocation();
                setMapSettings();
                addMarkers();
            }
        });
    }

    /**
     * Set settings for the map.
     */
    private void setMapSettings() {
        setMapType();

        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        //googleMap.getUiSettings().setRotateGesturesEnabled(true);

        // Constrain the camera target to the Sweden bounds.
        LatLngBounds swe = new LatLngBounds(
                new LatLng(56.1331192, 10.5930952), new LatLng(70.0599699, 24.1776819));
        googleMap.setLatLngBoundsForCameraTarget(swe);

        // Set zoom  limitations
        googleMap.setMinZoomPreference(5.0f);
        googleMap.setMaxZoomPreference(15.0f);

        if (currentCP != null)
            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(currentCP));
        else // Move camera to the center of Sweden
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(swe.getCenter(), 5));
    }


    /**
     * Adds the users location to the map
     * if it is allowed and the user wants it.
     */
    private void addLocation() {
        if(showLocation){
            if (activity.checkLocationPermission()) {
                googleMap.setMyLocationEnabled(true);
            }
        }
        else
            googleMap.setMyLocationEnabled(false);
    }

    /**
     * Removes the markers.
     */
    private void removeMarkers(){
        googleMap.clear();
        setMapType();
    }

    /**
     * Adds markers to the map, if the user wants it.
     * Sets a click listener on the info window of the markers.
     */
    private void addMarkers(){
        if(showMarkers){
            for(Fjallstation f : fjallstationer.getFjallstationer()){
                googleMap.addMarker(
                        new MarkerOptions()
                                .position(f.getLatLng())
                                .title(f.getName()));
            }
            googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    Fjallstation fjallstation = fjallstationer.getFjallstationer()
                            .get(Integer.parseInt(marker.getId()
                                    .replaceAll("[^\\d.]", "")));
                    Intent intent = new Intent(getActivity(), StationActivity.class);
                    intent.putExtra("station", fjallstation);
                    startActivity(intent);
                }
            });
        }
    }

    /**
     * Sets the type of map that should be shown.
     * 0 = Lantmäteriets map
     * 1 = Normal google maps
     * 2 = Satellite google maps
     * 3 = Terrain google maps
     * 4 = Hybrid google maps
     */
    private void setMapType(){
        switch (mapType) {
            case 0:
                googleMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                TileProvider tileProvider = new UrlTileProvider(256, 256) {
                    @Override
                    public URL getTileUrl(int x, int y, int zoom) {
                        String s = String.format(getLantmaterietUrl(), zoom, y, x);
                        try {
                            return new URL(s);
                        } catch (MalformedURLException e) {
                            throw new AssertionError(e);
                        }
                    }
                };
                tileOverlay = googleMap.addTileOverlay(new TileOverlayOptions().tileProvider(tileProvider));
                break;
            case 1:
                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                removeOverlay();
                break;
            case 2:
                googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                removeOverlay();
                break;
            case 3:
                googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                removeOverlay();
                break;
            case 4:
                googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                removeOverlay();
                break;
        }
    }

    /**
     * Removes the overlay, the Lantmäteriet map
     */
    private void removeOverlay(){
        if(tileOverlay != null)
            tileOverlay.remove();
    }

    /**
     * Returns the Lantmäteriet api-string
     * @return API URL
     */
    private String getLantmaterietUrl(){
        return "https://api.lantmateriet.se/open/topowebb-ccby/v1/wmts/token/" +
                API_KEY +
                "/1.0.0/topowebb/default/3857/%d/%d/%d.png";
    }
    /**
     * Converts dp to pixels
     * @param dp the amount of pixels for the given dp.
     * @return
     */
    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    /**
     * Captures the screen of the google map
     * and saves it to the device.
     */
    public void captureScreen() {
        if (activity.isWriteToStoragePermissionGranted()) {
            GoogleMap.SnapshotReadyCallback callback = new GoogleMap.SnapshotReadyCallback() {
                @Override
                public void onSnapshotReady(Bitmap snapshot) {
                    Bitmap bitmap = snapshot;
                    File dir = new File(FILE_PATH);
                    if (!dir.exists())
                        dir.mkdirs();

                    //String name = String.format("%.5f", googleMap.getCameraPosition().target.longitude) + "_"
                      //      + String.format("%.5f", googleMap.getCameraPosition().target.latitude) + ".png";

                    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                    Date date = new Date();
                    String name = dateFormat.format(date).replace(" ", "_").replace("/","") + ".png";

                    File file = new File(dir, name);
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
                        Toast.makeText(getActivity(),
                                "Kartan har sparats!", Toast.LENGTH_LONG)
                                .show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(),
                                "Det gick inte att spara kartan!", Toast.LENGTH_LONG);
                    }
                }
            };
            googleMap.snapshot(callback);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    /**
     * Enables and shows the saved map icon on the toolbar.
     * Sets a click listener that capures the screen.
     * @param menu The toolbar
     */
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        save = menu.findItem(R.id.save_map);
        save.setVisible(true);
        save.setEnabled(true);
        save.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                captureScreen();
                return false;
            }
        });
    }

    /**
     * Saves Fjallstationer and
     * the current fragment to the
     * @param saveInstanceState
     */
    @Override
    public void onSaveInstanceState(Bundle saveInstanceState) {
        super.onSaveInstanceState(saveInstanceState);
        saveInstanceState.putFloat("bearing",googleMap.getCameraPosition().bearing);
        saveInstanceState.putFloat("tilt",googleMap.getCameraPosition().tilt);
        saveInstanceState.putFloat("zoom",googleMap.getCameraPosition().zoom);
        saveInstanceState.putDouble("latitude",googleMap.getCameraPosition().target.latitude);
        saveInstanceState.putDouble("longitude",googleMap.getCameraPosition().target.longitude);
    }
}