package de.markhaehnel.rbtv.rocketbeanstv;

import android.content.Context;
import android.os.AsyncTask;
import android.renderscript.Double2;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class GetLatestVersionTask extends AsyncTask<Context, Void, JSONObject> {
    protected JSONObject doInBackground(Context... contexts) {

        try {
            String response = HttpRequest.get("https://api.github.com/repos/ezteq/rbtv-firetv/releases/latest").body();
            return new JSONObject(response);
        } catch (JSONException e) {
            e.printStackTrace();
            return new JSONObject();
        }
    }

    protected void onPostExecute(JSONObject json) {
        try {
            String latestVersionString = json.getString("name");
            boolean isDraft = json.getBoolean("draft");

            if (!isDraft) {
                double latestVersion = Double.parseDouble(latestVersionString);
                double currentVersion = Double.parseDouble(BuildConfig.VERSION_NAME);
                if (latestVersion > currentVersion) {
                    MainActivity.getInstance().showNewVersionAvailable();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
