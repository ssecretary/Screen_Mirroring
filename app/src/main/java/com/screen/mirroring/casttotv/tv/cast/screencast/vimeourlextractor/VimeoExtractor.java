package com.screen.mirroring.casttotv.tv.cast.screencast.vimeourlextractor;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

public abstract class VimeoExtractor extends AsyncTask<Void, Integer, ArrayList<VimeoFile>> {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.115 Safari/537.36";
    private final String DATA_CONFIG_URL_PATTERN_1 = "data-config-url=\"(.+?)\"";
    private final String DATA_CONFIG_URL_PATTERN_2 = "\"config_url\":\"(.+?)\"";
    Context mContext;
    String url;


    protected abstract void onExtractionComplete(ArrayList<VimeoFile> vimeoFileArrayList);
    protected abstract void onExtractionFail(String Error);



    public void Extractor(Context mContext, String url) {
        this.mContext = mContext;
        this.url = url;
        this.execute();
    }


    private String parseHtml(String url)
    {
        try {
            URL getUrl = new URL(url);
            HttpURLConnection urlConnection = (HttpURLConnection) getUrl.openConnection();
            BufferedReader reader = null;
            urlConnection.setRequestProperty("User-Agent", USER_AGENT);
            StringBuilder streamMap= new StringBuilder();
            try {
                reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String line;
                while ((line= reader.readLine()) != null) {
                    streamMap.append(line);
                }
            } catch (Exception E) {
                E.printStackTrace();
                if (reader != null)
                    reader.close();
                urlConnection.disconnect();
                onCancelled();
            } finally {
                if (reader != null)
                    reader.close();
                urlConnection.disconnect();
            }

            Pattern data_config_url_pattern = Pattern.compile(DATA_CONFIG_URL_PATTERN_1);
            Matcher data_config_url_pattern_matcher = data_config_url_pattern.matcher(streamMap.toString());
            if (data_config_url_pattern_matcher.find()) {
                return streamMap.substring(data_config_url_pattern_matcher.start(), data_config_url_pattern_matcher.end()).replace("data-config-url=\"", "").replace("\"", "").replace("&amp;", "&").replace("\\","");
            } else {

                Pattern data_config_url_pattern2 = Pattern.compile(DATA_CONFIG_URL_PATTERN_2);
                Matcher data_config_url_pattern_matcher2 = data_config_url_pattern2.matcher(streamMap.toString());
                if(data_config_url_pattern_matcher2.find()) {
                    return streamMap.substring(data_config_url_pattern_matcher2.start(), data_config_url_pattern_matcher2.end()).replace("\"config_url\":\"", "").replace("\"", "").replace("&amp;", "&").replace("\\","");
                }
            }

            return null;
        }
        catch (Exception E)
        {
            E.printStackTrace();
            return null;
        }
    }

    @Override
    protected ArrayList<VimeoFile> doInBackground(Void... strings) {
        String data_config_url = parseHtml(url);
        String filename;
        String author;
        Integer duration;
        ArrayList<VimeoFile> vimeoVideoArrayList=new ArrayList<>();

        if (data_config_url != null) {
            try {
                HttpsURLConnection data_config_url_connection = (HttpsURLConnection) new URL(data_config_url).openConnection();
                try {
                    data_config_url_connection.connect();
                } catch (IllegalStateException E) {
                    E.printStackTrace();
                }

                InputStream in = data_config_url_connection.getInputStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder JsonString = new StringBuilder();
                String line = "";
                while ((line = reader.readLine()) != null) {
                    JsonString.append(line);
                }

                JSONObject jsonObject = new JSONObject(JsonString.toString());
                JSONObject jsonObjectRequest = jsonObject.getJSONObject("request");
                JSONObject jsonObjectVideo = jsonObject.getJSONObject("video");
                filename = jsonObjectVideo.getString("title");
                duration = jsonObjectVideo.getInt("duration");
                author = jsonObjectVideo.getJSONObject("owner").getString("name");
                JSONObject jsonObject2 = jsonObjectRequest.getJSONObject("files");
                JSONArray jsonArray = jsonObject2.getJSONArray("progressive");
                for (int i = 0; i < jsonArray.length(); i++) {
                    //Log.e("Progressive","["+i+"] "+jsonArray.getString(i));
                    String[] arr = jsonArray.getString(i).toString().replace("{", "").replace("}", "").split(",");
                    String quality = null;
                    String url = null;
                    String ext = null;
                    for (int j = 0; j < arr.length; j++) {
                        arr[j] = arr[j].toString().replace("\"", "");
                        String key = arr[j].substring(0, arr[j].indexOf(":"));
                        String value = arr[j].substring(arr[j].indexOf(":") + 1, arr[j].length());
                        if (key.equals("url")) {
                            url = value.replace("\\", "");
                        }
                        if (key.equals("quality")) {
                            quality = value.replace("\\", "").replace(":", "");
                        }
                        if (key.equals("mime")) {
                            ext = value.replace("\\", "").replace("video/", "").replace(":", "");
                        }
                    }
                    if (quality != null && url != null && ext != null) {
                        String size="";
                        try {
                            HttpsURLConnection c = (HttpsURLConnection) new URL(url).openConnection();
                            c.setRequestMethod("GET");
                            try {
                                c.connect();
                            } catch (Exception E) {
E.printStackTrace();
                            }

                            InputStream X = c.getInputStream();
                            long x = c.getContentLength();
                            long fileSizeInKB = x / 1024;
                            long fileSizeInMB = fileSizeInKB / 1024;
                            //Log.e("File Size", "Getted File Size : " + fileSizeInMB);
                           size = (fileSizeInMB > 1) ? fileSizeInMB + " MB" : fileSizeInKB + " KB";
                           X.close();
                        }catch (Exception E)
                        {
E.printStackTrace();
                        }


                        VimeoFile v = new VimeoFile();
                        v.setQuality(quality);
                        v.setUrl(url);
                        v.setExt(ext);
                        v.setFilename(filename);
                        v.setAuthor(author);
                        v.setSize(size);
                        v.setDuration(duration);
                        vimeoVideoArrayList.add(v);
                    }

                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else {
            Log.e("TAG","Not Found");
        }
        return vimeoVideoArrayList;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(ArrayList<VimeoFile> vimeoFileArrayList) {
        super.onPostExecute(vimeoFileArrayList);
        onExtractionComplete(vimeoFileArrayList);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        onExtractionFail("Somthing Wrong......!!");
    }
}
