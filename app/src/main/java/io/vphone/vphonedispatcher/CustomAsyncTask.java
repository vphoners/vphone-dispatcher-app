package io.vphone.vphonedispatcher;

import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by FerasWilson on 2016-03-20.
 */
public class CustomAsyncTask extends AsyncTask<String, Void, JSONObject> {
    private JSONObject jsonResponse;
    private Map<String, String> headers;
    private RequestMethod requestMethod;
    private CustomAsyncTaskExecution<JSONObject> execution;

    /**
     * Body to be sent with the request
     */
    private JSONObject body;

    /**
     * Initialize custom async task
     *
     * @param requestMethod the request method. GET, POST, PUT, DELETE.
     * @param headers       custom headers.
     */
    public CustomAsyncTask(RequestMethod requestMethod, Map<String, String> headers) {
        this(requestMethod, headers, null);
    }

    /**
     * Initialize custom async task
     *
     * @param requestMethod          the request method. GET, POST, PUT, DELETE.
     * @param headers                custom headers.
     * @param customPrePostExecution custom execution of the pre- and post execute methods.
     */
    public CustomAsyncTask(RequestMethod requestMethod, Map<String, String> headers, JSONObject body, CustomAsyncTaskExecution<JSONObject> customPrePostExecution) {
        super();
        this.requestMethod = requestMethod;
        this.headers = headers;
        this.execution = customPrePostExecution;
        this.body = body;
    }

    /**
     * Initialize custom async task
     *
     * @param requestMethod          the request method. GET, POST, PUT, DELETE.
     * @param headers                custom headers.
     * @param customPrePostExecution custom execution of the pre- and post execute methods.
     */
    public CustomAsyncTask(RequestMethod requestMethod, Map<String, String> headers, CustomAsyncTaskExecution<JSONObject> customPrePostExecution) {
        this(requestMethod, headers, null, customPrePostExecution);
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        Log.v("INSIDE DOINBACKGRROUND", "HttpURLConnection");
        try {
            URL url = new URL(params[0]);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            Log.v("HttpURLConnection", "HttpURLConnection");
            conn.setRequestMethod(this.requestMethod.name());
            conn.setRequestProperty("Content-Type", "application/json");

            // Set the required headers
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    conn.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }

            // Allow to send a body to the api
            conn.setDoOutput(true);

            // Allow the application to receive a response
            conn.setDoInput(true);

            // Disable caching
            conn.setUseCaches(false);

            // Do not allow server to request more data
            conn.setAllowUserInteraction(false);

            if (this.requestMethod != RequestMethod.GET || this.requestMethod != RequestMethod.DELETE) {
                if (this.body != null) {
                    String str = this.body.toString();
                 /*   byte[] outputInBytes = str.getBytes("UTF-8");
                    OutputStream os = conn.getOutputStream();
                    os.write(outputInBytes);
                    os.close();*/

                    Writer writer = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(), "UTF-8"));
                    writer.write(str);
// json data
                    writer.close();

                    Log.v("Body", this.body.toString());
                }
            }


            Log.v("Response code", "Before get Response code");

            int code = conn.getResponseCode();

            Log.v("INSIDE ASYNC TASK", "Before Code check " + code);
            if (code == HttpURLConnection.HTTP_OK) {
                InputStream in = new BufferedInputStream(conn.getInputStream());
                String response = new Scanner(in, "UTF-8").useDelimiter("\\A").next();

                // Create a JSON object from the response
                JSONObject resultJson = new JSONObject(response);

                this.jsonResponse = resultJson;
            }

            return jsonResponse;

        } catch (ProtocolException e) {
            // e.printStackTrace();
            Log.e("INSIDE ASYNC TASK", "ERROR ProtocolException " + e.getLocalizedMessage() + " " + e.getMessage());
        } catch (MalformedURLException e) {
            // e.printStackTrace();
            Log.e("INSIDE ASYNC TASK", "ERROR MalformedURLException");
        } catch (IOException e) {
            //    e.printStackTrace();
            Log.e("INSIDE ASYNC TASK", "ERROR IOException");
        } catch (JSONException e) {
            // e.printStackTrace();
            Log.e("INSIDE ASYNC TASK", "ERROR JSONException");
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        if (execution != null) {
            this.execution.preExecution();
        }
    }

    @Override
    protected void onPostExecute(JSONObject jsonArray) {
        super.onPostExecute(jsonArray);

        if (execution != null && jsonArray != null) {
            this.execution.postExecution(jsonArray);
        }
    }
}
