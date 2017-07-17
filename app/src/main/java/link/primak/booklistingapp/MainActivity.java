package link.primak.booklistingapp;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

import link.primak.booklistingapp.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<VolumeInfo>> {

    //https://developers.google.com/books/docs/v1/getting_started#intro

    /** TODO: 13-Jul-17 For this project, you will be creating a book listing app.
     * A user should be able to enter a keyword, press the search button,
     * and recieve a list of published books which relate to that keyword.
     */

    /** TODO: 13-Jul-17 We suggest first exploring the API and learning what information it returns
     *  given a particular query. An example query that we found useful was:
     *  https://www.googleapis.com/books/v1/volumes?q=android&maxResults=1
     */

    // TODO: 13-Jul-17 Book Listing project rubric: https://review.udacity.com/#!/rubrics/164/view

    // TODO: 13-Jul-17 Text Wrapping: Information displayed on list items is not crowded.
    /** TODO: 13-Jul-17 Rotation: Upon device rotation - The layout remains scrollable.;
     *  The app should save state and restore the list back to the previously scrolled position.;
     *  The UI should adjust properly so that all contents of each list item is still visible and not truncated.;
     *  The Search button should still remain visible on the screen after the device is rotated.
     */

    /** TODO: 13-Jul-17 API Call: The user can enter a word or phrase to serve as a search query.
     *  The app fetches book data related to the query via an HTTP request from the Google Books API,
     *  using a class such as HttpUriRequest or HttpURLConnection.
     */

    /** TODO: 14-Jul-17
     *  Perform a search for quilting:
     *  GET https://www.googleapis.com/books/v1/volumes?q=quilting
     *  https://developers.google.com/books/docs/v1/reference/volumes/list
     *  https://developers.google.com/books/docs/v1/reference/volumes
     */

    /** TODO: 14-Jul-17
     *  Get information on volume s1gVAAAAYAAJ:
     *  GET https://www.googleapis.com/books/v1/volumes/s1gVAAAAYAAJ
     */

    /** TODO: 13-Jul-17
     * Bookshelf - create separate activity for managing Bookshelf
     * A bookshelf is a collection of volumes
     * Is it possible to request list of bookshelves?
     * Note: Creating and deleting bookshelves as well as modifying privacy settings on bookshelves
     * can currently only be done through the Google Books site.
     */

    /*
     TODO: 18-Jul-17 доработать поиск рускоязычных слов
     */

    /*
     TODO: 18-Jul-17 доработать загрузку картинок
     */


    private ActivityMainBinding mBinding;
    private VolumesAdapter mAdapter;
    private static final String TAG = "MainActivityTag";
    private static final int VOLUME_ASYNC_LOADER = 0;
    private static final String ARG_BUNDLE_SEARCH = "ARG_BUNDLE_SEARCH";

    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mBinding.listView.setEmptyView(mBinding.textSearchResult);
        mAdapter = new VolumesAdapter(this);
        mBinding.listView.setAdapter(mAdapter);
        mBinding.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // Find the current earthquake that was clicked on
                VolumeInfo volume = mAdapter.getItem(position);

                // Convert the String URL into a URI object (to pass into the Intent constructor)
                Uri uri = Uri.parse(volume.getLink());

                // Create a new intent to view the earthquake URI
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, uri);

                // Send the intent to launch a new activity
                startActivity(websiteIntent);
            }
        });

        hideKeyboard();
        if (checkConnectivity()) {
            Log.d(TAG, "initLoader(null)");
            getSupportLoaderManager().initLoader(VOLUME_ASYNC_LOADER, null, this);
        }
    }

    public void onSearchButtonClick(View view) {
        hideKeyboard();
        Log.d(TAG, "onSearchButtonClick");
        String searchPhrase = mBinding.editQuery.getText().toString();
        if (TextUtils.isEmpty(searchPhrase)) {
            Toast.makeText(this, "Search string could not be empty!", Toast.LENGTH_SHORT).show();
        } else {
            if (checkConnectivity()) {
                setProgressLoading(true);
                Log.d(TAG, "restartLoader(" + searchPhrase + ")");
                getSupportLoaderManager().restartLoader(VOLUME_ASYNC_LOADER,
                        getSearchArgs(searchPhrase), this);
            }
        }
    }

    @Override
    public Loader<List<VolumeInfo>> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case VOLUME_ASYNC_LOADER: {
                // Extract search phrase
                String phrase = getSearchPhrase(args);
                Log.d(TAG, "new VolumesLoader(" + phrase + ")");
                return new VolumesLoader(this, phrase);
            }
            default: return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<List<VolumeInfo>> loader, List<VolumeInfo> data) {
        mAdapter.clear();
        if (data != null) {
            mAdapter.addAll(data);
            Log.d(TAG, "onLoadFinished=" + data.size());
        } else {
            Log.d(TAG, "onLoadFinished=null");
        }
        mAdapter.notifyDataSetChanged();
        setProgressLoading(false);
    }

    @Override
    public void onLoaderReset(Loader<List<VolumeInfo>> loader) {
        Log.d(TAG, "onLoaderReset");
        // Loader reset, so we can clear out our existing data.
        mAdapter.clear();
        mAdapter.notifyDataSetChanged();
        setProgressLoading(false);
    }

    private String getSearchPhrase(Bundle args) {
        if (args != null) {
            return args.getString(ARG_BUNDLE_SEARCH, "");
        }
        return "";
    }

    private Bundle getSearchArgs(String searchPhrase) {
        Bundle args = new Bundle();
        args.putString(ARG_BUNDLE_SEARCH, searchPhrase);
        return args;
    }

    private void setProgressLoading(boolean isLoading) {
        if (isLoading) {
            mBinding.progressSearch.setVisibility(View.VISIBLE);
        } else {
            mBinding.progressSearch.setVisibility(View.INVISIBLE);
        }
    }

    private boolean checkConnectivity() {
        // Check internet connectivity
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if (!isConnected) {
            mBinding.textSearchResult.setText(getString(R.string.no_internet_connection));
            setProgressLoading(false);
        }
        return isConnected;
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        mBinding.focusGainer.requestFocus();
    }
}
