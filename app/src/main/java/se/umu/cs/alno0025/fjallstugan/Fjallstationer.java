/**
 * Course:  Development for mobile applications.
 *          Umeå University
 *          Summer 2019
 * @author Alex Norrman
 */

package se.umu.cs.alno0025.fjallstugan;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Fjallstationer implements Parcelable {

    private List<Fjallstation> fjallstationer;
    private LatLng cameraPostition;
    private double lat;
    private double lng;

    /**
     * Constructor for Fjallstationer
     * Gets all the information of all the stations from JSON string and
     * adds them into a list.
     * @param json JSON string.
     */
    public Fjallstationer(String json){
        fjallstationer = new ArrayList<>();

        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonObjectArray = jsonObject.getJSONArray("stationer");

            for (int i = 0; i < jsonObjectArray.length(); i++) {
                JSONObject currentObject = jsonObjectArray.getJSONObject(i);
                fjallstationer.add(new Fjallstation(
                        currentObject.getString("name"),
                        currentObject.getString("adress"),
                        currentObject.getString("epost"),
                        currentObject.getString("telefon"),
                        currentObject.getString("url"),
                        currentObject.getString("imgUrl"),
                        currentObject.getDouble("lat"),
                        currentObject.getDouble("lng")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Constrain the camera target to the Sweden bounds.
        LatLngBounds swe = new LatLngBounds(
                new LatLng(56.1331192, 10.5930952), new LatLng(70.0599699, 24.1776819));
        cameraPostition = swe.getCenter();
        sortList();
    }

    /**
     * Sorts the list in alphabetical order,
     * but do not take into account the first 4 characters ("STF ")
     */
    private void sortList(){
        Collections.sort(fjallstationer, new Comparator<Fjallstation>() {
            @Override
            public int compare(Fjallstation f1, Fjallstation f2) {
                return f1.getName().substring(3,f1.getName().length()-3)
                        .compareTo(f2.getName().substring(3,f2.getName().length()-3));
            }
        });
    }

    /**
     * Sets the current camera position.
     * Used to set the camera position after a activity/fragment is recreated.
     * @param cameraPostition
     */
    public void setCameraPostition(LatLng cameraPostition) {
        this.cameraPostition = cameraPostition;
        this.lat = cameraPostition.latitude;
        this.lng = cameraPostition.longitude;
    }

    /**
     * Gets the camera position, in LatLng.
     * @return
     */
    public LatLng getCameraPostition() {
        return cameraPostition;
    }

    /**
     * Sets latitude and longitude.
     * @param lat
     * @param lng
     */
    private void setLatLng(double lat, double lng){
        setCameraPostition(new LatLng(lat,lng));
    }
    /**
     * @return List of fjallstationer.
     */
    public List<Fjallstation> getFjallstationer() {
        return fjallstationer;
    }

    /** Method for Parcelable.
     * @return int = 0.
     */
    public int describeContents() {
        return 0;
    }

    /**
     * Flatten this dice in to a Parcel
     * @param out The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written
     */
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeTypedList(fjallstationer);
        out.writeDouble(lat);
        out.writeDouble(lng);
    }
    /**
     * Constructor used to initialize the die
     * again after recreation.
     * @param in Parcel with the dice to recreate
     */
    private Fjallstationer(Parcel in){
        fjallstationer = in.createTypedArrayList(Fjallstation.CREATOR);
        lat = in.readDouble();
        lng = in.readDouble();
    }

    /**
     * Interface that must be implemented and provided as a public CREATOR field
     * that generates instances of the Parcelable dice class from a Parcel.
     */
    public static final Creator<Fjallstationer> CREATOR = new Creator<Fjallstationer>() {

        // Create a new instance of the Parcelable class.
        @Override
        public Fjallstationer createFromParcel(Parcel in) {
            return new Fjallstationer(in);
        }

        // Create a new array of the Parcelable class.
        @Override
        public Fjallstationer[] newArray(int size) {
            return new Fjallstationer[size];
        }
    };
}
