package edu.stlawu.locationgps;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.Observable;
import java.util.Observer;

public class MainActivity
        extends AppCompatActivity
        implements Observer{

    private TextView tv_lat;
    private TextView tv_lon;
    private TextView tv_currentLocation;
    private TextView tv_startingLocation;
    private ScrollView scrollView;
    private Button controlButton;

    private Observable location;
    private LocationHandler handler = null;
    private final static int PERMISSION_REQUEST_CODE = 999;

    private boolean permissions_granted;
    private final static String LOGTAG = MainActivity.class.getSimpleName();

    private Location startLocation = new Location("");
    private Location currentLocation = new Location("");

    private Boolean justStarted = true;


    //Remi was here :)


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.tv_startingLocation = findViewById(R.id.startingLocation);
        this.tv_currentLocation = findViewById(R.id.currentLocation);

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
                    new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                    PERMISSION_REQUEST_CODE
            );
        }

        final TextView tv_distances = findViewById(R.id.distances);


        controlButton = findViewById(R.id.control_button);
        controlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(controlButton.getText().equals("Start")){
                    tv_startingLocation.setText("Starting Location: "+Double.toString(startLocation.getLatitude())+","+Double.toString(startLocation.getLongitude()));
                    controlButton.setText("Checkpoint");
                }
                else if(controlButton.getText().equals("Checkpoint")){
                    double distanceFromStart = startLocation.distanceTo(currentLocation);
                    distanceFromStart = Math.round(distanceFromStart*100.0)/100.0;
                    tv_distances.append("\t"+Double.toString(distanceFromStart)+"\n");


                }
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
            }
            else {
                this.permissions_granted = false;
                Log.i(LOGTAG, "Fine location permisssion not granted.");
            }
        }

    }

    @Override
    public void update(Observable observable,
                       Object o) {
        final ScrollView scrollview = ((ScrollView) findViewById(R.id.sv_distances));
        if (observable instanceof LocationHandler) {
            Location l = (Location) o;
                final double lat = l.getLatitude();
                final double lon = l.getLongitude();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(justStarted){
                            startLocation.setLatitude(lat);
                            startLocation.setLongitude(lon);
                            controlButton.setEnabled(true);
                            justStarted = false;
                        }else{
                            currentLocation.setLatitude(lat);
                            currentLocation.setLongitude(lon);
                            tv_currentLocation.setText("Current Location: "+Double.toString(currentLocation.getLatitude())+","+Double.toString(currentLocation.getLongitude()));
                        }
                        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                });


        }
    }




}
