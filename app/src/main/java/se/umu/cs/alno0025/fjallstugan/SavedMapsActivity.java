/**
 * Course:  Development for mobile applications.
 *          Umeå University
 *          Summer 2019
 * @author Alex Norrman
 */

package se.umu.cs.alno0025.fjallstugan;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;

public class SavedMapsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_maps);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setTitle("Sparade kartor");
        }
        ArrayList<Bitmap> bitmaps = new ArrayList<>();
        Intent intent = getIntent();
        for (int i = 1; i <= intent.getIntExtra("count",0); i++) {
            bitmaps.add(BitmapFactory.decodeFile(intent.getStringExtra("path"+i)));
        }

        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        ImageAdapter adapter = new ImageAdapter(this,bitmaps);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(intent.getIntExtra("position",0));
    }

    /**
     * Adapter for the images.
     */
    public class ImageAdapter extends PagerAdapter {
        Context context;
        LayoutInflater mLayoutInflater;
        ArrayList<Bitmap> bitmaps;
        ArrayList<String> names;

        ImageAdapter(Context context, ArrayList<Bitmap> bitmaps) {
            this.bitmaps = bitmaps;
            this.names = names;
            this.context = context;
            mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return bitmaps.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == ((RelativeLayout) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View itemView = mLayoutInflater.inflate(R.layout.saved_maps_fullscreen, container, false);

            ImageView imageView = (ImageView) itemView.findViewById(R.id.imgDisplay);
            imageView.setImageBitmap(bitmaps.get(position));

            container.addView(itemView);

            return itemView;
        }
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((RelativeLayout)object);
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
