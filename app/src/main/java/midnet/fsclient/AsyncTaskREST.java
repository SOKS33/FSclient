package midnet.fsclient;

/**
 * Created by tai on 08/09/15.
 */

import android.os.AsyncTask;
import android.util.Log;


import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

public class AsyncTaskREST extends AsyncTask<String, Integer, JSONObject> {
    private static final String TAG = "AsyncTaskREST";

    @Override
    protected JSONObject doInBackground(String... params) {
        // TODO Auto-generated method stub
        postData(params[0], params[1]);
        return null;
    }

    protected void onPostExecute(JSONObject result) {
        super.onPostExecute(result);
    }

    protected void onProgressUpdate(Integer... progress) {
    }

    public JSONObject postData(String url, String jsonString) {
        // Create a new HttpClient and Post Header
        JSONObject jsonObj = null;
        try {
            jsonObj = new JSONObject(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            DefaultHttpClient httpclient = new DefaultHttpClient();
            HttpPost httpPostRequest = new HttpPost(url);
            StringEntity se;
            se = new StringEntity(jsonObj.toString());

            // Set HTTP parameters
            httpPostRequest.setEntity(se);
            httpPostRequest.setHeader("Accept", "application/json");
            httpPostRequest.setHeader("Content-type", "application/json");
            httpPostRequest.setHeader("Accept-Encoding", "gzip"); // only set this parameter if you would like to use gzip compression

            long t = System.currentTimeMillis();
            HttpResponse response = httpclient.execute(httpPostRequest);
            Log.i(TAG, "HTTPResponse received in [" + (System.currentTimeMillis() - t) + "ms]");

            // Get hold of the response entity (-> the data):

            HttpEntity entity = response.getEntity();

            if (entity != null) {
                // Read the content stream
                InputStream instream = entity.getContent();
                Header contentEncoding = response.getFirstHeader("Content-Encoding");
                if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
                    instream = new GZIPInputStream(instream);
                }

                // convert content stream to a String
                String resultString = convertStreamToString(instream);
                instream.close();
                resultString = resultString.substring(1, resultString.length() - 1); // remove wrapping "[" and "]"

                // Transform the String into a JSONObject
                JSONObject jsonObjRecv = new JSONObject(resultString);
                // Raw DEBUG output of our received JSON object:
                Log.i(TAG, "<JSONObject>\n" + jsonObjRecv.toString() + "\n</JSONObject>");

                return jsonObjRecv;
            }

        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
        } catch (IOException e) {
            // TODO Auto-generated catch block
        } catch (JSONException e) {
            // TODO Auto-generated catch block
        }
        return null;
    }

    private static String convertStreamToString(InputStream is) {
        /*
		 * To convert the InputStream to String we use the BufferedReader.readLine()
		 * method. We iterate until the BufferedReader return null which means
		 * there's no more data to read. Each line will appended to a StringBuilder
		 * and returned as String.
		 *
		 * (c) public domain: http://senior.ceng.metu.edu.tr/2009/praeda/2009/01/11/a-simple-restful-client-at-android/
		 */
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}
