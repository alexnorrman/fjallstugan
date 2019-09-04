/**
 * Course:  Development for mobile applications.
 *          Umeå University
 *          Summer 2019
 * @author Alex Norrman
 */

package se.umu.cs.alno0025.fjallstugan;


import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;


public class SavedMapsGridFragment extends Fragment {

    private static final String FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() +
            "/fjallstugan/kartor/";

    private RecyclerView mImgRecyclerView;
    private SavedMapsGridFragment.ImgAdapter mAdapter;
    private List<SavedMap> savedMaps;
    private File[] allFiles = null;

    private View view;
    private MainActivity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_grid, container, false);

        activity = (MainActivity)getActivity();
        savedMaps = new ArrayList<>();

        // If the permission to read/write to the device and
        // files are found, load the files
        if(activity.isWriteToStoragePermissionGranted()){
            File folder = new File(FILE_PATH);
            if(folder.exists())
                allFiles = folder.listFiles(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        return (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png"));
                    }
                });
            if(allFiles != null){
                for(File f : allFiles){
                    SavedMap savedMap = new SavedMap(f.getPath(), f.getName(), BitmapFactory.decodeFile(f.getPath()));
                    savedMaps.add(savedMap);
                }
            }
        }

        mImgRecyclerView = (RecyclerView) view
                .findViewById(R.id.img_recycler_view);
        mImgRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),2));

        updateUI();

        return view;
    }

    /**
     * Updates the UI
     */
    private void updateUI() {
        if(savedMaps.size() ==  0)
            view.findViewById(R.id.empty).setAlpha(1);
        else
            view.findViewById(R.id.empty).setAlpha(0);

        mAdapter = new SavedMapsGridFragment.ImgAdapter();
        mImgRecyclerView.setAdapter(mAdapter);
    }

    /**
     * Recycler for the grid.
     */
    private class ImgHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {

        private ImageView savedMapImageView;

        public ImgHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.fragment_grid_item, parent, false));
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            savedMapImageView = (ImageView) itemView.findViewById(R.id.map_img);
        }

        public void bind(SavedMap saved) {

            savedMapImageView.setImageBitmap(saved.getImg());
            savedMapImageView.setVisibility(View.VISIBLE);
        }

        /**
         * Normal click to go the SavedMapsActivity
         * @param view
         */
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(getActivity(), SavedMapsActivity.class);
            int i = 0;
            for(SavedMap savedMap : savedMaps){
                i++;
                intent.putExtra("path"+String.valueOf(i),savedMap.getPath());
            }
            intent.putExtra("count",i);
            intent.putExtra("position",getAdapterPosition());
            startActivity(intent);
        }

        /**
         * Long Click for delete the image.
         * @param view
         * @return
         */
        @Override
        public boolean onLongClick(View view) {
            // Handle long click
            // Return true to indicate the click was handled
            itemView.setAlpha((float) 0.2);
            new AlertDialog.Builder(activity)
                    .setTitle("Ta bort sparad karta.")
                    .setIcon(R.drawable.tool_delete)
                    .setMessage("Vill du ta bort den sparade kartan?")
                    .setNegativeButton("Nej", null)
                    .setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            File file = new File(savedMaps.get(getAdapterPosition()).getPath());
                            boolean isSuccess = file.delete();
                            if (isSuccess) {
                                savedMaps.remove(getAdapterPosition());
                                updateUI();
                            }
                        }
                    })
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialogInterface) {
                            itemView.setAlpha((float) 1);
                        }
                    })
                    .create()
                    .show();
            return true;
        }
    }

    /**
     * Adapter for the grid.
     */
    private class ImgAdapter extends RecyclerView.Adapter<SavedMapsGridFragment.ImgHolder> {

        public ImgAdapter() {

        }

        @Override
        public SavedMapsGridFragment.ImgHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new SavedMapsGridFragment.ImgHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(SavedMapsGridFragment.ImgHolder holder, int position) {
            holder.bind(savedMaps.get(position));
            holder.itemView.setLongClickable(true);
        }

        @Override
        public int getItemCount() {
            return savedMaps.size();
        }
    }
}
