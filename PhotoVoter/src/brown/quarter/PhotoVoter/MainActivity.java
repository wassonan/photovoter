package brown.quarter.PhotoVoter;


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
import com.moodstocks.android.MoodstocksError;
import com.moodstocks.android.Scanner;

public class MainActivity extends Activity implements Scanner.SyncListener{
	private static String key = "";
    private static String secret = "";
    private static DBCollection events = null;
    
    private boolean compatible = false;
    private Scanner scanner;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    MongoClient mongoClient = null;
	    
		try{
			String textUri = "mongodb://photovoter:photovoter@ds027419.mongolab.com:27419/photovoter";
			MongoClientURI uri = new MongoClientURI(textUri);
			System.out.println(uri);
			mongoClient = new MongoClient(uri);
		}catch(Exception e){
			
			Log.d("mainactivity", "Server not found");
			System.out.println("Server not found");
			System.exit(-1);
		}
		
		DB db = mongoClient.getDB("photovoter");
    	events = db.getCollection("events");
		
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

	public DBCollection getEvents(){
		
		return events;
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
