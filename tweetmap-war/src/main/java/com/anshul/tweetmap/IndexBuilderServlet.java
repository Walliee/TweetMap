package com.anshul.tweetmap;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Field;
import com.google.appengine.api.search.GeoPoint;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.IndexSpec;
import com.google.appengine.api.search.SearchServiceFactory;
import com.google.appengine.api.search.PutException;
import com.google.appengine.api.search.StatusCode;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

public class IndexBuilderServlet extends HttpServlet {
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
		  	URL url = new URL("https://s3-us-west-2.amazonaws.com/com.tweetmap/test"+fileNumber+".json");
			URLConnection connection = url.openConnection();
			connection.setDoInput(true);
			InputStream inStream = connection.getInputStream();
			BufferedReader input = new BufferedReader(new InputStreamReader(  
			        inStream));  
			//FileReader input = new FileReader("/Users/Walliee/Documents/workspace/twitter/test"+i+".json");
			Object obj = parser.parse(input);
			JSONObject jsonObject =  (JSONObject) obj;
			JSONArray jsonArray = (JSONArray) jsonObject.get("tweets");			
			Iterator itr = jsonArray.iterator();
			List<Document> docList = new ArrayList<Document>(200);
			IndexSpec indexSpec = IndexSpec.newBuilder().setName("TweetSearch").build(); 
		    Index index = SearchServiceFactory.getSearchService().getIndex(indexSpec);
			
			while(itr.hasNext()) {
				Status status = null;
				try {
					status = (Status) TwitterObjectFactory.createStatus(itr.next().toString());
					System.out.println(status.getUser().getName());
					Entity greeting = new Entity("Tweet");
					GeoPoint geoPoint = new GeoPoint(status.getGeoLocation().getLatitude(), status.getGeoLocation().getLongitude());
					Document doc = Document.newBuilder()
						    .addField(Field.newBuilder().setName("text").setText(status.getText()))
						    .addField(Field.newBuilder().setName("user").setAtom(status.getUser().getName()))
						    .addField(Field.newBuilder().setName("userScreenName").setAtom("@".concat(status.getUser().getScreenName())))
						    .addField(Field.newBuilder().setName("userImage").setAtom(status.getUser().getMiniProfileImageURL()))
						    .addField(Field.newBuilder().setName("dateCreated").setDate(status.getCreatedAt()))
						    .addField(Field.newBuilder().setName("location").setGeoPoint(geoPoint))
						    .build();
						    
					docList.add(doc);
					if(docList.size()==200) {
						try {
							index.put(docList);
							docList.clear();
						}
						catch (PutException e) {
							e.printStackTrace();
						}
					}
				}
				catch (TwitterException e) {
					e.printStackTrace();
				}
			}
	  } catch (FileNotFoundException e) {
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
