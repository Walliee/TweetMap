package com.anshul.tweetmap;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import twitter4j.JSONException;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;

import java.util.List;

public class RunQuery extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
//	  if (req.getParameter("file") != null) {
//		  put(Integer.parseInt(req.getParameter("file")));
//	  }
		String callBackJavaScripMethodName = req.getParameter("callback");
		JSONObject json	= new JSONObject();
		JSONArray  tweets = new JSONArray();
		JSONObject tweet;
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Query query = new Query("Tweet");
		List<Entity> greetings = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(10000).chunkSize(1000));
		if (!greetings.isEmpty()) {
			try {
				for (Entity greeting : greetings) {
					tweet = new JSONObject();
					JSONObject geometry = new JSONObject();
					geometry.put("type", "Point");
					JSONArray coordinates = new JSONArray();
					coordinates.add(greeting.getProperty("long"));
					coordinates.add(greeting.getProperty("lat"));
					
					geometry.put("coordinates", coordinates);
					tweet.put("geometry", geometry);
					
					JSONObject properties = new JSONObject();
					properties.put("text", greeting.getProperty("text"));
					properties.put("userImage", greeting.getProperty("userImage"));
					properties.put("user", greeting.getProperty("user"));
					properties.put("dateCreated", greeting.getProperty("dateCreated").toString());
					tweet.put("properties", properties);
					
					tweet.put("type", "Feature");

					tweets.add(tweet);
				}
				json.put("features", tweets);
				json.put("type", "FeatureCollection");
				
			}
			catch (Exception jse) {
				jse.printStackTrace();
			}
//			resp.setContentType("application/json");
//			resp.getWriter().write(json.toString());
			String jsonPoutput = callBackJavaScripMethodName + "("+ json.toString() + ");";
			resp.setContentType("text/javascript");
			resp.getWriter().println(jsonPoutput);
		}
	}
}