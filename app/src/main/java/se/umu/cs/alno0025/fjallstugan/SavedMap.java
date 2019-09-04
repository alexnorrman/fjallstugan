/**
 * Course:  Development for mobile applications.
 *          Umeå University
 *          Summer 2019
 * @author Alex Norrman
 */
package se.umu.cs.alno0025.fjallstugan;

import android.graphics.Bitmap;

public class SavedMap {
    private String path;
    private String name;
    private Bitmap img;

    /**
     * Constructor of the Saved Map
     * @param path Path to the image file
     * @param name Name of the image file
     * @param img A bitmap of the file
     */
    SavedMap(String path, String name, Bitmap img){
        this.path = path;
        this.name = name;
        this.img = img;
    }

    /**
     * Returns the name of the saved map
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the bitmap of the image
     * @return Bitmap
     */
    public Bitmap getImg() {
        return img;
    }

    /**
     * Returns the path to the image file
     * @return String with path
     */
    public String getPath() {
        return path;
    }
}
