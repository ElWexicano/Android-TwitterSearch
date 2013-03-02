package ie.iamshanedoyle.twittersearch;

import ie.iamshanedoyle.twittersearch.models.Tweet;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.example.twittersearch.R;

/**
 * This Activity is used to search Twitter for tweets.
 * 
 * @author Shane Doyle
 */
public class MainActivity extends Activity {

	private static final String TWITTER_URL = "http://search.twitter.com/search.json?q=";
	private Context mContext;
	private EditText mSearchEditText;
	private ListView mTweetsListView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		setmContext(this);
		Button searchButton = (Button) findViewById(R.id.button_search);
		setmSearchEditText((EditText) findViewById(R.id.edit_search));
		setmTweetsListView((ListView) findViewById(R.id.list_view_tweets));
		
		searchButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (getmSearchEditText().getText().length() >= 0) {
					try {
						String searchString = getmSearchEditText().getText()
								.toString();
						String twitterSearchString = TWITTER_URL
								+ URLEncoder.encode(searchString, "UTF-8");

						new GetTweetsTask().execute(twitterSearchString);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					// Pop up notification!
				}
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	public Context getmContext() {
		return mContext;
	}

	public void setmContext(Context mContext) {
		this.mContext = mContext;
	}

	public EditText getmSearchEditText() {
		return mSearchEditText;
	}

	public void setmSearchEditText(EditText mSearchEditText) {
		this.mSearchEditText = mSearchEditText;
	}

	public ListView getmTweetsListView() {
		return mTweetsListView;
	}

	public void setmTweetsListView(ListView mTweetsListView) {
		this.mTweetsListView = mTweetsListView;
	}

	/**
	 * This AsyncTask class is used to get the Tweets.
	 * 
	 * @author Shane Doyle
	 */
	private class GetTweetsTask extends AsyncTask<String, Void, String> {

		static final int OK_RESPONSE_CODE = 200;

		@Override
		protected String doInBackground(String... twitterURL) {
			StringBuilder tweetFeedBuilder = new StringBuilder();

			for (String searchURL : twitterURL) {
				HttpClient tweetClient = new DefaultHttpClient();
				HttpGet tweetGet = new HttpGet(searchURL);

				try {
					HttpResponse tweetResponse = tweetClient.execute(tweetGet);
					StatusLine searchStatus = tweetResponse.getStatusLine();

					if (searchStatus.getStatusCode() == OK_RESPONSE_CODE) {
						HttpEntity tweetEntity = tweetResponse.getEntity();
						InputStream tweetContent = tweetEntity.getContent();

						InputStreamReader tweetInput = new InputStreamReader(
								tweetContent);
						BufferedReader tweetReader = new BufferedReader(
								tweetInput);

						String lineIn;
						while ((lineIn = tweetReader.readLine()) != null) {
							tweetFeedBuilder.append(lineIn);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}

			return tweetFeedBuilder.toString();
		}

		@Override
		protected void onPostExecute(String result) {
			List<Tweet> tweets = new ArrayList<Tweet>();

			try {
				JSONObject resultObject = new JSONObject(result);
				JSONArray tweetArray = resultObject.getJSONArray("results");

				for (int i = 0; i < tweetArray.length(); i++) {
					JSONObject tweetObject = (JSONObject) tweetArray.get(i);
					tweets.add(new Tweet(tweetObject.getString("from_user"),
							tweetObject.getString("text")));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			getmTweetsListView().setAdapter(new TweetsArrayAdapter(mContext, tweets));
		}
	}

}
