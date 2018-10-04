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
import android.widget.TextView;

import java.util.Observable;
import java.util.Observer;

public class MainActivity
        extends AppCompatActivity
        implements Observer {

    private TextView tv_lat;
    private TextView tv_lon;

    private Observable location;
    private LocationHandler handler = null;
    private final static int PERMISSION_REQUEST_CODE = 999;

    private boolean permissions_granted;
    private final static String LOGTAG =
            MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.tv_lat = findViewById(R.id.tv_lat);
        this.tv_lon = findViewById(R.id.tv_lon);

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
        if (observable instanceof LocationHandler) {
            Location l = (Location) o;
            final double lat = l.getLatitude();
            final double lon = l.getLongitude();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv_lat.setText(Double.toString(lat));
                    tv_lon.setText(Double.toString(lon));
                }
            });
        }
    }
}
