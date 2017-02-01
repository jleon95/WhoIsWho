package com.npi.whoiswho.pandora;

import android.os.StrictMode;
import android.util.Log;

import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class PandoraConnection {

    private static final String LOGTAG = "PANDORA_CONNECT";

    private String host;
    private String userKey;
    private String appId;
    private String botName;

    public PandoraConnection(String host, String appId, String userKey, String botName) {
        this.host = host;
        this.appId = appId;
        this.userKey = userKey;
        this.botName = botName;
    }

    public String talk(String input) throws PandoraException {

        String responses = "";
        input = input.replace(" ", "%20");

        URI uri = null;
        try {
            uri = new URI("https://"+host+"/talk/"+appId+"/"+botName+"?input="+input+"&user_key="+userKey);
            Log.d(LOGTAG, "Request to pandorabot: Botname=" + botName + ", input=\"" + input + "\"" + " uri="+ uri);
        } catch (URISyntaxException e) {
            Log.e(LOGTAG, e.getMessage());
            throw new PandoraException(PandoraErrorCode.IDORHOST);
        }


        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy); //Why the strictmode: http://stackoverflow.com/questions/25093546/android-os-networkonmainthreadexception-at-android-os-strictmodeandroidblockgua

            try {
                Content content = Request.Post(uri).execute().returnContent();
                String response = content.asString();
                JSONObject jObj = new JSONObject(response);
                JSONArray jArray = jObj.getJSONArray("responses");
                for (int i = 0; i < jArray.length(); i++) {
                    responses += jArray.getString(i).trim();
                }
            } catch (JSONException e) {
                Log.e(LOGTAG, e.getMessage());
                throw new PandoraException(PandoraErrorCode.PARSE);
            } catch (IOException e) {
                Log.e(LOGTAG, e.getMessage());
                throw new PandoraException(PandoraErrorCode.CONNECTION);
            } catch (Exception e){
                throw new PandoraException(PandoraErrorCode.IDORHOST);
            }

        }

        if(responses.toLowerCase().contains("match failed")) {
            Log.e(LOGTAG, "Match failed");
            throw new PandoraException(PandoraErrorCode.NOMATCH);
        }

        Log.d(LOGTAG, "Bot response:" + responses);

        return responses;
    }
}
