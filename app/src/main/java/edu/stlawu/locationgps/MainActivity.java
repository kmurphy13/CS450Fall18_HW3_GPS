package edu.stlawu.locationgps;

import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.tv_lat = findViewById(R.id.tv_lat);
        this.tv_lon = findViewById(R.id.tv_lon);

        if (handler == null) {
            this.handler = new LocationHandler();
            this.handler.addObserver(this);
        }
    }


    @Override
    public void update(Observable observable, Object o) {

    }
}
