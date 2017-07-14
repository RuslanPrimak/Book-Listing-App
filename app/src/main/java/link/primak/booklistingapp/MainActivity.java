package link.primak.booklistingapp;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.IOException;

import link.primak.booklistingapp.databinding.ActivityMainBinding;

import static android.R.attr.button;
import static android.provider.ContactsContract.CommonDataKinds.Website.URL;
import static link.primak.booklistingapp.GoogleBooksUtils.getInputStreamStringProcessor;

public class MainActivity extends AppCompatActivity {

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

    /** TODO: 13-Jul-17 Once you've explored the API, begin work in Android Studio.
     * You'll want a simple layout initially, with an editable TextView and a 'search' button.
     * Then, you'll want to build the AsyncTask that queries the API.
     * This is a complex step, so be sure to reference the course materials when needed.
     * Once you've queried the API, you'll need to parse the result.
     * This will involve storing the information returned by the API in a custom class.
     * Finally, you'll use the List and Adapter pattern to populate a list on the user's screen
     * with the information stored in the custom objects you wrote earlier.
     */

    // TODO: 13-Jul-17 Overall Layout: App contains a ListView which becomes populated with list items.

    // List Item Layout: List Items display at least author and title information.

    /** TODO: 13-Jul-17 Layout Best Practices: The code adheres to all of the following best practices:
     *  Text sizes are defined in sp; Lengths are defined in dp; Padding and margin is used appropriately,
     *  such that the views are not crammed up against each other.
     */

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

    /** TODO: 13-Jul-17 Response Validation: The app checks whether the device is connected to the
     *  internet and responds appropriately. The result of the request is validated to account for
     *  a bad server response or lack of server response.
     */

    // TODO: 13-Jul-17 Async Task: The network call occurs off the UI thread using an AsyncTask or similar threading object.

    /** TODO: 13-Jul-17 No Data Message: When there is no data to display,
     *  the app shows a default TextView that informs the user how to populate the list.
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


    private ActivityMainBinding mBinding;
    private static final String TAG = "MainActivity";

    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
    }

    public void onSearchButtonClick(View view) {
        if (TextUtils.isEmpty(mBinding.editQuery.getText())) {
            Toast.makeText(this, "Search string could not be empty!", Toast.LENGTH_SHORT).show();
        } else {
            hideKeyboard();
            if (checkConnectivity()) {
                String query = GoogleBooksUtils.getQuery(this, mBinding.editQuery.getText().toString());
                setProgressLoading(true);
                new VolumesAsyncTask().execute(query);
            }
        }
    }

    private class VolumesAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... query) {
            if ((query != null) && (query.length > 0)) {
                try {
                    return GoogleBooksUtils.processHttpRequest(GoogleBooksUtils.createUrl(query[0]),
                            GoogleBooksUtils.getInputStreamStringProcessor());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            setProgressLoading(false);
            mBinding.textSearchResult.setText(s);
        }
    }

    private void setProgressLoading(boolean isLoading) {
        if (isLoading) {
            mBinding.progressSearch.show();
            mBinding.textSearchResult.setVisibility(View.GONE);
        } else {
            mBinding.progressSearch.hide();
            mBinding.textSearchResult.setVisibility(View.VISIBLE);
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
    }
}
