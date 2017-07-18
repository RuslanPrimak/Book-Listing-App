/*
 * Copyright (c) 2017. Ruslan Primak
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Last modified 7/14/17 9:27 PM
 */

package link.primak.booklistingapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static java.net.HttpURLConnection.HTTP_OK;

class GoogleBooksUtils {
    private static final String TAG = "GoogleBooksUtils";

    static String getQuery(Context context, String keywords) throws UnsupportedEncodingException {
        return context.getString(R.string.search_volumes_query, URLEncoder.encode(keywords, "UTF-8"));
    }

    static URL createUrl(String stringUrl) {
        try {
            return new URL(stringUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    static <T> T processHttpRequest(URL url, @NonNull InputStreamProcessor<T> streamProcessor) throws IOException {
        T result = null;
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;

        if (url != null) {
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setReadTimeout(10000 /* milliseconds */);
                urlConnection.setConnectTimeout(15000 /* milliseconds */);
                urlConnection.connect();
                final int response = urlConnection.getResponseCode();
                if (response == HTTP_OK) {
                    inputStream = urlConnection.getInputStream();
                    result = streamProcessor.processStream(inputStream);
                } else {
                    Log.e(TAG, "Connection response error: " + response);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }

        return result;
    }

    interface InputStreamProcessor<T> {
        T processStream(InputStream stream) throws IOException;
    }

    static InputStreamProcessor<String> getInputStreamStringProcessor() {
        return new InputStreamProcessor<String>() {
            @Override
            public String processStream(InputStream stream) throws IOException {
                StringBuilder output = new StringBuilder();
                if (stream != null) {
                    InputStreamReader inputStreamReader = new InputStreamReader(stream, Charset.forName("UTF-8"));
                    BufferedReader reader = new BufferedReader(inputStreamReader);
                    String line = reader.readLine();
                    while (line != null) {
                        output.append(line);
                        line = reader.readLine();
                    }
                }
                return output.toString();
            }
        };
    }

    static InputStreamProcessor<Bitmap> getInputStreamBitmapProcessor() {
        return new InputStreamProcessor<Bitmap>() {
            @Override
            public Bitmap processStream(InputStream stream) throws IOException {
                Bitmap bmp = BitmapFactory.decodeStream(stream);
                return bmp;
            }
        };
    }

    static InputStreamProcessor<List<VolumeInfo>> getInputStreamVolumeListProcessor() {
        return new InputStreamProcessor<List<VolumeInfo>>() {
            @Override
            public List<VolumeInfo> processStream(InputStream stream) throws IOException {
                StringBuilder output = new StringBuilder();
                if (stream != null) {
                    InputStreamReader inputStreamReader = new InputStreamReader(stream, Charset.forName("UTF-8"));
                    BufferedReader reader = new BufferedReader(inputStreamReader);
                    String line = reader.readLine();
                    while (line != null) {
                        output.append(line);
                        line = reader.readLine();
                    }
                }

                // Load thumbnails
                List<VolumeInfo> volumes = parseJsonVolumes(output.toString());
                for (VolumeInfo volume : volumes) {
                    volume.setImage(processHttpRequest(
                            createUrl(volume.getSmallThumbnail()),
                            getInputStreamBitmapProcessor()));
                }

                return volumes;
            }
        };
    }

    static List<VolumeInfo> parseJsonVolumes(String jsonString) {
        List<VolumeInfo> list = new ArrayList<>();
        VolumeInfo.Builder builder = new VolumeInfo.Builder();
        if (!TextUtils.isEmpty(jsonString)) {
            try {
                JSONObject root = new JSONObject(jsonString);
                JSONArray items = root.getJSONArray("items");
                for (int i = 0; i < items.length(); i++) {
                    JSONObject item = items.getJSONObject(i);
                    JSONObject volumeInfo = item.getJSONObject("volumeInfo");

                    String link = volumeInfo.getString("canonicalVolumeLink");

                    // Authors could not exists, show publisher instead
                    StringBuilder authorBuider = new StringBuilder();
                    if (volumeInfo.has("authors")) {
                        JSONArray authors = volumeInfo.getJSONArray("authors");
                        if (authors.length() > 0) {
                            authorBuider.append(authors.getString(0));
                        }

                        for (int j = 1; j < authors.length(); j++) {
                            authorBuider.append(", ").append(authors.getString(j));
                        }
                    } else if (volumeInfo.has("publisher")) {
                        authorBuider.append(volumeInfo.getString("publisher"));
                    }

                    JSONObject imageLinks = volumeInfo.getJSONObject("imageLinks");

                    list.add(builder.reset()
                            .setId(item.getString("id"))
                            .setAuthor(authorBuider.toString())
                            .setTitle(volumeInfo.getString("title"))
                            .setLink(volumeInfo.getString("canonicalVolumeLink"))
                            .setSmallThumbnail(imageLinks.getString("smallThumbnail"))
                            .build()
                    );
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return list;
    }
}
