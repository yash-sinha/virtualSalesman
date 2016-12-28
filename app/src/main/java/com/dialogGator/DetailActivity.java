package com.dialogGator;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.util.List;

public class DetailActivity extends AppCompatActivity {
    Bitmap b;
    ImageView img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Product product= new Product();
        String productId = getIntent().getStringExtra(MainFragment.PRODUCT_ID);
        /*DataProvider dp = new DataProvider(this);
        dp.getProducts();*/
        List<Product> products =  ((ListenerTask) getApplication()).getProductList();
        for(Product prod : products){
            if(prod.getProductId().equalsIgnoreCase(productId)){
                product=prod;
            }
        }

        TextView tv = (TextView) findViewById(R.id.nameText);
        tv.setText(product.getName());

        TextView descView = (TextView) findViewById(R.id.descriptionText);
        descView.setText(product.getDescription());

        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        String price = formatter.format(product.getPrice());
        TextView priceText = (TextView) findViewById(R.id.priceText);
        priceText.setText(price);

        img = (ImageView) findViewById(R.id.imageView);
        new DownloadImageTask(img).execute(product.getImgUrl());
        //b= getbmpfromURL("http://pbs.twimg.com/profile_images/614526496112144385/O_p28XM2.jpg");
        //img.setImageBitmap(b);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Item added to cart", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.main_bg_color));
    }

    public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        private ImageView imageView;
        private Bitmap image;

        public DownloadImageTask(ImageView imageView) {
            this.imageView = imageView;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                image = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                image = null;
            }
            return image;
        }

        @SuppressLint("NewApi")
        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                imageView.setImageBitmap(result);
            }
        }
    }


//    private void getBitmapFromAsset(ImageView iv) {
////        AssetManager assetManager = getAssets();
////        InputStream stream = null;
////
////        try {
////            stream = assetManager.open(productId + ".png");
////            return BitmapFactory.decodeStream(stream);
////        } catch (IOException e) {
////            e.printStackTrace();
////            return null;
////        }
//
//        new AsyncTask<ImageView, Void, ImageView>() {
//            Bitmap bmp;
//
//
//            @Override
//            protected ImageView doInBackground(ImageView... iv) {
//                try {
//                    InputStream in = new URL("http://img.wonkette.com/wp-content/uploads/2016/08/nbc-fires-donald-trump-after-he-calls-mexicans-rapists-and-drug-runners.jpg").openStream();
//                    bmp = BitmapFactory.decodeStream(in);
//
//                } catch (Exception e) {
//                    // log error
//                }
//                return null;
//            }
//
//            @Override
//            protected void onPostExecute(ImageView iv) {
//                if (bmp != null)
//                    iv.setImageBitmap(bmp);
//            }
//
//        }.execute();
//    }

}
