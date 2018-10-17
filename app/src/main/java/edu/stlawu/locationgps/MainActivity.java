package edu.stlawu.locationgps;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;

import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;


import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;


import java.text.DecimalFormat;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity
        extends AppCompatActivity
        implements Observer {


    private TextView tv_currentLocation;
    private TextView tv_startingLocation;
    private TextView tv_instantVelocity;
    private TextView blankspace;
    private Button controlButton;
    private LinearLayout ll_info;

    private Timer t;
    private Counter ctr;

    private Observable location;
    private LocationHandler handler = null;
    private final static int PERMISSION_REQUEST_CODE = 999;

    private boolean permissions_granted;
    private final static String LOGTAG = MainActivity.class.getSimpleName();

    private Location startLocation = new Location("");
    private Location currentLocation = new Location("");
    private Location previousLocation = new Location("");

    private double velocityFromStart;
    private double velocityFromPrev;
    private double distanceFromStart;
    private double distanceFromPrev;
    private double recentTime = 0;

    private Boolean justStarted = true;
    private Boolean timerStarted = false;



    //Rmei was here :)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);



        this.tv_startingLocation = findViewById(R.id.startingLocation);
        this.tv_currentLocation = findViewById(R.id.currentLocation);
        this.tv_instantVelocity = findViewById(R.id.instantVelocity);

        if (handler == null) {
            this.handler = new LocationHandler(this);
            this.handler.addObserver(this);
        }


        // check permissions
        if (checkSelfPermission(
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_CODE
            );
        }


        final TextView tv_distances = findViewById(R.id.distances);
        final ScrollView sv_distances = findViewById(R.id.sv_distances);

        controlButton = findViewById(R.id.control_button);
        controlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (controlButton.getText().equals("Start")) {
                    tv_startingLocation.setText("Starting Location: " + Double.toString(startLocation.getLatitude()) + "," + Double.toString(startLocation.getLongitude()));
                    controlButton.setText("Checkpoint");
                    previousLocation.setLatitude(startLocation.getLatitude());
                    previousLocation.setLongitude(startLocation.getLongitude());
                    t = new Timer();
                    ctr = new Counter();
                    t.scheduleAtFixedRate(ctr,0,1000);
                    timerStarted = true;


                    Toast toast = Toast.makeText(MainActivity.this,
                            "Starting location recorded.",
                            Toast.LENGTH_SHORT);
                    toast.show();

                } else if (controlButton.getText().equals("Checkpoint")) {

                    distanceFromStart = startLocation.distanceTo(currentLocation);
                    distanceFromPrev = previousLocation.distanceTo(currentLocation);

                    DecimalFormat df = new DecimalFormat("00.00");

                    velocityFromStart = distanceFromStart/ctr.count;
                    velocityFromPrev = distanceFromPrev/(ctr.count-recentTime);


                    tv_distances.append("\t\t\t" + df.format(distanceFromStart) + "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t"
                            + df.format(distanceFromPrev) + "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t"+
                            df.format(velocityFromStart) + "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t"+
                            df.format(velocityFromPrev)+"\n"+
                            "------------------------------------------------------------------------------------------------------------------------------------"+"\n");
                    sv_distances.fullScroll(View.FOCUS_DOWN);
                    previousLocation.setLatitude(currentLocation.getLatitude());
                    previousLocation.setLongitude(currentLocation.getLongitude());
                    recentTime = ctr.count;

                    Toast toast = Toast.makeText(MainActivity.this,
                            "Checkpoint recorded.",
                            Toast.LENGTH_SHORT);

                    toast.show();
                }
            }

        });

        ImageButton infoButton = findViewById(R.id.info_button);
        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // inflate the layout of the popup window
                LayoutInflater inflater = (LayoutInflater)
                        getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView = inflater.inflate(R.layout.popup_window, null);

                // create the popup window
                int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                boolean focusable = true; // lets taps outside the popup also dismiss it
                final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

                // show the popup window
                // which view you pass in doesn't matter, it is only used for the window tolken
                popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

                // dismiss the popup window when touched
                popupView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        popupWindow.dismiss();
                        return true;
                    }
                });

            }
        });
    }

    public boolean isPermissions_granted() {
        return permissions_granted;
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            // we have only asked for FINE LOCATION
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                this.permissions_granted = true;
                Log.i(LOGTAG, "Fine location permisssion granted.");
            } else {
                this.permissions_granted = false;
                Log.i(LOGTAG, "Fine location permisssion not granted.");
            }
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        this.ll_info = findViewById(R.id.ll_info);

        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            ll_info.setWeightSum(5);
            TextView blankspace = new TextView(this);

            ViewGroup.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);

            blankspace.setLayoutParams(params);
            ll_info.addView(blankspace);

        } else {
            ll_info.setWeightSum(4);
            ll_info.removeView(blankspace);
        }


    }

    @Override
    public void update(Observable observable,
                       Object o) {
        if (observable instanceof LocationHandler) {
            Location l = (Location) o;
            final double lat = l.getLatitude();
            final double lon = l.getLongitude();


            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (justStarted) {
                        currentLocation.setLatitude(lat);
                        currentLocation.setLongitude(lon);
                        tv_currentLocation.setText("Current Location: " + Double.toString(currentLocation.getLatitude()) + "," + Double.toString(currentLocation.getLongitude()));
                        startLocation.setLatitude(lat);
                        startLocation.setLongitude(lon);
                        controlButton.setEnabled(true);
                        justStarted = false;
                    } else {
                        currentLocation.setLatitude(lat);
                        currentLocation.setLongitude(lon);
                        tv_currentLocation.setText("Current Location: " + Double.toString(currentLocation.getLatitude()) + "," + Double.toString(currentLocation.getLongitude()));
                        if(timerStarted){
                            double distance = startLocation.distanceTo(currentLocation);
                            double instantVelocity = distance/ctr.count;
                            DecimalFormat df = new DecimalFormat("00.00");
                            tv_instantVelocity.setText("Current Velocity: "+df.format(instantVelocity));
                        }

                    }
                }
            });
        }
    }

    class Counter extends TimerTask {
        // Set the count to the count from the main activity
        private int count = 0;


        @Override
        public void run() {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Increase the count
                    count++;
                }
            });
        }
    }
}
