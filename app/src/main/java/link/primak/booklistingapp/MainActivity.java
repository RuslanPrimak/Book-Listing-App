package link.primak.booklistingapp;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Toast;

import java.util.List;

import link.primak.booklistingapp.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<VolumeInfo>> {

    private static final String TAG = "MainActivityTag";
    private static final int VOLUME_ASYNC_LOADER = 0;
    private static final String ARG_BUNDLE_SEARCH = "ARG_BUNDLE_SEARCH";
    private static final String ARG_BUNDLE_POSITION = "ARG_BUNDLE_POSITION";

    private ActivityMainBinding mBinding;
    private VolumesAdapter mAdapter;
    private int mListFirstVisiblePosition;

    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mListFirstVisiblePosition = savedInstanceState.getInt(ARG_BUNDLE_POSITION, 0);
        }

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mBinding.listView.setEmptyView(mBinding.textSearchResult);
        mAdapter = new VolumesAdapter(this);
        mBinding.listView.setAdapter(mAdapter);
        mBinding.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // Find the current earthquake that was clicked on
                VolumeInfo volume = mAdapter.getItem(position);

                if (volume != null) {
                    // Convert the String URL into a URI object (to pass into the Intent constructor)
                    // Create a new intent to view the earthquake URI
                    Intent websiteIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(volume.getLink()));

                    // Send the intent to launch a new activity
                    startActivity(websiteIntent);
                }
            }
        });

        hideKeyboard();
        if (checkConnectivity()) {
            //Log.d(TAG, "INIT VolumeLoader(null)");
            getSupportLoaderManager().initLoader(VOLUME_ASYNC_LOADER, null, this);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ARG_BUNDLE_POSITION, mBinding.listView.getFirstVisiblePosition());
    }

    public void onSearchButtonClick(View view) {
        mListFirstVisiblePosition = 0;
        hideKeyboard();
        //Log.d(TAG, "onSearchButtonClick");
        String searchPhrase = mBinding.editQuery.getText().toString();
        if (TextUtils.isEmpty(searchPhrase)) {
            Toast.makeText(this, "Search string could not be empty!", Toast.LENGTH_SHORT).show();
        } else {
            if (checkConnectivity()) {
                setProgressLoading(true);
                //Log.d(TAG, "RESTART VolumeLoader(" + searchPhrase + ")");
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
                //Log.d(TAG, "NEW VolumeLoader(" + phrase + ")");
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
            //Log.d(TAG, "onLoadFinished=" + data.size());
        } /*else {
            Log.d(TAG, "onLoadFinished=null");
        }*/
        mAdapter.notifyDataSetChanged();
        setProgressLoading(false);
        if (mListFirstVisiblePosition > 0)
            mBinding.listView.smoothScrollToPosition(mListFirstVisiblePosition);
    }

    @Override
    public void onLoaderReset(Loader<List<VolumeInfo>> loader) {
        //Log.d(TAG, "onLoaderReset");
        // Loader reset, so we can clear out our existing data.
        if (loader instanceof VolumesLoader) {
            mAdapter.clear();
            mAdapter.notifyDataSetChanged();
            setProgressLoading(false);
        }
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
