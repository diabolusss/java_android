package com.example.cdtimer;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cdtimer.Timer;

@SuppressLint("InlinedApi")
public class FullscreenActivity extends Activity {
		
	//button status flag
	public static Button btnStart;
	
	public static int started = 0;
	public static int initial_start = 1;	
	
	public static TextView timerValue;
	public static TextView currRound;
	public static TextView txtDebug;
	
	public static ProgressBar pbRoundsProgress;
	
	Timer timer = null;
	
	public static MediaPlayer player;
	
	public static int soundRoundStart, soundRoundEnd, soundTimerFinish, soundTimerBeep;
	
	public static SoundPool soundPool;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// If the Android version is lower than Jellybean, use this call to hide
        // the status bar.
        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }        
		
		// Set the user interface layout for this Activity
	    // The layout file is defined in the project res/layout/main_activity.xml file
		setContentView(R.layout.activity_fullscreen);
		
		if (Build.VERSION.SDK_INT >= 16) {
			 View decorView = getWindow().getDecorView();
			 // Hide the status bar.
			 int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
			 decorView.setSystemUiVisibility(uiOptions);
			 // Remember that you should never show the action bar if the
			 // status bar is hidden, so hide that too if necessary.
			 ActionBar actionBar = getActionBar();
			 actionBar.hide();
		}
		
		btnStart	= (Button) findViewById(R.id.btn_start);
		
		timerValue 	= (TextView) findViewById(R.id.timerValue);	
		txtDebug 	= (TextView) findViewById(R.id.txtDebug);	
		currRound	= (TextView) findViewById(R.id.currRound);	
		
		/**
		 * Turns out that SoundPool have two bugs/restrictions.
The sound volume is from 0.0f to but not inclusive 1.0f. Both 1.0f and 0.0f are mute, so you must cap your volume at 0.99f.
Loading samples into the SoundPool that do not fit in ram will not result in an exception being thrown, nor is there a soundId returned that can be checked for failure. So you must look at your logs, and pray to the Android gods that your samples fit on the target device.
		 http://stackoverflow.com/questions/1394694/why-is-my-soundpool-mute
		 */
		 // Set the hardware buttons to control the music
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		
		soundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 100);
		soundRoundStart = soundPool.load(this, R.raw.air_horne, 1);
		soundRoundEnd	= soundPool.load(this, R.raw.pring, 1);
		soundTimerFinish= soundPool.load(this, R.raw.gong, 1);
		soundTimerBeep	= soundPool.load(this, 
				R.raw.beep_once_race, 
				//R.raw.cut13s_01, 
				1);
		
		//rotate status bar so that value starts&ends at circle bottom
		pbRoundsProgress = (ProgressBar) findViewById(R.id.progressBarRoundsCircular);
		Animation an = new RotateAnimation(0.0f, 90.0f, RotateAnimation.RELATIVE_TO_SELF, 0.5f,RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		an.setFillAfter(true);
		pbRoundsProgress.startAnimation(an);
		
		//zero bar value
		pbRoundsProgress.setProgress(0);
		
		// Upon interacting with UI controls, delay any scheduled hide()
		// operations to prevent the jarring behavior of controls going away
		// while interacting with the UI.
		btnStart.setOnTouchListener(
				
				new OnTouchListener(){
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						
						if (event.getAction() == MotionEvent.ACTION_UP) if(started == 0){
							
							//init timer
							if(initial_start == 1){
								
								timer = initTimer();					
								
								if (timer != null) initial_start = 0;
								else return false;
							}				            
							
							if(timer != null) {
								timer.start();	
								btnStart.setText("Pause");
								started = 1;	
							}						
							
						}else if(started == 1){
							if(timer != null) {
								timer.pause();
								btnStart.setText("Start");
								started = 0;
							}
						}
						
						return false;						
					}					
				}
		);
		
		findViewById(R.id.btnReset).setOnClickListener(new View.OnClickListener() {		
			public void onClick(View view) {			
				initial_start = 1;
				started 	  = 0;
				
				if(timer!=null) {
					timer.reset();
					timer = null;
				}		
				
				pbRoundsProgress.setProgress(0);
				
				btnStart.setText("Start");			
			}
		});
		
		findViewById(R.id.btnQuit).setOnClickListener(new View.OnClickListener() {		
			public void onClick(View view) {			
				finish();
			}
		});		
		
		final TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        PhoneStateListener phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String number) {
                String currentPhoneState = null;
                switch (state) {
                    case TelephonyManager.CALL_STATE_RINGING:
                        //currentPhoneState = "Device is ringing. Call from " + number + ".\n\n";
                        if(started == 1){
							if(timer != null) {
								timer.pause();
								btnStart.setText("Start");
								started = 0;
							}
						}
                        break;
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        //currentPhoneState = "Device call state is currently Off Hook.\n\n";
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        //currentPhoneState = "Device call state is currently Idle.\n\n";
                        break;
                }
            }
        };
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    
	}
	
	@Override
	public void finish() {
		System.out.println("System finish");
		
		super.finish();
		android.os.Process.killProcess(android.os.Process.myPid());
	}
	
	private Timer initTimer(){
		Timer localTimer;
		try{
			int const_total_rounds = Integer.parseInt( ((EditText) findViewById(R.id.roundCount)).getText().toString() );
			float const_round_time_minutes = (float) Double.parseDouble( ((EditText)findViewById(R.id.roundTime)).getText().toString());
			float const_cd_time_minutes = (float) Double.parseDouble( ((EditText)findViewById(R.id.cooldownTime)).getText().toString());

			((TextView) findViewById(R.id.totalRounds)).setText(
						((EditText) findViewById(R.id.roundCount)).getText().toString()
					);			
			
			long const_round_time_millis 	= (long) (const_round_time_minutes * 60 * 1000);
			long const_cd_time_millis 		= (long) (const_cd_time_minutes * 60 * 1000);
			
			System.out.println("[DBG] - const_round_time_millis:" + const_round_time_millis+"; const_cd_time_millis:"+const_cd_time_millis);
			
			localTimer = new Timer(FullscreenActivity.this, const_total_rounds, const_round_time_millis, const_cd_time_millis);
			return localTimer;
			
		}catch(Exception e){
			System.out.println("ERROR - " + e.getMessage());
			btnStart.setText("Push me");
			
			android.app.AlertDialog.Builder alert = new AlertDialog.Builder(FullscreenActivity.this);
            alert.setTitle("ERROR");
            alert.setMessage("Ошибка при присвоении значений!");
            alert.setPositiveButton("OK", null);
            alert.show();
            
			return null;
		}
	}
	
	@Override
	protected void onStop() {
	    super.onStop();  // Always call the superclass method first
	    
	    if(timer != null) timer.stop();
	}
}

