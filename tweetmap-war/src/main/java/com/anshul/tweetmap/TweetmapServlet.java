package com.anshul.tweetmap;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import twitter4j.GeoLocation;
import twitter4j.HashtagEntity;
import twitter4j.MediaEntity;
import twitter4j.Place;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;
import twitter4j.URLEntity;
import twitter4j.User;
import twitter4j.UserMentionEntity;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;

import java.util.List;

public class TweetmapServlet extends HttpServlet {
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
	  if (req.getParameter("file") != null) {
		  put(Integer.parseInt(req.getParameter("file")));
	  }
  }
  
  public void put(int fileNumber) {
	JSONParser parser = new JSONParser();
	
	try {
		//for(int i=1;i<=1;i++) {
			URL url = new URL("https://s3-us-west-2.amazonaws.com/com.tweetmap/test"+fileNumber+".json");
			URLConnection connection = url.openConnection();
			connection.setDoInput(true);
			//connection.setConnectTimeout(100000);
			InputStream inStream = connection.getInputStream();
			BufferedReader input = new BufferedReader(new InputStreamReader(  
			        inStream));  
			//FileReader input = new FileReader("/Users/Walliee/Documents/workspace/twitter/test"+fileNumber+".json");
			Object obj = parser.parse(input);
			JSONObject jsonObject =  (JSONObject) obj;
			JSONArray jsonArray = (JSONArray) jsonObject.get("tweets");			
			Iterator itr = jsonArray.iterator();
			
			while(itr.hasNext()) {
				Status status = null;
				try {
					status = (Status) TwitterObjectFactory.createStatus(itr.next().toString());
					//System.out.println(status.getUser().getName());
					Entity greeting = new Entity("Tweet");
				    greeting.setProperty("text", status.getText());
				    greeting.setProperty("user", status.getUser().getName());
				    greeting.setProperty("userScreenName", "@".concat(status.getUser().getScreenName()));
				    HashtagEntity[] hashtagEntities = status.getHashtagEntities();
	            	String hash = "";
	            	for (HashtagEntity entity:hashtagEntities) {
	            		hash = hash.concat(" ").concat(entity.getText());
	            	}
				    greeting.setProperty("hashtags", hash);
				    greeting.setProperty("lat", status.getGeoLocation().getLatitude());
				    greeting.setProperty("long", status.getGeoLocation().getLongitude());
				    greeting.setProperty("userImage", status.getUser().getMiniProfileImageURL());
				    greeting.setProperty("dateCreated", status.getCreatedAt());
				    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
				    datastore.put(greeting);
			
				}
				catch (TwitterException e) {
					e.printStackTrace();
				}
			}
		//}
	}
	catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	catch (ParseException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  }
}