package com.cdtimer;

import com.example.cdtimer.FullscreenActivity;
import com.example.cdtimer.R;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.widget.ProgressBar;

public class Timer implements Runnable{	
	long threadRunTimeMS = 0L;
	long timeSwapBuff = 0L;
	long totalTimerTimeMS = 0L;
	long threadStartTimeMS = 0L;
	
	long roundStartTimeMS = 0L;
	long roundRunTimeMS = 0L;
	int roundRunTimeS_prev = 0; //variable to control last 10s call, so that its called only each second, not ms
		
	int restRoundStartAlertFlag = 1;
	int beepAlertFlag = 1;//to call 10sec sound once 
	
	Integer curr_round = 1;
	
	final int 
		CONST_TOTAL_ROUNDS		
		;
	
	int CONST_BEEP_DURATION_MS = 10000;
	
	final long 
		CONST_ROUND_DURATION_MS_WORK,
		CONST_ROUND_DURATION_MS_COOLDOWN
		;
	
	Handler customHandler = new Handler();
	
	Context mainContext = null;
	
	public Timer(Context mainContext, int totalRounds, long roundTime, long cdTime){
		this.mainContext 				= mainContext;
		this.CONST_TOTAL_ROUNDS 		= totalRounds;
		this.CONST_ROUND_DURATION_MS_WORK 	= roundTime;
		this.CONST_ROUND_DURATION_MS_COOLDOWN 		= cdTime;
		
		if(CONST_BEEP_DURATION_MS > CONST_ROUND_DURATION_MS_WORK) CONST_BEEP_DURATION_MS = (int)CONST_ROUND_DURATION_MS_WORK;
		
		FullscreenActivity.pbRoundsProgress.setMax(CONST_TOTAL_ROUNDS);
	}
	
	public void start(){		
		threadStartTimeMS = SystemClock.uptimeMillis();
		roundStartTimeMS = threadStartTimeMS;
		customHandler.postDelayed(this, 0);
		
		roundRunTimeS_prev = 0;
		
		FullscreenActivity.currRound.setText(curr_round.toString());				
		FullscreenActivity.pbRoundsProgress.setProgress(curr_round);
		FullscreenActivity.txtDebug.setText("TimerStart"); 
		
		this.run();		
		
		FullscreenActivity.soundPool.play(FullscreenActivity.soundRoundStart, .99f, .99f, 1, 0, 1f);
	}
	
	public void stop(){
		customHandler.removeCallbacks(this);
		
		FullscreenActivity.initial_start = 1;		
		FullscreenActivity.btnStart.setText("Start");
		FullscreenActivity.started = 0;
		
		this.threadRunTimeMS 	= 0L;
		this.timeSwapBuff 	= 0L;
		this.totalTimerTimeMS 	= 0L;
		this.threadStartTimeMS		= 0L;
		
		this.curr_round 	= 1;
		
		FullscreenActivity.timerValue.setText("00:00:000");
		FullscreenActivity.currRound.setText(curr_round.toString());
	}
	
	public void pause(){
		timeSwapBuff += threadRunTimeMS;
		System.out.println("[DBG]"+totalTimerTimeMS+" - timeSwapBuff="+timeSwapBuff);
		customHandler.removeCallbacks(this);
	}
	
	public void reset(){
		stop();
		//start();
	}
	
	public long getCurrTimeMillis(){
		return this.threadRunTimeMS;
	}

	@Override
	public void run() {
		//this thread run duration
		threadRunTimeMS = SystemClock.uptimeMillis() - threadStartTimeMS;		
		//add thread duration time to last pause point
		totalTimerTimeMS = timeSwapBuff + threadRunTimeMS;

		int secs = (int) (totalTimerTimeMS / 1000);
		int mins = secs / 60;
		secs = secs % 60;
		int milliseconds = (int) (totalTimerTimeMS % 1000);
		FullscreenActivity.timerValue.setText("" + mins + ":"
				+ String.format("%02d", secs) + ":"
				+ String.format("%03d", milliseconds));
		
		//round duration
		roundRunTimeMS = SystemClock.uptimeMillis() - roundStartTimeMS;
		
		//if round passed-> new round or end
		if(threadRunTimeMS > curr_round * (CONST_ROUND_DURATION_MS_WORK + CONST_ROUND_DURATION_MS_COOLDOWN)){
			if(curr_round < CONST_TOTAL_ROUNDS) {
				//save new round start time to calculate it duration
				roundStartTimeMS = SystemClock.uptimeMillis();
				roundRunTimeS_prev = 0;
				beepAlertFlag = 1;
				
				FullscreenActivity.soundPool.play(FullscreenActivity.soundRoundStart, .99f, .99f, 1, 0, 1f);
				
				curr_round++ ;
				
				FullscreenActivity.currRound.setText(curr_round.toString());				
				FullscreenActivity.pbRoundsProgress.setProgress(curr_round);
				FullscreenActivity.txtDebug.setText(
						"Beep! За работу!"
						);		

				System.out.println("[DBG]"+totalTimerTimeMS+" - WORK");
				
			}else{
				FullscreenActivity.soundPool.play(FullscreenActivity.soundTimerFinish, .99f, .99f, 1, 0, 1f);
				
				android.app.AlertDialog.Builder alert = new AlertDialog.Builder(mainContext);
	            alert.setTitle("FINISH");
	            alert.setMessage("Отсчет закончен!" );
	            alert.setPositiveButton("OK", null);
	            alert.show();
	            
	            FullscreenActivity.txtDebug.setText(
						"Beep! Приехали!"
						);
	            
	            stop();
	            return;
			}
			restRoundStartAlertFlag = 1;
			
		//otherwise its rest
		}else if(threadRunTimeMS > ((curr_round * CONST_ROUND_DURATION_MS_WORK)+((curr_round-1) * CONST_ROUND_DURATION_MS_COOLDOWN)) 
				&& restRoundStartAlertFlag == 1){
			//just to reset start time
			roundStartTimeMS = SystemClock.uptimeMillis();
			roundRunTimeS_prev = 0;
			 
			FullscreenActivity.txtDebug.setText(
					"Beep! Отдыхаем!"
					);
			System.out.println("[DBG]"+totalTimerTimeMS+" - REST");
			
			FullscreenActivity.soundPool.play(FullscreenActivity.soundRoundEnd, .99f, .99f, 1, 0, 1f);
			restRoundStartAlertFlag = 0;
			
		//beep last 10 secs of round
		}else if(
				(roundRunTimeMS > (CONST_ROUND_DURATION_MS_WORK-CONST_BEEP_DURATION_MS)) 
				&& ((int) (roundRunTimeMS/1000) != roundRunTimeS_prev) 
				//&& beepAlertFlag == 1
				){
			FullscreenActivity.soundPool.play(FullscreenActivity.soundTimerBeep, .99f, .99f, 1, 0, 1f);
			
			System.out.println("[DBG]"+totalTimerTimeMS+" - beep last 10 secs of round: roundRunTimeMS:"+roundRunTimeMS+" roundRunTimeS_prev:"+roundRunTimeS_prev );
			roundRunTimeS_prev = (int) (roundRunTimeMS/1000);
			beepAlertFlag = 0;
			
		}
		
		customHandler.postDelayed(this, 0);		
	}
}
