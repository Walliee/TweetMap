package com.anshul.tweetmap;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.LinkedHashMap;

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
		List<Entity> greetings = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(20000).chunkSize(1000));
		HashMap<String, Integer> counter = new HashMap<String, Integer>();
		Map<String, Integer> counter2;
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
					String hash = (String) greeting.getProperty("hashtags");
					if(!hash.equals("")){
						String[] hashes = hash.split("\\s");
						for (String a : hashes) {
							if (counter.containsKey(a)) {
								int oldValue = counter.get(a);
								counter.put(a, oldValue + 1);
							} else {
								counter.put(a, 1);
							}
						}
					}
					
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
				//System.out.println(counter.toString());
				counter2 = sortByValues(counter);
				//System.out.println(counter2);
				JSONArray hashtags = new JSONArray();
				//List<String> keys = new ArrayList<String>(counter2.keySet());
				int cnt = 0;
				for(String key:counter2.keySet()){
					if(cnt == 0) {
						cnt++;
						continue;
					}
					if(cnt == 10)
						break;
					JSONObject hash = new JSONObject();
					hash.put("hashtag", key);
					hash.put("frequency", counter2.get(key));
					hashtags.add(hash);
					//System.out.println(key);
					cnt++;
				}
				json.put("hash", hashtags);
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
	
	public static <K extends Comparable,V extends Comparable> Map<K,V> sortByValues(Map<K,V> map){
        List<Map.Entry<K,V>> entries = new LinkedList<Map.Entry<K,V>>(map.entrySet());
      
        Collections.sort(entries, new Comparator<Map.Entry<K,V>>() {

            @Override
            public int compare(Entry<K, V> o1, Entry<K, V> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
      
        //LinkedHashMap will keep the keys in the order they are inserted
        //which is currently sorted on natural ordering
        Map<K,V> sortedMap = new LinkedHashMap<K,V>();
      
        for(Map.Entry<K,V> entry: entries){
            sortedMap.put(entry.getKey(), entry.getValue());
        }
      
        return sortedMap;
    }

}