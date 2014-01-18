package brown.quarter.PhotoVoter;

import java.util.ArrayList;
import java.util.Locale;

import brown.quarter.photovoter.R;

import com.mongodb.DBCollection;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import com.moodstocks.android.MoodstocksError;
import com.moodstocks.android.Result;
import com.moodstocks.android.ScannerSession;
import com.moodstocks.android.ScannerSession.Listener;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

@SuppressLint("ValidFragment")
public class Swipe extends FragmentActivity implements ScannerSession.Listener{

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;
	int ScanOptions = Result.Type.IMAGE | Result.Type.EAN8
			| Result.Type.EAN13 | Result.Type.QRCODE | Result.Type.DATAMATRIX;
	ScannerSession session = null;
	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_swipe);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.swipe, menu);
		return true;
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a DummySectionFragment (defined as a static inner class
			// below) with the page number as its lone argument.
			if (position == 0){
				return new ScannerFragment();
			}else{
				return new TableFragment();
			}
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_section1).toUpperCase(l);
			case 1:
				return getString(R.string.title_section2).toUpperCase(l);
			}
			return null;
		}
	}

	public class TableFragment extends Fragment {
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			Log.d("SWAG", "create table view");
			View rootView = inflater.inflate(R.layout.table_fragment,
					container, false);
			ScrollView table = (ScrollView) rootView.findViewById(R.id.table);
			//UPDATE TABLE HERE
			return rootView;
		}
	}

	
	public class ScannerFragment extends Fragment{
		SurfaceView preview;
		@Override
		public void onStart(){
			super.onStart();
			try {
				if(session == null){
					Log.d("SWAG", "create session");
					session = new ScannerSession(this.getActivity(), (Listener) this.getActivity(), preview);
				}
			} catch (MoodstocksError e) {
				e.log();
			}

			// set session options
			session.setOptions(ScanOptions);
			session.resume();
		}
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			Log.d("SWAG", "create view");
			View rootView = inflater.inflate(R.layout.fragment_swipe_dummy,
					container, false);
			preview = (SurfaceView) rootView.findViewById(R.id.preview);
			// Create a scanner session
			
			Log.d("SWAG", "return");

			return rootView;
		}

	}
	@Override
	public void onApiSearchStart() {
		// TODO Auto-generated method stub
		Log.d("SWAG", "start");
	}

	@Override
	public void onApiSearchComplete(Result result) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onApiSearchFailed(MoodstocksError e) {
		Log.d("SWAG - api", e.getMessage());

	}

	@Override
	public void onScanComplete(Result result) {
		// TODO Auto-generated method stub
		if (result != null){
			
			String imgid = result.toString();
			DBCollection events = MainActivity.getEvents();
			DBObject teams = events.findOne(new BasicDBObject("key", MainActivity.getKey()));

			ArrayList<DBObject> teamList = (ArrayList<DBObject>) teams.get("teams");
			
			for(DBObject team: teamList){
			
				if(team.get("imgid").equals(imgid)){
					
					ArrayList voters = (ArrayList) team.get("voters");
					voters.add(new BasicDBObject("vid", MainActivity.getVid()));
					team.put("voters", voters);
				}
			}
			
			teams.put("teams", teamList);
			events.findAndModify(new BasicDBObject("key", MainActivity.getKey()),
					teams);
			
			
			new AlertDialog.Builder(Swipe.this)
			.setTitle("Do you want to vote for")
			.setMessage(imgid).setNeutralButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) { 
					session.resume();
				}}).show();
			session.pause();
		}
	}

	@Override
	public void onScanFailed(MoodstocksError error) {
		// TODO Auto-generated method stub
		Log.d("SWAG - scan", error.getMessage());

	}
}



