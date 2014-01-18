package brown.quarter.PhotoVoter;


import java.net.UnknownHostException;
import java.util.Set;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import brown.quarter.photovoter.R;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoException;
import com.mongodb.ServerAddress;
import com.moodstocks.android.MoodstocksError;
import com.moodstocks.android.Scanner;

public class MainActivity extends Activity implements Scanner.SyncListener{
	private static String key = "";
    private static String secret = "";
    private static DBCollection events = null;
    private static int vid = 0;
    
    private boolean compatible = false;
    private Scanner scanner;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Thread thread = new Thread(new Runnable(){
		    @Override
		    public void run() {
		        try{
		    	    MongoClient mongoClient = null;
		    	    
		    		try{
		    			String textUri = "mongodb://test:test@ds027419.mongolab.com:27419/photovoter";
		    			MongoClientURI uri = new MongoClientURI(textUri);
		    			mongoClient = new MongoClient(uri);
		    			
		    		}catch(MongoException e){
		    			System.out.println("First Error");
		    			//e.printStackTrace();
		    			Log.d("mainactivity", "Server not found");
		    			System.out.println("Server not found");
		    			System.exit(0);
		    		} catch (UnknownHostException e) {
		    			e.printStackTrace();
		    		}
		    		
		    		System.out.println("Blah");
		    		DB db = mongoClient.getDB("photovoter"); 
		        	events = db.getCollection("events");
		        	
		        	Set<String> colls = db.getCollectionNames();

		        	for (String s : colls) {
		        	    System.out.println(s);
		        	}
		    	
		        } catch (Exception e) {
		            e.printStackTrace();
		        }
		    }
		});

		thread.start(); 
		try {
			thread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		setContentView(R.layout.activity_main);
	}
	
	
	public void startSwipe(){
		compatible = Scanner.isCompatible();
		
        if (compatible) {
          try {
            scanner = Scanner.get();
            scanner.open(this, key, secret);
            scanner.sync(this);
          } catch (MoodstocksError e) {
            e.log();
          }
        }
    	startActivity(new Intent(this, Swipe.class));
    }
	
	public void onGoClicked(View view) {
    	final EditText input = new EditText(this);
    	new AlertDialog.Builder(this)
		  .setTitle("Welcome")
		  .setMessage("Please Enter Your Event Key")
		  .setView(input)
		  .setNeutralButton("GO", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) { 
		        	Editable value = input.getText();//USE THIS AS KEY
		        
		        	DBObject event = events.findOne(new BasicDBObject("eid", value.toString()));
		        	
		        	if(event != null){
		        	
		        		key = (String) event.get("key");
		        		secret = (String) event.get("secret");
			        	
			        	startSwipe();
		        	}
		        	else{
		        	
		        		//put a failed box
		        	}
		        }})
		  .show();
	}

	public static DBCollection getEvents(){
		
		return events;
	}
	
	public static String getKey(){
		
		return key;
	}
	
	public static int getVid(){
	
		return vid;
	}
	
	protected void onDestroy() {
        super.onDestroy();
        if (compatible) {
          try {
            scanner.close();
            scanner.destroy();
          } catch (MoodstocksError e) {
            e.log();
          }
        }
      }
      
    
    @Override
    public void onSyncStart() {
      Log.d("Moodstocks SDK", "Sync will start.");
    }

    @Override
    public void onSyncComplete() {
      try {
        Log.d("Moodstocks SDK", String.format("Sync succeeded (%d image(s))", scanner.count()));
      } catch (MoodstocksError e) {
        e.printStackTrace();
      }
    }

    @Override
    public void onSyncFailed(MoodstocksError e) {
      Log.d("Moodstocks SDK", "Sync error: " + e.getErrorCode());
    }

    @Override
    public void onSyncProgress(int total, int current) {
      int percent = (int) ((float) current / (float) total * 100);
      Log.d("Moodstocks SDK", String.format("Sync progressing: %d%%", percent));
    }
  
}
