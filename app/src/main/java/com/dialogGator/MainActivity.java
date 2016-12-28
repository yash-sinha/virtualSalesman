package com.dialogGator;

import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.voice.APIAITaskAgent;
import com.voice.TTS;

import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.inject.Singleton;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    NavigationView navigationView = null;
    Toolbar toolbar = null;
    ShowcaseView showcaseView;
    FloatingActionButton fab = null;
    private int counter = 0;
    final int SHOWCASEVIEW_ID = 28;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Set the fragment initially
        MainFragment fragment = new MainFragment();
        android.support.v4.app.FragmentTransaction fragmentTransaction =
                getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment,"scheduleFragment");
        //fragmentTransaction.addToBackStack("scheduleFragment");
        fragmentTransaction.commit();
        //To Handle ShowCaseView positions
        int margin = ((Number) (getResources().getDisplayMetrics().density * 16)).intValue();
        fab = (FloatingActionButton) findViewById(R.id.fab);
        RelativeLayout.LayoutParams lps = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lps.setMargins(margin, 1550, margin, margin);
        //lps.setMargins(400, 800, 200, 200);
        showcaseView = new ShowcaseView.Builder(this).withMaterialShowcase()
                .setTarget(new ViewTarget(findViewById(R.id.fab)))
                .setStyle(R.style.ShowCaseViewStyle)
                .setOnClickListener(this)
                .setContentText("\n\nTap the button to speak.\n\nYou can ask for the product you want to buy.\n\nExample: Show me a blue shirt.\n\nSay 'Open product number 123' for product details.\n\nSay 'Help!' if you need help with something.\n\nSay 'Start Over' to clear all filters.")
                .setContentTitle("Things you can say.")
                .build();
        showcaseView.setButtonPosition(lps);
        //Setup Logging
        if ( isExternalStorageWritable() ) {

            File appDirectory = new File( Environment.getExternalStorageDirectory() + "/VirtualSA" );
            File logDirectory = new File( appDirectory + "/log" );
            File logFile = new File( logDirectory, "logcat" + System.currentTimeMillis() + ".txt" );

            // create app folder
            if ( !appDirectory.exists() ) {
                appDirectory.mkdir();
            }

            // create log folder
            if ( !logDirectory.exists() ) {
                logDirectory.mkdir();
            }

            // clear the previous logcat and then write the new one to the file
            try {
                Process process = Runtime.getRuntime().exec("logcat -c");
                process = Runtime.getRuntime().exec("logcat -f " + logFile);
            } catch ( IOException e ) {
                e.printStackTrace();
            }

        } else if ( isExternalStorageReadable() ) {
            // only readable
        } else {
            // not accessible
        }

        /*FragmentTransaction fragmentTransaction =  getActivity().getSupportFragmentManager().beginTransaction();
        Fragment scheduleFragment = new ScheduleFragment();
        fragmentTransaction.replace(R.id.content_container, scheduleFragment, "scheduleFragment");
        fragmentTransaction.addToBackStack("scheduleFragment");
        fragmentTransaction.commit();
        */
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TTS.init(getApplicationContext());
        ProductAttributes.init();
        String attributes = "";
        Toolbar searchBar = (Toolbar) findViewById(R.id.search_bar);
        searchBar.setTitle("Filters: "+ attributes);
        //DBHelper.getInstance(getApplicationContext());
        final APIAITaskAgent apiaiTaskAgent = new APIAITaskAgent(this);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TTS.stopSpeaking();
                showcaseView.hide();
                apiaiTaskAgent.startRecognition();
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);

        //How to change elements in the header programatically
        View headerView = navigationView.getHeaderView(0);
        TextView emailText = (TextView) headerView.findViewById(R.id.email);
        emailText.setText("virtualsalesman@gmail.com");

        navigationView.setNavigationItemSelectedListener(this);


        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.main_bg_color));


    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if ( Environment.MEDIA_MOUNTED.equals( state ) ) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if ( Environment.MEDIA_MOUNTED.equals( state ) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals( state ) ) {
            return true;
        }
        return false;
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
/*
            int count = getFragmentManager().getBackStackEntryCount();

            if (count == 0) {
                super.onBackPressed();
                //additional code
            } else {
                getFragmentManager().popBackStack();
            }*/

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.action_search);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainFragment fragment = new MainFragment();
                android.support.v4.app.FragmentTransaction fragmentTransaction =
                        getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, fragment,"scheduleFragment");
                //fragmentTransaction.addToBackStack("scheduleFragment");
                fragmentTransaction.commit();
            }
        });*/
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        else if(id==R.id.action_search){
            //Toast.makeText(this, "Hello!", Toast.LENGTH_LONG).show();
            MainFragment fragment = new MainFragment();
            android.support.v4.app.FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment,"scheduleFragment");
            APIAITaskAgent.clearFilters();
            //fragmentTransaction.addToBackStack("scheduleFragment");
            fragmentTransaction.commit();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camara) {
            //Set the fragment initially
            MainFragment fragment = new MainFragment();
            android.support.v4.app.FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment,"scheduleFragment");
            //fragmentTransaction.addToBackStack("scheduleFragment");
            fragmentTransaction.commit();
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {
            //Set the fragment initially
            UniversalFragment fragment = new UniversalFragment();
            android.support.v4.app.FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }



    @Override
    public void onClick(View v) {

        showcaseView.hide();
    }



}
