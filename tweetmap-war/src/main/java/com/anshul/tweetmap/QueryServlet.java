package com.anshul.tweetmap;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import com.google.appengine.api.search.SearchException;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.IndexSpec;
import com.google.appengine.api.search.SearchServiceFactory;
import com.google.appengine.api.search.Query;
import com.google.appengine.api.search.QueryOptions;
import com.google.appengine.api.search.Cursor;

public class QueryServlet extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String callBackJavaScripMethodName = req.getParameter("callback");
		String queryString = req.getParameter("q");
		JSONObject json	= new JSONObject();
		JSONArray  tweets = new JSONArray();
		JSONObject tweet;
		
		IndexSpec indexSpec = IndexSpec.newBuilder().setName("TweetSearch").build(); 
		Index index = SearchServiceFactory.getSearchService().getIndex(indexSpec);
		HashMap<String, Integer> counter = new HashMap<String, Integer>();
		Map<String, Integer> counter2;
		try {
			Cursor cursor = Cursor.newBuilder().build();
			do {
				//String queryString = "product: piano AND price &lt; 5000";
				QueryOptions options = QueryOptions.newBuilder()
					     				.setLimit(1000)
					     				.setCursor(cursor)
					     				.build();
				Query query = Query.newBuilder()
								.setOptions(options)
								.build(queryString);
			    Results<ScoredDocument> results = index.search(query);
			    int numberRetrieved = results.getNumberReturned();
		        cursor = results.getCursor();
		        
		        if (numberRetrieved>0) {
		        	for (ScoredDocument document : results) {
				    	tweet = new JSONObject();
						JSONObject geometry = new JSONObject();
						geometry.put("type", "Point");
						JSONArray coordinates = new JSONArray();
						coordinates.add(document.getOnlyField("location").getGeoPoint().getLongitude());
						coordinates.add(document.getOnlyField("location").getGeoPoint().getLatitude());
						
						geometry.put("coordinates", coordinates);
						tweet.put("geometry", geometry);
						
						String hash = (String) document.getOnlyField("hashtags").getText();
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
						properties.put("text", document.getOnlyField("text").getText());
						properties.put("userImage", document.getOnlyField("userImage").getAtom());
						properties.put("user", document.getOnlyField("user").getAtom());
						properties.put("dateCreated", document.getOnlyField("dateCreated").getDate().toString());
						tweet.put("properties", properties);
						
						tweet.put("type", "Feature");

						tweets.add(tweet);
				    }
		        	//System.out.println(tweets);
		        	numberRetrieved=0;
		        }
			    // Iterate over the documents in the results
			    
			} while (cursor!=null);
		    
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
		    json.put("features", tweets);
			json.put("type", "FeatureCollection");
		} catch (SearchException e) {
//		    if (StatusCode.TRANSIENT_ERROR.equals(e.getOperationResult().getCode())) {
//		        // retry
//		    }
			e.printStackTrace();
		}
		String jsonPoutput = callBackJavaScripMethodName + "("+ json.toString() + ");";
		resp.setContentType("text/javascript");
		resp.getWriter().println(jsonPoutput);
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