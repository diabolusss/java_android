package com.example.cdtimer;

import java.io.IOException;

import com.cdtimer.Timer;
import com.example.cdtimer.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class FullscreenActivity_deprecated extends Activity {
	/** Called when the activity is first created. */
	/**
	 * Whether or not the system UI should be auto-hidden after
	 * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
	 */
	private static final boolean AUTO_HIDE = false;

	/**
	 * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
	 * user interaction before hiding the system UI.
	 */
	private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

	/**
	 * If set, will toggle the system UI visibility upon interaction. Otherwise,
	 * will show the system UI visibility upon interaction.
	 */
	private static final boolean TOGGLE_ON_CLICK = true;

	/**
	 * The flags to pass to {@link SystemUiHider#getInstance}.
	 */
	private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

	/**
	 * The instance of the {@link SystemUiHider} for this activity.
	 */
	private SystemUiHider mSystemUiHider;
	
	//button status flag
	public static Button btnStart;
	
	public static int started = 0;
	public static int initial_start = 1;	
	
	public static TextView timerValue;
	public static TextView currRound;
	public static TextView txtDebug;
	
	Timer timer = null;
	
	public static MediaPlayer player;
	
	public static int soundRoundStart, soundRoundEnd, soundTimerFinish;
	
	public static SoundPool soundPool;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set the user interface layout for this Activity
	    // The layout file is defined in the project res/layout/main_activity.xml file
		setContentView(R.layout.activity_fullscreen);

		// Initialize members ... so we can manipulate them later
		final View controlsView = findViewById(R.id.fullscreen_content_controls);
		final View contentView = findViewById(R.id.fullscreen_content);
		
		btnStart	= (Button) findViewById(R.id.btn_start);
		
		timerValue 	= (TextView) findViewById(R.id.timerValue);	
		txtDebug 	= (TextView) findViewById(R.id.txtDebug);	
		currRound	= (TextView) findViewById(R.id.currRound);	
		
		soundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 100);
		soundRoundStart = soundPool.load(this, R.raw.air_horne, 1);
		soundRoundEnd	= soundPool.load(this, R.raw.pring, 1);
		//soundTimerFinish= soundPool.load(this, R.raw.yaba_daba_doo, 1);

		// Set up an instance of SystemUiHider to control the system UI for
		// this activity.
		mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
		mSystemUiHider.setup();
		mSystemUiHider.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
			// Cached values.
			int mControlsHeight;
			int mShortAnimTime;

			@Override
			@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
			public void onVisibilityChange(boolean visible) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
					// If the ViewPropertyAnimator API is available
					// (Honeycomb MR2 and later), use it to animate the
					// in-layout UI controls at the bottom of the
					// screen.
					if (mControlsHeight == 0) {
						mControlsHeight = controlsView.getHeight();
					}
					if (mShortAnimTime == 0) {
						mShortAnimTime = getResources().getInteger(
								android.R.integer.config_shortAnimTime);
					}
					controlsView
							.animate()
							.translationY(visible ? 0 : mControlsHeight)
							.setDuration(mShortAnimTime);
				} else {
					// If the ViewPropertyAnimator APIs aren't
					// available, simply show or hide the in-layout UI
					// controls.
					controlsView.setVisibility(visible ? View.VISIBLE
							: View.GONE);
				}

				if (visible && AUTO_HIDE) {
					// Schedule a hide().
					delayedHide(AUTO_HIDE_DELAY_MILLIS);
				}
			}
		});

		// Set up the user interaction to manually show or hide the system UI.
		contentView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (TOGGLE_ON_CLICK) {
					mSystemUiHider.toggle();
				} else {
					mSystemUiHider.show();
				}
			}
		});

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
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		// Trigger the initial hide() shortly after the activity has been
		// created, to briefly hint to the user that UI controls
		// are available.
		delayedHide(100);
	}

	/**
	 * Touch listener to use for in-layout UI controls to delay hiding the
	 * system UI. This is to prevent the jarring behavior of controls going away
	 * while interacting with activity UI.
	 */
	View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			if (AUTO_HIDE) {
				delayedHide(AUTO_HIDE_DELAY_MILLIS);
			}
			return false;
		}
	};

	Handler mHideHandler = new Handler();
	Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			mSystemUiHider.hide();
		}
	};

	/**
	 * Schedules a call to hide() in [delay] milliseconds, canceling any
	 * previously scheduled calls.
	 */
	private void delayedHide(int delayMillis) {
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, delayMillis);
	}
	
	private Timer initTimer(){
		Timer localTimer;
		try{
			int const_total_rounds = Integer.parseInt( ((EditText) findViewById(R.id.roundCount)).getText().toString() );
			float const_round_time_minutes = (float) Double.parseDouble( ((EditText)findViewById(R.id.roundTime)).getText().toString());
			float const_cd_time_minutes = (float) Double.parseDouble( ((EditText)findViewById(R.id.cooldownTime)).getText().toString());

			((TextView) findViewById(R.id.totalRounds)).setText(
					" из " + ((EditText) findViewById(R.id.roundCount)).getText().toString()
					);
			
			long const_round_time_millis 	= (long) (const_round_time_minutes * 60 * 1000);
			long const_cd_time_millis 		= (long) (const_cd_time_minutes * 60 * 1000);
			
			localTimer = new Timer(FullscreenActivity_deprecated.this, const_total_rounds, const_round_time_millis, const_cd_time_millis);
			return localTimer;
			
		}catch(Exception e){
			System.out.println("ERROR - " + e.getMessage());
			btnStart.setText("Push me");
			
			android.app.AlertDialog.Builder alert = new AlertDialog.Builder(FullscreenActivity_deprecated.this);
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

