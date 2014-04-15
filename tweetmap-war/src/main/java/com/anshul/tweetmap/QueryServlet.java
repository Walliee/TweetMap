package com.anshul.tweetmap;

import java.io.IOException;

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
//import com.google.appengine.api.search.Document;

public class QueryServlet extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String callBackJavaScripMethodName = req.getParameter("callback");
		String query = req.getParameter("q");
		JSONObject json	= new JSONObject();
		JSONArray  tweets = new JSONArray();
		JSONObject tweet;
		
		IndexSpec indexSpec = IndexSpec.newBuilder().setName("TweetSearch").build(); 
		Index index = SearchServiceFactory.getSearchService().getIndex(indexSpec);
		
		try {
		    //String queryString = "product: piano AND price &lt; 5000";
		    Results<ScoredDocument> results = index.search(query);

		    // Iterate over the documents in the results
		    for (ScoredDocument document : results) {
		    	tweet = new JSONObject();
				JSONObject geometry = new JSONObject();
				geometry.put("type", "Point");
				JSONArray coordinates = new JSONArray();
				coordinates.add(document.getOnlyField("location").getGeoPoint().getLongitude());
				coordinates.add(document.getOnlyField("location").getGeoPoint().getLatitude());
				
				geometry.put("coordinates", coordinates);
				tweet.put("geometry", geometry);
				
				JSONObject properties = new JSONObject();
				properties.put("text", document.getOnlyField("text").getText());
				properties.put("userImage", document.getOnlyField("userImage").getAtom());
				properties.put("user", document.getOnlyField("user").getAtom());
				//properties.put("dateCreated", document.getOnlyField("dateCreated").getDate());
				tweet.put("properties", properties);
				
				tweet.put("type", "Feature");

				tweets.add(tweet);
		    }
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
}