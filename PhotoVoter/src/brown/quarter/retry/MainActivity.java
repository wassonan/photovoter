package brown.quarter.retry;


import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import brown.quarter.photovoter.R;
import com.moodstocks.android.MoodstocksError;
import com.moodstocks.android.Scanner;

public class MainActivity extends Activity implements Scanner.SyncListener{
	private static final String API_KEY = "nzclqxo8kfkjry7bessy";
    private static final String API_SECRET = "1K3hcCwI6wS6WCet";

    private boolean compatible = false;
    private Scanner scanner;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
	}

	
	public void startSwipe(){
		compatible = Scanner.isCompatible();
		
        if (compatible) {
          try {
            scanner = Scanner.get();
            scanner.open(this, API_KEY, API_SECRET);
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
		        	startSwipe();
		        }})
		  .show();
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
