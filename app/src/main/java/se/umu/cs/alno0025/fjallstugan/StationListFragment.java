/**
 * Course:  Development for mobile applications.
 *          Umeå University
 *          Summer 2019
 * @author Alex Norrman
 */

package se.umu.cs.alno0025.fjallstugan;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class StationListFragment extends Fragment {

    private static final String FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() +
            "/fjallstugan/fjallstugor/";

    private Fjallstationer fjallstationer;
    private RecyclerView mFjallRecyclerView;
    private FjallAdapter mAdapter;
    private MainActivity activity;
    private boolean permission;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        activity = (MainActivity)getActivity();
        fjallstationer = activity.getFjallstationer();
        permission = activity.isReadStoragePermissionGranted();

        if(permission){
            for(Fjallstation fs : fjallstationer.getFjallstationer()){
                if(!isImgAlreadyDownloaded(fs.getName()))
                    new DownloadImageTask(fs.getName()).execute(fs.getImgUrl());
            }
        }


        mFjallRecyclerView = (RecyclerView) view
                .findViewById(R.id.station_recycler_view);
        mFjallRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        updateUI();

        return view;
    }

    /**
     * Updates the UI
     */
    private void updateUI() {
        mAdapter = new FjallAdapter();
        mFjallRecyclerView.setAdapter(mAdapter);
    }

    /**
     * Recycler holder for the list.
     */
    private class FjallHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private Bitmap imgBitmap = null;
        private TextView stationNameTextView;
        private ImageView stationImageView;

        public FjallHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.fragment_list_item, parent, false));
            itemView.setOnClickListener(this);

            stationNameTextView = (TextView) itemView.findViewById(R.id.station_name);
            stationImageView = (ImageView) itemView.findViewById(R.id.station_img);
        }

        public void bind(Fjallstation fs) {
            Fjallstation fjallstation = fs;

            if(permission){
                if(isImgAlreadyDownloaded(fs.getName())){
                    String file_path = FILE_PATH + fs.getName().
                            replaceAll("[^a-zA-Z]+", "")+".png";
                    stationImageView.setImageBitmap(BitmapFactory.decodeFile(file_path));
                }
            }
            else{
                new LoadImageTask().execute(fjallstation.getImgUrl());
                stationImageView.setImageBitmap(imgBitmap);
            }
            stationNameTextView.setText(fjallstation.getName());
            stationImageView.setVisibility(View.VISIBLE);
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(getActivity(), StationActivity.class);
            intent.putExtra("station", fjallstationer.getFjallstationer()
                    .get(getAdapterPosition()));
            startActivity(intent);
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
    }

    private class FjallAdapter extends RecyclerView.Adapter<FjallHolder> {

        public FjallAdapter() {

        }

        @Override
        public FjallHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new FjallHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(FjallHolder holder, int position) {
            holder.bind(fjallstationer.getFjallstationer().get(position));
        }

        @Override
        public int getItemCount() {
            return fjallstationer.getFjallstationer().size();
        }
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
}
