package midnet.fsclient;

import android.graphics.Color;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by tai on 11/09/15.
 */
public class User {

    private String name;
    private Marker marker;
    private ArrayList<LatLng> history;
    private int color;
    private float hue;
    private PolylineOptions options;

    public User(String name) {
        this.name = name;
        this.marker = null;
        this.history = new ArrayList<LatLng>();
        Random rand = new Random();
        this.color = Color.argb(255, rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
        this.hue = rand.nextFloat() * 360;
        options = new PolylineOptions().width(5).color(color);
        this.line = null;
    }

    public void setColor(int color) {this.color = color;}
    public void setName(String name) {this.name = name;}
    public void setOldMarker(Marker marker){this.marker = marker;}
    public void addPosition(LatLng pos) {
        this.history.add(pos);
        options.add(pos);
    }

    public Marker getOldMarker() {return marker;}
    public String getName() {return name;}
    public int getColor() {return color;}
    public float getHue() {return hue;}
    public ArrayList<LatLng> getList() {return history;}
    public PolylineOptions getLine() {return options;}
    public LatLng lastPosition() {
        return history.get(history.size() - 1);
    }
    public MarkerOptions getMarker() {
        return new MarkerOptions()
                .position(this.lastPosition())
                .icon(BitmapDescriptorFactory.defaultMarker(this.getHue()));
    }


    private Polyline line;
    public void setPolyline(Polyline line) {this.line = line;}
    public Polyline getPolyline(){return line;}
}