package com.example.twittersearch;

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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

/**
 * This Activity is used to search Twitter for tweets.
 * 
 * @author Shane Doyle
 */
public class MainActivity extends Activity {

	/**
	 * This class is used to get the Tweets.
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
					} else {
						// Heuston we have a problem..
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

			tweetsListView.setAdapter(new TweetsArrayAdapter(context, tweets));
		}
	}

	/**
	 * This class is used to store Tweet Information.
	 * 
	 * @author Shane Doyle
	 */
	private class Tweet {

		private String message;
		private String username;

		private Tweet(String username, String message) {
			setUsername(username);
			setMessage(message);
		}

		public String getMessage() {
			return message;
		}

		public String getUsername() {
			return username;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public void setUsername(String username) {
			this.username = username;
		}
	}

	/**
	 * This class is used to display the tweets in a list.
	 * 
	 * @author Shane Doyle
	 */
	private class TweetsArrayAdapter extends ArrayAdapter<Tweet> {

		private Context context;
		private List<Tweet> tweets;

		public TweetsArrayAdapter(Context context, List<Tweet> tweets) {
			super(context, R.layout.row_tweet, tweets);
			setContext(context);
			setTweets(tweets);
		}

		public Context getContext() {
			return context;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			Tweet tweet = tweets.get(position);

			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			View view = inflater.inflate(R.layout.row_tweet, parent, false);

			TextView usernameTextView = (TextView) view
					.findViewById(R.id.usernameTextView);
			TextView tweetTextView = (TextView) view
					.findViewById(R.id.tweetTextView);

			usernameTextView.setText(tweet.getUsername());
			tweetTextView.setText(tweet.getMessage());

			return view;
		}

		public void setContext(Context context) {
			this.context = context;
		}

		public void setTweets(List<Tweet> tweets) {
			this.tweets = tweets;
		}
	}

	private static final String TWITTER_URL = "http://search.twitter.com/search.json?q=";

	private Context context;

	private EditText searchEditText;

	private ListView tweetsListView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		context = this;
		Button searchButton = (Button) findViewById(R.id.searchButton);
		searchEditText = (EditText) findViewById(R.id.searchEditText);
		tweetsListView = (ListView) findViewById(R.id.tweetsListView);

		searchButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (searchEditText.getText().length() >= 0) {
					try {
						String searchString = searchEditText.getText()
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
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public void onResume() {
		super.onResume();
	}
}
