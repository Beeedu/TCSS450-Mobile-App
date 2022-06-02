package edu.uw.tcss450.blynch99.tcss450mobileapp.ui.contacts;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.uw.tcss450.blynch99.tcss450mobileapp.R;

public class SearchViewModel extends AndroidViewModel {
    private MutableLiveData<List<Contact>> mSearchList;

    public SearchViewModel(@NonNull Application application) {
        super(application);
        mSearchList = new MutableLiveData<>();
        mSearchList.setValue(new ArrayList<>());
    }

    public void addSearchListObserver(@NonNull LifecycleOwner owner,
                                       @Nullable Observer<?super List<Contact>> observer){
        mSearchList.observe(owner,observer);
    }

    public void connectSearch(String jwt, String searched) {
        String url =
                getApplication().getResources().getString(R.string.base_url_service)
                        + "search/" + searched;
        Request request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                this::handleResult,
                this::handleError) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                // add headers <key,value>
                // anything works for the jwt for now
                headers.put("Authorization", jwt);
                return headers;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(
                10_000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        //Instantiate the RequestQueue and add the request to the queue
        Volley.newRequestQueue(getApplication().getApplicationContext())
                .add(request);
    }

    protected void handleError(final VolleyError error) {
        throw new IllegalStateException(error.getMessage());
    }

    protected void handleResult(final JSONObject result) {
        try {
            JSONObject response = result;
            FriendStatus status = FriendStatus.NOT_FRIENDS;

            if (response.has("rows")) {
                JSONArray rows = response.getJSONArray("rows");
                for (int i = 0; i< rows.length(); i++){
                    JSONObject jsonContact = rows.getJSONObject(i);
                    Contact contact = new Contact(
                            jsonContact.getString("id"),
                            jsonContact.getString("username"),
                            jsonContact.getString("firstname"),
                            jsonContact.getString("lastname"),
                            jsonContact.getString("email"),
                            status
                    );

                    if(!mSearchList.getValue().contains(contact))
                        mSearchList.getValue().add(contact);
                }
            } else {
                Log.e("ERROR", "No Friends Provided");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR",e.getMessage());
        }
        mSearchList.setValue(mSearchList.getValue());
    }
}
