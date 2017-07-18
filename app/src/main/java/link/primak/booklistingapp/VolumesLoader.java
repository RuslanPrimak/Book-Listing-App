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
 * Last modified 7/15/17 1:39 AM
 */

package link.primak.booklistingapp;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.text.TextUtils;

import java.io.IOException;
import java.util.List;

class VolumesLoader extends AsyncTaskLoader<List<VolumeInfo>> {
    private static final String TAG = "VolumesLoader";
    private String mSearchPhrase;
    private List<VolumeInfo> mData;
    private Context mContext;

    VolumesLoader(Context context, String searchPhrase) {
        super(context);
        mSearchPhrase = searchPhrase;
        mContext = context;
        //Log.d(TAG, "VolumesLoader(" + searchPhrase + ")");
    }

    @Override
    protected void onStartLoading() {
        if (mData != null) {
            //Log.d(TAG, "onStartLoading(mData)");
            deliverResult(mData);
        }

        //if (takeContentChanged() || mData == null) {
            forceLoad();
        //}
    }

    @Override
    public List<VolumeInfo> loadInBackground() {
        if (TextUtils.isEmpty(mSearchPhrase)) {
            //Log.d(TAG, "loadInBackground(null 1)");
            return null;
        }

        try {
            //Log.d(TAG, "loadInBackground(" + mSearchPhrase + ")");
            String query = GoogleBooksUtils.getQuery(mContext, mSearchPhrase);
            return GoogleBooksUtils.processHttpRequest(
                    GoogleBooksUtils.createUrl(query),
                    GoogleBooksUtils.getInputStreamVolumeListProcessor());
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Log.d(TAG, "loadInBackground(null 2)");
        return null;
    }

    @Override
    public void deliverResult(List<VolumeInfo> data) {
        //Log.d(TAG, "deliverResult(" + (data == null ? "null" : data.size()) + ")");
        mData = data;
        super.deliverResult(data);
    }
}
