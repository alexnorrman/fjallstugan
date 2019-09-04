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


public class Fjallstation implements Parcelable{

    private String name;
    private String adress;
    private String email;
    private String phoneNr;
    private String url;
    private String imgUrl;
    private double lat;
    private double lng;

    /**
     * Constructor for the Fjallstation-model.
     * @param name Name of the station.
     * @param adress The adress for the station.
     * @param email Email for the station.
     * @param phoneNr Phone number to the station.
     * @param url Website for the station.
     * @param imgUrl URL for the image of the station.
     * @param lat Latitude of the stations position.
     * @param lng Longitude of the stations position.
     */
    public Fjallstation(String name, String adress, String email, String phoneNr,
                        String url, String imgUrl, double lat, double lng){
        this.name = name;
        this.adress = adress;
        this.email = email;
        this.phoneNr = phoneNr;
        this.url = url;
        this.imgUrl = imgUrl;
        this.lat = lat;
        this.lng = lng;
    }

    /**
     * Returns the coordinates, in LatLng, for the position of the station.
     * @return latitude and longitude.
     */
    public com.google.android.gms.maps.model.LatLng getLatLng() {
        return new LatLng(lat, lng);
    }

    /**
     * Returns the adress for the station.
     * @return adress.
     */
    public String getAdress() {
        return adress;
    }

    /**
     * Returns the email for the station.
     * @return email.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Returns the name for the station.
     * @return name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the phone number for the station.
     * @return phone number.
     */
    public String getPhoneNr() {
        return phoneNr;
    }

    /**
     * Returns the website for the station.
     * @return url.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Returns the URL for the stations image.
     * @return image-url.
     */
    public String getImgUrl(){
        return imgUrl;
    }

    /** Method for Parcelable.
     * @return int = 0.
     */
    public int describeContents() {
        return 0;
    }

    /**
     * Flatten this fjallstation in to a Parcel
     * @param out The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written
     */
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(name);
        out.writeString(adress);
        out.writeString(email);
        out.writeString(phoneNr);
        out.writeString(url);
        out.writeString(imgUrl);
        out.writeDouble(lat);
        out.writeDouble(lng);
    }
    /**
     * Constructor used to initialize the fjallstation
     * again after recreation.
     * @param in Parcel with the fjallstation to recreate
     */
    private Fjallstation(Parcel in){
        name = in.readString();
        adress = in.readString();
        email = in.readString();
        phoneNr = in.readString();
        url = in.readString();
        imgUrl = in.readString();
        lat = in.readDouble();
        lng = in.readDouble();
    }

    /**
     * Interface that must be implemented and provided as a public CREATOR field
     * that generates instances of the Parcelable fjallstation class from a Parcel.
     */
    public static final Creator<Fjallstation> CREATOR = new Creator<Fjallstation>() {

        // Create a new instance of the Parcelable class.
        @Override
        public Fjallstation createFromParcel(Parcel in) {
            return new Fjallstation(in);
        }

        // Create a new array of the Parcelable class.
        @Override
        public Fjallstation[] newArray(int size) {
            return new Fjallstation[size];
        }
    };
}
