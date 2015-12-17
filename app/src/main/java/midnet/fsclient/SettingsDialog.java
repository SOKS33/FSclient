package midnet.fsclient;

/**
 * Created by tai on 11/09/15.
 */
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class SettingsDialog extends DialogFragment{
    private String feedServer;
    private String ipServer;
    private int portServer;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        retrievePreferences();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        final View v = inflater.inflate(R.layout.dialog_settings, null);
        initView(v);
        builder.setView(v)
                // Add action buttons
                .setPositiveButton(R.string.Validate, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        saveSettings(v);
                        mListener.onSettingsDialogClick(SettingsDialog.this);
                    }
                })
                .setNeutralButton(R.string.Reset, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        resetSettings();
                        mListener.onSettingsDialogClick(SettingsDialog.this);
                    }
                })
                .setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SettingsDialog.this.getDialog().cancel();

                    }
                })
        ;
        return builder.create();
    }

    /**
     * Retrieve Preferences from shared Preferences
     */
    private void retrievePreferences() {
        SharedPreferences preferences = getActivity().getSharedPreferences("Settings", Activity.MODE_PRIVATE);

        ipServer = preferences.getString("ipServer",MapsActivity.STATIC_IP_SERVER);
        portServer = preferences.getInt("portServer", MapsActivity.STATIC_PORT_SERVER);
        feedServer = preferences.getString("feedServer", MapsActivity.STATIC_FEED_SERVER);

    }

    private void saveSettings(View v){
        IPAddressText ipField = (IPAddressText) v.findViewById(R.id.ipTextDialog);
        EditText portField = (EditText) v.findViewById(R.id.portTextDialog);
        EditText feedField = (EditText) v.findViewById(R.id.feedTextDialog);
        ipServer = ipField.getText().toString();
        portServer = Integer.parseInt(portField.getText().toString());
        feedServer = feedField.getText().toString();

        SharedPreferences preferences = getActivity().getSharedPreferences("Settings", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString("ipServer", ipServer);
        editor.putInt("portServer", portServer);
        editor.putString("feedServer", feedServer);
        editor.commit();
    }

    private void resetSettings(){
        SharedPreferences preferences = getActivity().getSharedPreferences("Settings", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("ipServer", MapsActivity.STATIC_IP_SERVER);
        editor.putInt("portServer",MapsActivity.STATIC_PORT_SERVER);
        editor.putString("feedServer", MapsActivity.STATIC_FEED_SERVER);
        editor.commit();
    }

    public void initView(View v){
        IPAddressText ipField = (IPAddressText) v.findViewById(R.id.ipTextDialog);
        EditText portField = (EditText) v.findViewById(R.id.portTextDialog);
        EditText feedField = (EditText) v.findViewById(R.id.feedTextDialog);

        ipField.setText(ipServer);
        portField.setText(Integer.toString(portServer));
        feedField.setText(feedServer);
    }

    public interface SettingsDialogListener {
         void onSettingsDialogClick(DialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    SettingsDialogListener mListener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (SettingsDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }


}