package midnet.fsclient;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MapsActivity extends FragmentActivity implements View.OnClickListener, SettingsDialog.SettingsDialogListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private Boolean running = false;

    private Button run;
    private EditText debug;

    private IPAddressText ipField;
    private EditText portField;
    private EditText socketField;

    private String ipServer;
    private int portServer;
    private String feedServer;

    private Socket sock;

    private boolean showing = false;

    private HashMap<String,User> users;

    public static final String STATIC_IP_SERVER = "192.168.49.52";
    public static final int STATIC_PORT_SERVER = 8080;
    public static final String STATIC_FEED_SERVER = "123456123456123456123456";

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.animateCamera( CameraUpdateFactory.newLatLngZoom(new LatLng(48.887937, 2.339916), 17.0f));
        //mMap.animateCamera(CameraUpdateFactory.zoomTo(17.0f));
        //mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();

        //Handle Run button
        run = (Button) findViewById(R.id.buttonRun);
        run.setOnClickListener(this);

        ipField = (IPAddressText) findViewById(R.id.ipText);
        portField = (EditText) findViewById(R.id.portText);
        debug = (EditText) findViewById(R.id.debugTest);
        socketField = (EditText) findViewById(R.id.socketText);

        ipServer ="";
        portServer=0;
        feedServer="";

        users= new HashMap<String,User>();
    }

    @Override
    protected void onResume() {
        super.onResume();
        retrievePreferences();
        setUpMapIfNeeded();
    }

    @Override
    public void onSettingsDialogClick(DialogFragment dialog) {
        retrievePreferences();
    }
    private void retrievePreferences() {
        SharedPreferences preferences = getSharedPreferences("Settings", MODE_PRIVATE);
        ipServer = preferences.getString("ipServer", STATIC_IP_SERVER);
        portServer = preferences.getInt("portServer", STATIC_PORT_SERVER);
        feedServer = preferences.getString("feedServer", STATIC_FEED_SERVER);
        ipField.setText(ipServer);
        portField.setText(Integer.toString(portServer));
    }

    /*
     * OPTIONS MENU
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_items, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.Run:
                if(!running){
                    if(connect())
                        item.setTitle("Stop");
                }else{
                    item.setTitle("Run");
                    cleanClose();
                }
                return true;
            case R.id.Parameters:
                showSettingsDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    public void showSettingsDialog(){
        SettingsDialog newFragment = new SettingsDialog();
        newFragment.show(getFragmentManager(), "Set Settings");
    }

    // RUN Button listener
    @Override
    public void onClick(View v) {
        if(!running)
            connect();
        else
            cleanClose();
    }

    public boolean connect() {
        if( !checkParameters())
            return false;

        debug.setText("Server addr = " + ipServer + ":" + portServer);

        try {
            setupSocket();
        }
        catch (URISyntaxException e){
            debug.setText("URISyntaxException : " + e.toString());
            return false;
        }

        running = true;
        run.setText("Stop");
        run.setTextColor(Color.rgb(0x99, 0x33, 0x33));

        return true;
       // MenuItem item = (MenuItem) findViewById(R.id.Run);
       // item.setTitle("Stop2");
    }

    public void cleanClose(){
        running = false;

        run.setText("Start");
        run.setTextColor(Color.rgb(0x33, 0x99, 0x33));
        debug.setText("");

       // MenuItem item = (MenuItem) findViewById(R.id.Run);
       // item.setTitle("Run2");

        sock.disconnect();

        users.clear();
        mMap.clear();
    }

    public boolean checkParameters(){
        // ipServer = ipField.getText().toString();
        // String port = portField.getText().toString();
        // feedServer = feedField.getText().toString();
        if(ipServer.isEmpty() || feedServer.isEmpty())
        {
            debug.setText("Server IP/feed field is EMPTY !");
            return false;
        }
        //portServer = Integer.parseInt(port);
        if (portServer < 1 || portServer > 65535)
        {
            debug.setText("Server Port must be between 0 & 65535 !");
            return false;
        }
        if(! feedServer.matches("^[0-9a-f]+$"))
        {
            debug.setText("Feed ID is not an hexa sequence !");
            return false;
        }
        return true;
    }

    public void setupSocket() throws URISyntaxException {

        IO.Options opts = new IO.Options();
        opts.reconnection = true;

        final Socket socket = IO.socket("http://"+ipServer+":"+portServer);
        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        socketField.setText("Connected");}});
            }

        })
                .on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                socketField.setText("Disconnected");
                            }
                        });
                    }
                })
                .on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                socketField.setText("Connect Error");
                                cleanClose();
                                if (!showing) {
                                    showing = true;
                                    new AlertDialog.Builder(MapsActivity.this)
                                            .setTitle("Error !")
                                            .setMessage("Connect error. Please change server preferences")
                                            .setIcon(android.R.drawable.ic_dialog_alert)
                                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    Toast.makeText(getApplicationContext(), "Reset connection", Toast.LENGTH_SHORT).show();
                                                    showing = false;
                                                }
                                            })
                                            .show();
                                }
                            }
                        });
                    }
                })
                .on(Socket.EVENT_CONNECT_TIMEOUT, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                socketField.setText("Connect Timeout");
                                cleanClose();
                                if(!showing) {
                                    showing = true;
                                    new AlertDialog.Builder(MapsActivity.this)
                                            .setTitle("Error !")
                                            .setMessage("Connect timeout. Please change server preferences")
                                            .setIcon(android.R.drawable.ic_dialog_alert)
                                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    Toast.makeText(getApplicationContext(), "Reset connection", Toast.LENGTH_SHORT).show();
                                                    showing = false;
                                                }
                                            })
                                            .show();
                                }
                            }
                        });
                    }
                })
                .on("entry_post", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        final JSONObject json = (JSONObject) args[0];
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    String name = json.getString("author");
                                    JSONObject description = new JSONObject(json.getString("description"));

                                    //Log.d("JSON", "author : " + name + " lat = "+ description.getDouble("latitude") + " , lo = " + description.getDouble("longitude"));
                                    debug.setText(name + " New update ! ");

                                    //Retrieve position
                                    boolean first = users.isEmpty();
                                    LatLng position = new LatLng(description.getDouble("latitude"), description.getDouble("longitude"));
                                    if(first)
                                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 14.0f));

                                    //Retrieve user
                                    User user = users.get(name);
                                    if (user == null) {
                                        users.put(name, new User(name));
                                        user = users.get(name);
                                    }

                                    //Marker update
                                    user.addPosition(position);
                                    Marker old = user.getOldMarker();
                                    if(old != null)
                                        old.remove();
                                    user.setOldMarker(mMap.addMarker(user.getMarker()));

                                    //Line update
                                    if(user.getPolyline() != null)
                                        user.getPolyline().remove();
                                    user.setPolyline(mMap.addPolyline(user.getLine()));

                                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(updateBounds(), 100));

                                } catch (JSONException e) {
                                    debug.setText( e.toString());
                                }
                            }});
                    }

                });

        socket.connect();
        sock = socket;
    }

    private LatLngBounds updateBounds(){
        //double minLat=Double.MAX_VALUE,minLong=Double.MAX_VALUE;
        //double maxLat=Double.MIN_VALUE,maxLong=Double.MIN_VALUE;

        double minLat = 100.0 , minLong = 100.0;
        double maxLat = -100.0 , maxLong = -100.0;
        for(User user : users.values())
        {
            ArrayList<LatLng> list = user.getList();
            //Log.d("BOUNDS", "User " + user.getName() + " has " + list.size() + " positions");
            for(LatLng pos : list)
            {
                //Log.d("BOUNDS", "Comparing " +  pos.toString());
                minLat = (minLat > pos.latitude ? pos.latitude : minLat);
                maxLat = (maxLat < pos.latitude ? pos.latitude : maxLat);
                minLong = (minLong > pos.longitude ? pos.longitude : minLong);
                maxLong = (maxLong < pos.longitude ? pos.longitude : maxLong);
            }
        }
        Log.d("BOUNDS ", "(" + minLat + "," + minLong + ") (" + maxLat+ "," + maxLong + ")");
        return new LatLngBounds(new LatLng(minLat,minLong),new LatLng(maxLat,maxLong));
    }

}
