package com.neurosky.algo_sdk_sample;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.HandlerThread;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import android.content.res.AssetManager;
import android.app.AlertDialog;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.neurosky.AlgoSdk.NskAlgoBCQType;
import com.neurosky.AlgoSdk.NskAlgoConfig;
import com.neurosky.AlgoSdk.NskAlgoDataType;
import com.neurosky.AlgoSdk.NskAlgoSdk;
import com.neurosky.AlgoSdk.NskAlgoSignalQuality;
import com.neurosky.AlgoSdk.NskAlgoState;
import com.neurosky.AlgoSdk.NskAlgoType;
import com.neurosky.connection.ConnectionStates;
import com.neurosky.connection.TgStreamHandler;
import com.neurosky.connection.TgStreamReader;
import com.neurosky.connection.DataType.MindDataType;

import com.androidplot.xy.*;

public class MainActivity extends Activity {

    final String TAG = "MainActivityTag";

    // graph plot variables
    private final static int X_RANGE = 50;
    private SimpleXYSeries apSeries = null;
    private SimpleXYSeries abs_meSeries = null;
    private SimpleXYSeries diff_meSeries = null;
    private SimpleXYSeries abs_fSeries = null;
    private SimpleXYSeries diff_fSeries = null;
    private SimpleXYSeries crSeries = null;
    private SimpleXYSeries alSeries = null;
    private SimpleXYSeries cpSeries = null;
    private SimpleXYSeries etSeries = null;
    private SimpleXYSeries yySeries = null;
    private SimpleXYSeries bp_deltaSeries = null;
    private SimpleXYSeries bp_thetaSeries = null;
    private SimpleXYSeries bp_alphaSeries = null;
    private SimpleXYSeries bp_betaSeries = null;
    private SimpleXYSeries bp_gammaSeries = null;

    // COMM SDK handles
    private TgStreamReader tgStreamReader;
    private BluetoothAdapter mBluetoothAdapter;

    // internal variables
    private boolean bInited = false;
    private boolean bRunning = false;
    private NskAlgoType currentSelectedAlgo;
    private int apInterval = 1;
    private int meInterval = 1;
    private int me2Interval = 30;
    private int fInterval = 1;
    private int f2Interval = 30;
    private int crInterval = 1;
    private int alInterval = 1;
    private int cpInterval = 1;
    private int etInterval = 1;
    private int yyInterval = 1;
    private NskAlgoConfig.NskAlgoBCQThreshold crThreshold = NskAlgoConfig.NskAlgoBCQThreshold.NSK_ALGO_BCQ_THRESHOLD_LIGHT;
    private NskAlgoConfig.NskAlgoBCQThreshold alThreshold = NskAlgoConfig.NskAlgoBCQThreshold.NSK_ALGO_BCQ_THRESHOLD_LIGHT;
    private NskAlgoConfig.NskAlgoBCQThreshold cpThreshold = NskAlgoConfig.NskAlgoBCQThreshold.NSK_ALGO_BCQ_THRESHOLD_LIGHT;
    private int crWindow = 60;
    private int alWindow = 60;
    private int cpWindow = 60;

    // canned data variables
    private short raw_data[] = {0};
    private int raw_data_index= 0;
    private float ap[];
    private int me_index = 0;
    private int ap_index = 0;
    private int f_index = 0;
    private float output_data[];
    private int output_data_count = 0;
    private int raw_data_sec_len = 235;

    private int crCount = 0;
    private int alCount = 0;
    private int cpCount = 0;
    private int crValid = -1;
    private int alValid = -1;
    private int cpValid = -1;
    
    // UI components
    private XYPlot plot;
    private EditText text;

    private Button headsetButton;
    private Button cannedButton;
    private Button setAlgosButton;
    private Button setIntervalButton;
    private Button startButton;
    private Button stopButton;

    private SeekBar intervalSeekBar;
    private TextView intervalText;

    private Button apText;
    private Button meText;
    private Button me2Text;
    private Button fText;
    private Button f2Text;
    private Button crText;
    private Button alText;
    private Button cpText;
    private Button etText;
    private Button yyText;
    private Button bpText;

    private TextView attValue;
    private TextView medValue;

    private CheckBox apCheckBox;
    private CheckBox meCheckBox;
    private CheckBox me2CheckBox;
    private CheckBox fCheckBox;
    private CheckBox f2CheckBox;
    private CheckBox attCheckBox;
    private CheckBox medCheckBox;
    private CheckBox blinkCheckBox;
    private CheckBox crCheckBox;
    private CheckBox alCheckBox;
    private CheckBox cpCheckBox;
    private CheckBox etCheckBox;
    private CheckBox yyCheckBox;
    private CheckBox bpCheckBox;

    private TextView stateText;
    private TextView sqText;

    private ImageView blinkImage;

    private NskAlgoSdk nskAlgoSdk;

    private int bLastOutputInterval = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nskAlgoSdk = new NskAlgoSdk();


        try {
            // (1) Make sure that the device supports Bluetooth and Bluetooth is on
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                Toast.makeText(
                        this,
                        "Please enable your Bluetooth and re-run this program !",
                        Toast.LENGTH_LONG).show();
                //finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "error:" + e.getMessage());
            return;
        }

        headsetButton = (Button)this.findViewById(R.id.headsetButton);
        cannedButton = (Button)this.findViewById(R.id.cannedDatabutton);
        setAlgosButton = (Button)this.findViewById(R.id.setAlgosButton);
        setIntervalButton = (Button)this.findViewById(R.id.setIntervalButton);
        startButton = (Button)this.findViewById(R.id.startButton);
        stopButton = (Button)this.findViewById(R.id.stopButton);

        intervalSeekBar = (SeekBar)this.findViewById(R.id.intervalSeekBar);
        intervalText = (TextView)this.findViewById(R.id.intervalText);

        apText = (Button)this.findViewById(R.id.apTitle);
        meText = (Button)this.findViewById(R.id.meTitle);
        me2Text = (Button)this.findViewById(R.id.me2Title);
        fText = (Button)this.findViewById(R.id.fTitle);
        f2Text = (Button)this.findViewById(R.id.f2Title);
        crText = (Button)this.findViewById(R.id.crTitle);
        alText = (Button)this.findViewById(R.id.alTitle);
        cpText = (Button)this.findViewById(R.id.cpTitle);
        etText = (Button)this.findViewById(R.id.etTitle);
        yyText = (Button)this.findViewById(R.id.yyTitle);
        bpText = (Button)this.findViewById(R.id.bpTitle);

        attValue = (TextView)this.findViewById(R.id.attText);
        medValue = (TextView)this.findViewById(R.id.medText);

        apCheckBox = (CheckBox)this.findViewById(R.id.apCheckBox);
        meCheckBox = (CheckBox)this.findViewById(R.id.meCheckBox);
        me2CheckBox = (CheckBox)this.findViewById(R.id.me2CheckBox);
        fCheckBox = (CheckBox)this.findViewById(R.id.fCheckBox);
        f2CheckBox = (CheckBox)this.findViewById(R.id.f2CheckBox);
        attCheckBox = (CheckBox)this.findViewById(R.id.attCheckBox);
        medCheckBox = (CheckBox)this.findViewById(R.id.medCheckBox);
        blinkCheckBox = (CheckBox)this.findViewById(R.id.blinkCheckBox);
        crCheckBox = (CheckBox)this.findViewById(R.id.crCheckBox);
        alCheckBox = (CheckBox)this.findViewById(R.id.alCheckBox);
        cpCheckBox = (CheckBox)this.findViewById(R.id.cpCheckBox);
        etCheckBox = (CheckBox)this.findViewById(R.id.etCheckBox);
        yyCheckBox = (CheckBox)this.findViewById(R.id.yyCheckBox);
        bpCheckBox = (CheckBox)this.findViewById(R.id.bpCheckBox);

        blinkImage = (ImageView)this.findViewById(R.id.blinkImage);

        stateText = (TextView)this.findViewById(R.id.stateText);
        sqText = (TextView)this.findViewById(R.id.sqText);

        headsetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                output_data_count = 0;
                output_data = null;
                me_index = 0;
                ap_index = 0;

                raw_data = new short[512];
                raw_data_index = 0;

                cannedButton.setEnabled(false);
                headsetButton.setEnabled(false);

                startButton.setEnabled(false);

                // Example of constructor public TgStreamReader(BluetoothAdapter ba, TgStreamHandler tgStreamHandler)
                tgStreamReader = new TgStreamReader(mBluetoothAdapter,callback);

                if(tgStreamReader != null && tgStreamReader.isBTConnected()){

                    // Prepare for connecting
                    tgStreamReader.stop();
                    tgStreamReader.close();
                }

                // (4) Demo of  using connect() and start() to replace connectAndStart(),
                // please call start() when the state is changed to STATE_CONNECTED
                tgStreamReader.connect();
            }
        });

        cannedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                output_data_count = 0;
                output_data = null;
                me_index = 0;
                ap_index = 0;

                System.gc();

                headsetButton.setEnabled(false);
                cannedButton.setEnabled(false);

                AssetManager assetManager = getAssets();
                InputStream inputStream = null;

                Log.d(TAG, "Reading output data");
                try {
                    int j;
                    // check the output count first
                    inputStream = assetManager.open("output_data.bin");
                    output_data_count = 0;
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    try {
                        String line = reader.readLine();
                        while (!(line == null || line.isEmpty())) {
                            output_data_count++;
                            line = reader.readLine();
                        }
                    } catch (IOException e) {

                    }
                    inputStream.close();

                    if (output_data_count > 0) {
                        inputStream = assetManager.open("output_data.bin");
                        output_data = new float[output_data_count];
                        ap = new float[output_data_count];
                        j = 0;
                        reader = new BufferedReader(new InputStreamReader(inputStream));
                        try {
                            String line = reader.readLine();
                            while (j < output_data_count) {
                                output_data[j++] = Float.parseFloat(line);
                                line = reader.readLine();
                            }
                        } catch (IOException e) {

                        }
                        inputStream.close();
                    }
                } catch (IOException e) {
                }


                Thread mThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        AssetManager assetManager = getAssets();
                        InputStream inputStream = null;
                        int count = 0;

                        Log.d(TAG, "Reading raw data");
                        try {
                            inputStream = assetManager.open("raw_data_em.bin");
                            raw_data = readData(inputStream, 512*raw_data_sec_len);
                            raw_data_index = 512*raw_data_sec_len;
                            short raw[] = new short[512];
                            short pq[] = new short[1];
                            pq[0] = 0;
                            inputStream.close();
                            /*while (count < raw_data_sec_len) {
                                for (int i=0;i<512;i++) {
                                    raw[i] = raw_data[count*512 + i];
                                }
                                //nskAlgoSdk.NskAlgoDataStream(NskAlgoDataType.NSK_ALGO_DATA_TYPE_BULK_EEG.value, raw_data, 512 * raw_data_sec_len);
                                nskAlgoSdk.NskAlgoDataStream(NskAlgoDataType.NSK_ALGO_DATA_TYPE_EEG.value, raw, 512);
                                nskAlgoSdk.NskAlgoDataStream(NskAlgoDataType.NSK_ALGO_DATA_TYPE_PQ.value, pq, 1);
                                count++;
                                Thread.sleep(500);
                                //Log.d(TAG, "Sent [" + count + "s]");
                            }*/
                            nskAlgoSdk.NskAlgoDataStream(NskAlgoDataType.NSK_ALGO_DATA_TYPE_BULK_EEG.value, raw_data, 512 * raw_data_sec_len);
                        } catch (IOException e) {

                        }/* catch (InterruptedException e) {
                            e.printStackTrace();
                        }*/
                        Log.d(TAG, "Finished reading data");
                    }
                });
                mThread.start();
            }
        });

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bRunning == false) {
                    // reset BCQ valid flags
                    crValid = -1;
                    alValid = -1;
                    cpValid = -1;
                    nskAlgoSdk.NskAlgoStart(false);
                } else {
                    nskAlgoSdk.NskAlgoPause();
                }
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nskAlgoSdk.NskAlgoStop();
            }
        });

        setAlgosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // check selected algos
                int algoTypes = 0;// = NskAlgoType.NSK_ALGO_TYPE_CR.value;

                startButton.setEnabled(false);
                stopButton.setEnabled(false);
                clearAllSeries();
                text.setVisibility(View.INVISIBLE);
                text.setText("");

                apText.setEnabled(false);
                meText.setEnabled(false);
                me2Text.setEnabled(false);
                fText.setEnabled(false);
                f2Text.setEnabled(false);
                crText.setEnabled(false);
                alText.setEnabled(false);
                cpText.setEnabled(false);
                etText.setEnabled(false);
                yyText.setEnabled(false);
                bpText.setEnabled(false);

                currentSelectedAlgo = NskAlgoType.NSK_ALGO_TYPE_INVALID;
                intervalSeekBar.setEnabled(false);
                setIntervalButton.setEnabled(false);
                intervalText.setText("--");

                apInterval = 1;
                meInterval = 1;
                me2Interval = 30;
                fInterval = 1;
                f2Interval = 30;
                crInterval = alInterval = cpInterval = 1;
                crThreshold = alThreshold = cpThreshold = NskAlgoConfig.NskAlgoBCQThreshold.NSK_ALGO_BCQ_THRESHOLD_LIGHT;
                crWindow = alWindow = cpWindow = 60;
                etInterval = 1;
                yyInterval = 5;

                attValue.setText("--");
                medValue.setText("--");

                stateText.setText("");
                sqText.setText("");

                if (apCheckBox.isChecked()) {
                    algoTypes += NskAlgoType.NSK_ALGO_TYPE_AP.value;
                    apText.setEnabled(true);
                    apSeries = createSeries("AP");
                }
                if (meCheckBox.isChecked()) {
                    algoTypes += NskAlgoType.NSK_ALGO_TYPE_ME.value;
                    meText.setEnabled(true);
                    abs_meSeries = createSeries("Abs ME");
                    diff_meSeries = createSeries("Diff ME");
                }
                if (me2CheckBox.isChecked()) {
                    algoTypes += NskAlgoType.NSK_ALGO_TYPE_ME2.value;
                    me2Text.setEnabled(true);
                }
                if (fCheckBox.isChecked()) {
                    algoTypes += NskAlgoType.NSK_ALGO_TYPE_F.value;
                    fText.setEnabled(true);
                    abs_fSeries = createSeries("Abs F");
                    diff_fSeries = createSeries("Diff F");
                }
                if (f2CheckBox.isChecked()) {
                    algoTypes += NskAlgoType.NSK_ALGO_TYPE_F2.value;
                    f2Text.setEnabled(true);
                }
                if (medCheckBox.isChecked()) {
                    algoTypes += NskAlgoType.NSK_ALGO_TYPE_MED.value;
                }
                if (attCheckBox.isChecked()) {
                    algoTypes += NskAlgoType.NSK_ALGO_TYPE_ATT.value;
                }
                if (blinkCheckBox.isChecked()) {
                    algoTypes += NskAlgoType.NSK_ALGO_TYPE_BLINK.value;
                }
                if (crCheckBox.isChecked()) {
                    algoTypes += NskAlgoType.NSK_ALGO_TYPE_CR.value;
                    crText.setEnabled(true);
                    crSeries = createSeries("CR");
                }
                if (alCheckBox.isChecked()) {
                    algoTypes += NskAlgoType.NSK_ALGO_TYPE_AL.value;
                    alText.setEnabled(true);
                    alSeries = createSeries("AL");
                }
                if (cpCheckBox.isChecked()) {
                    algoTypes += NskAlgoType.NSK_ALGO_TYPE_CP.value;
                    cpText.setEnabled(true);
                    cpSeries = createSeries("CP");
                }
                if (etCheckBox.isChecked()) {
                    algoTypes += NskAlgoType.NSK_ALGO_TYPE_ET.value;
                    etText.setEnabled(true);
                    etSeries = createSeries("ET");
                }
                if (yyCheckBox.isChecked()) {
                    algoTypes += NskAlgoType.NSK_ALGO_TYPE_YY.value;
                    yyText.setEnabled(true);
                    yySeries = createSeries("YY");
                }
                if (bpCheckBox.isChecked()) {
                    algoTypes += NskAlgoType.NSK_ALGO_TYPE_BP.value;
                    bpText.setEnabled(true);
                    bp_deltaSeries = createSeries("Delta");
                    bp_thetaSeries = createSeries("Theta");
                    bp_alphaSeries = createSeries("Alpha");
                    bp_betaSeries = createSeries("Beta");
                    bp_gammaSeries = createSeries("Gamma");
                }


                if (algoTypes == 0) {
                    showDialog("Please select at least one algorithm");
                } else {
                    if (bInited) {
                        nskAlgoSdk.NskAlgoUninit();
                        bInited = false;
                    }
                    int ret = nskAlgoSdk.NskAlgoInit(algoTypes, getFilesDir().getAbsolutePath(), "Evaluation_Build");
                    if (ret == 0) {
                        bInited = true;
                        showToast("EEG Algo SDK has been initialized successfully", Toast.LENGTH_LONG);
                    } else {
                        showToast("EEG Algo SDK failed to initialize", Toast.LENGTH_LONG);
                        return;
                    }

                    Log.d(TAG, "NSK_ALGO_Init() " + ret);
                    String sdkVersion = "SDK ver.: " + nskAlgoSdk.NskAlgoSdkVersion();

                    if ((algoTypes & NskAlgoType.NSK_ALGO_TYPE_AP.value) != 0) {
                        sdkVersion += "\nAP ver.: " + nskAlgoSdk.NskAlgoAlgoVersion(NskAlgoType.NSK_ALGO_TYPE_AP.value);
                    }
                    if ((algoTypes & NskAlgoType.NSK_ALGO_TYPE_ME.value) != 0) {
                        sdkVersion += "\nME ver.: " + nskAlgoSdk.NskAlgoAlgoVersion(NskAlgoType.NSK_ALGO_TYPE_ME.value);
                    }
                    if ((algoTypes & NskAlgoType.NSK_ALGO_TYPE_ME2.value) != 0) {
                        sdkVersion += "\nME2 ver.: " + nskAlgoSdk.NskAlgoAlgoVersion(NskAlgoType.NSK_ALGO_TYPE_ME2.value);
                    }
                    if ((algoTypes & NskAlgoType.NSK_ALGO_TYPE_F.value) != 0) {
                        sdkVersion += "\nF ver.: " + nskAlgoSdk.NskAlgoAlgoVersion(NskAlgoType.NSK_ALGO_TYPE_F.value);
                    }
                    if ((algoTypes & NskAlgoType.NSK_ALGO_TYPE_F2.value) != 0) {
                        sdkVersion += "\nF2 ver.: " + nskAlgoSdk.NskAlgoAlgoVersion(NskAlgoType.NSK_ALGO_TYPE_F2.value);
                    }
                    if ((algoTypes & NskAlgoType.NSK_ALGO_TYPE_ATT.value) != 0) {
                        sdkVersion += "\nATT ver.: " + nskAlgoSdk.NskAlgoAlgoVersion(NskAlgoType.NSK_ALGO_TYPE_ATT.value);
                    }
                    if ((algoTypes & NskAlgoType.NSK_ALGO_TYPE_MED.value) != 0) {
                        sdkVersion += "\nMED ver.: " + nskAlgoSdk.NskAlgoAlgoVersion(NskAlgoType.NSK_ALGO_TYPE_MED.value);
                    }
                    if ((algoTypes & NskAlgoType.NSK_ALGO_TYPE_BLINK.value) != 0) {
                        sdkVersion += "\nBlink ver.: " + nskAlgoSdk.NskAlgoAlgoVersion(NskAlgoType.NSK_ALGO_TYPE_BLINK.value);
                    }
                    if ((algoTypes & NskAlgoType.NSK_ALGO_TYPE_CR.value) != 0) {
                        sdkVersion += "\nCreativity ver.: " + nskAlgoSdk.NskAlgoAlgoVersion(NskAlgoType.NSK_ALGO_TYPE_CR.value);
                    }
                    if ((algoTypes & NskAlgoType.NSK_ALGO_TYPE_AL.value) != 0) {
                        sdkVersion += "\nAlertness ver.: " + nskAlgoSdk.NskAlgoAlgoVersion(NskAlgoType.NSK_ALGO_TYPE_AL.value);
                    }
                    if ((algoTypes & NskAlgoType.NSK_ALGO_TYPE_CP.value) != 0) {
                        sdkVersion += "\nCognitive Preparedness ver.: " + nskAlgoSdk.NskAlgoAlgoVersion(NskAlgoType.NSK_ALGO_TYPE_CP.value);
                    }
                    if ((algoTypes & NskAlgoType.NSK_ALGO_TYPE_ET.value) != 0) {
                        sdkVersion += "\neTensity ver.: " + nskAlgoSdk.NskAlgoAlgoVersion(NskAlgoType.NSK_ALGO_TYPE_ET.value);
                    }
                    if ((algoTypes & NskAlgoType.NSK_ALGO_TYPE_YY.value) != 0) {
                        sdkVersion += "\nYinYang ver.: " + nskAlgoSdk.NskAlgoAlgoVersion(NskAlgoType.NSK_ALGO_TYPE_YY.value);
                    }
                    if ((algoTypes & NskAlgoType.NSK_ALGO_TYPE_BP.value) != 0) {
                        sdkVersion += "\nEEG Bandpower ver.: " + nskAlgoSdk.NskAlgoAlgoVersion(NskAlgoType.NSK_ALGO_TYPE_BP.value);
                    }
                    showToast(sdkVersion, Toast.LENGTH_LONG);
                }
            }
        });

        bpText.setEnabled(false);
        bpText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeAllSeriesFromPlot();
                setupPlot(-20, 20, "EEG Bandpower");
                addSeries(plot, bp_deltaSeries, R.xml.line_point_formatter_with_plf1);
                addSeries(plot, bp_thetaSeries, R.xml.line_point_formatter_with_plf2);
                addSeries(plot, bp_alphaSeries, R.xml.line_point_formatter_with_plf3);
                addSeries(plot, bp_betaSeries, R.xml.line_point_formatter_with_plf4);
                addSeries(plot, bp_gammaSeries, R.xml.line_point_formatter_with_plf5);
                plot.redraw();

                text.setVisibility(View.INVISIBLE);

                currentSelectedAlgo = NskAlgoType.NSK_ALGO_TYPE_BP;

                intervalSeekBar.setMax(1);
                intervalSeekBar.setProgress(0);
                intervalSeekBar.setEnabled(false);
                intervalText.setText(String.format("%d", 1));
                setIntervalButton.setEnabled(false);
            }
        });

        crText.setEnabled(false);
        crText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeAllSeriesFromPlot();
                setupPlot(-1, 1, "Creativity");
                addSeries(plot, crSeries, R.xml.line_point_formatter_with_plf1);
                plot.redraw();

                text.setVisibility(View.INVISIBLE);

                currentSelectedAlgo = NskAlgoType.NSK_ALGO_TYPE_CR;

                intervalSeekBar.setMax(4);
                intervalSeekBar.setProgress(crInterval - 1);
                intervalSeekBar.setEnabled(true);
                intervalText.setText(String.format("%d", crInterval));
                setIntervalButton.setEnabled(true);
                {
                    Paint paint = plot.getBackgroundPaint();
                    if (crValid == 1) {
                        paint.setColor(Color.rgb(0, 128, 0));
                    } else if (crValid == 0) {
                        paint.setColor(Color.rgb(128, 0, 0));
                    } else {
                        paint.setColor(Color.rgb(0, 0, 0));
                    }
                }
            }
        });

        alText.setEnabled(false);
        alText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeAllSeriesFromPlot();
                setupPlot(-1, 1, "Alertness");
                addSeries(plot, alSeries, R.xml.line_point_formatter_with_plf1);
                plot.redraw();

                text.setVisibility(View.INVISIBLE);

                currentSelectedAlgo = NskAlgoType.NSK_ALGO_TYPE_AL;

                intervalSeekBar.setMax(4);
                intervalSeekBar.setProgress(alInterval - 1);
                intervalSeekBar.setEnabled(true);
                intervalText.setText(String.format("%d", alInterval));
                setIntervalButton.setEnabled(true);
                {
                    Paint paint = plot.getBackgroundPaint();
                    if (alValid == 1) {
                        paint.setColor(Color.rgb(0, 128, 0));
                    } else if (alValid == 0) {
                        paint.setColor(Color.rgb(128, 0, 0));
                    } else {
                        paint.setColor(Color.rgb(0, 0, 0));
                    }
                }
            }
        });

        cpText.setEnabled(false);
        cpText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeAllSeriesFromPlot();
                setupPlot(-1, 1, "Cognitive Preparedness");
                addSeries(plot, cpSeries, R.xml.line_point_formatter_with_plf1);
                plot.redraw();

                text.setVisibility(View.INVISIBLE);

                currentSelectedAlgo = NskAlgoType.NSK_ALGO_TYPE_CP;

                intervalSeekBar.setMax(4);
                intervalSeekBar.setProgress(cpInterval - 1);
                intervalSeekBar.setEnabled(true);
                intervalText.setText(String.format("%d", cpInterval));
                setIntervalButton.setEnabled(true);
                {
                    Paint paint = plot.getBackgroundPaint();
                    if (cpValid == 1) {
                        paint.setColor(Color.rgb(0, 128, 0));
                    } else if (cpValid == 0) {
                        paint.setColor(Color.rgb(128, 0, 0));
                    } else {
                        paint.setColor(Color.rgb(0, 0, 0));
                    }
                }
            }
        });

        etText.setEnabled(false);
        etText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeAllSeriesFromPlot();
                setupPlot(1, 4, "eTensity");
                addSeries(plot, etSeries, R.xml.line_point_formatter_with_plf1);
                plot.redraw();

                text.setVisibility(View.INVISIBLE);

                currentSelectedAlgo = NskAlgoType.NSK_ALGO_TYPE_ET;

                intervalSeekBar.setMax(4);
                intervalSeekBar.setProgress(etInterval - 1);
                intervalSeekBar.setEnabled(true);
                intervalText.setText(String.format("%d", etInterval));
                setIntervalButton.setEnabled(true);
            }
        });

        yyText.setEnabled(false);
        yyText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeAllSeriesFromPlot();
                setupPlot(-1, 1, "YinYang");
                addSeries(plot, yySeries, R.xml.line_point_formatter_with_plf1);
                plot.redraw();

                text.setVisibility(View.INVISIBLE);

                currentSelectedAlgo = NskAlgoType.NSK_ALGO_TYPE_YY;

                intervalSeekBar.setMax(9);
                intervalSeekBar.setProgress(yyInterval - 1);
                intervalSeekBar.setEnabled(true);
                intervalText.setText(String.format("%d", yyInterval));
                setIntervalButton.setEnabled(true);
            }
        });

        apText.setEnabled(false);
        apText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeAllSeriesFromPlot();
                setupPlot(1, 4, "Appreciation");
                addSeries(plot, apSeries, R.xml.line_point_formatter_with_plf1);
                plot.redraw();
                text.setVisibility(View.INVISIBLE);

                currentSelectedAlgo = NskAlgoType.NSK_ALGO_TYPE_AP;
                intervalSeekBar.setMax(4);
                intervalSeekBar.setProgress(apInterval - 1);
                intervalSeekBar.setEnabled(true);
                intervalText.setText(String.format("%d", apInterval));
                setIntervalButton.setEnabled(true);

            }
        });

        meText.setEnabled(false);
        meText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeAllSeriesFromPlot();
                setupPlot(-100, 100, "Mental Effort");
                addSeries(plot, abs_meSeries, R.xml.line_point_formatter_with_plf1);
                addSeries(plot, diff_meSeries, R.xml.line_point_formatter_with_plf2);
                plot.redraw();
                text.setVisibility(View.INVISIBLE);

                currentSelectedAlgo = NskAlgoType.NSK_ALGO_TYPE_ME;
                intervalSeekBar.setMax(4);
                intervalSeekBar.setProgress(meInterval - 1);
                intervalSeekBar.setEnabled(true);
                intervalText.setText(String.format("%d", meInterval));
                setIntervalButton.setEnabled(true);
            }
        });

        me2Text.setEnabled(false);
        me2Text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeAllSeriesFromPlot();
                plot.setVisibility(View.INVISIBLE);
                text.setVisibility(View.VISIBLE);
                //text.setText("");

                currentSelectedAlgo = NskAlgoType.NSK_ALGO_TYPE_ME2;
                intervalSeekBar.setMax(870);
                intervalSeekBar.setProgress(me2Interval - 30);
                intervalSeekBar.setEnabled(true);
                intervalText.setText(String.format("%d", me2Interval));
                setIntervalButton.setEnabled(true);
            }
        });

        fText.setEnabled(false);
        fText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeAllSeriesFromPlot();
                setupPlot(-100, 100, "Familiarity");
                addSeries(plot, abs_fSeries, R.xml.line_point_formatter_with_plf1);
                addSeries(plot, diff_fSeries, R.xml.line_point_formatter_with_plf2);
                plot.redraw();
                text.setVisibility(View.INVISIBLE);

                currentSelectedAlgo = NskAlgoType.NSK_ALGO_TYPE_F;
                intervalSeekBar.setMax(4);
                intervalSeekBar.setProgress(fInterval - 1);
                intervalSeekBar.setEnabled(true);
                intervalText.setText(String.format("%d", fInterval));
                setIntervalButton.setEnabled(true);
            }
        });

        f2Text.setEnabled(false);
        f2Text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeAllSeriesFromPlot();
                plot.setVisibility(View.INVISIBLE);
                text.setVisibility(View.VISIBLE);
                //text.setText("");

                currentSelectedAlgo = NskAlgoType.NSK_ALGO_TYPE_F2;
                intervalSeekBar.setMax(870);
                intervalSeekBar.setProgress(f2Interval - 30);
                intervalSeekBar.setEnabled(true);
                intervalText.setText(String.format("%d", f2Interval));
                setIntervalButton.setEnabled(true);

            }
        });

        intervalSeekBar.setEnabled(false);
        intervalSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (currentSelectedAlgo == NskAlgoType.NSK_ALGO_TYPE_AP) {
                    intervalText.setText(String.format("%d", progress + 1));
                }
                if (currentSelectedAlgo == NskAlgoType.NSK_ALGO_TYPE_ME) {
                    intervalText.setText(String.format("%d", progress + 1));
                }
                if (currentSelectedAlgo == NskAlgoType.NSK_ALGO_TYPE_ME2) {
                    intervalText.setText(String.format("%d", (progress + 30)));
                }
                if (currentSelectedAlgo == NskAlgoType.NSK_ALGO_TYPE_F) {
                    intervalText.setText(String.format("%d", progress + 1));
                }
                if (currentSelectedAlgo == NskAlgoType.NSK_ALGO_TYPE_F2) {
                    intervalText.setText(String.format("%d", (progress + 30)));
                }
                if (currentSelectedAlgo == NskAlgoType.NSK_ALGO_TYPE_CR) {
                    intervalText.setText(String.format("%d", progress + 1));
                }
                if (currentSelectedAlgo == NskAlgoType.NSK_ALGO_TYPE_AL) {
                    intervalText.setText(String.format("%d", progress + 1));
                }
                if (currentSelectedAlgo == NskAlgoType.NSK_ALGO_TYPE_CP) {
                    intervalText.setText(String.format("%d", progress + 1));
                }
                if (currentSelectedAlgo == NskAlgoType.NSK_ALGO_TYPE_ET) {
                    intervalText.setText(String.format("%d", progress + 1));
                }
                if (currentSelectedAlgo == NskAlgoType.NSK_ALGO_TYPE_YY) {
                    intervalText.setText(String.format("%d", progress + 1));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                bLastOutputInterval = seekBar.getProgress();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (currentSelectedAlgo == NskAlgoType.NSK_ALGO_TYPE_AP) {
                    apInterval = seekBar.getProgress() + 1;
                    intervalText.setText(String.format("%d", apInterval));
                }
                if (currentSelectedAlgo == NskAlgoType.NSK_ALGO_TYPE_ME) {
                    meInterval = seekBar.getProgress() + 1;
                    intervalText.setText(String.format("%d", meInterval));
                }
                if (currentSelectedAlgo == NskAlgoType.NSK_ALGO_TYPE_ME2) {
                    me2Interval = (seekBar.getProgress() + 30);
                    intervalText.setText(String.format("%d", me2Interval));
                }
                if (currentSelectedAlgo == NskAlgoType.NSK_ALGO_TYPE_F) {
                    fInterval = seekBar.getProgress() + 1;
                    intervalText.setText(String.format("%d", fInterval));
                }
                if (currentSelectedAlgo == NskAlgoType.NSK_ALGO_TYPE_F2) {
                    f2Interval = (seekBar.getProgress() + 30);
                    intervalText.setText(String.format("%d", f2Interval));
                }
                if (currentSelectedAlgo == NskAlgoType.NSK_ALGO_TYPE_CR) {
                    crInterval = seekBar.getProgress() + 1;
                    intervalText.setText(String.format("%d", crInterval));
                }
                if (currentSelectedAlgo == NskAlgoType.NSK_ALGO_TYPE_AL) {
                    alInterval = seekBar.getProgress() + 1;
                    intervalText.setText(String.format("%d", alInterval));
                }
                if (currentSelectedAlgo == NskAlgoType.NSK_ALGO_TYPE_CP) {
                    cpInterval = seekBar.getProgress() + 1;
                    intervalText.setText(String.format("%d", cpInterval));
                }
                if (currentSelectedAlgo == NskAlgoType.NSK_ALGO_TYPE_ET) {
                    etInterval = seekBar.getProgress() + 1;
                    intervalText.setText(String.format("%d", etInterval));
                }
                if (currentSelectedAlgo == NskAlgoType.NSK_ALGO_TYPE_YY) {
                    yyInterval = seekBar.getProgress() + 1;
                    intervalText.setText(String.format("%d", yyInterval));
                }

            }
        });

        setIntervalButton.setEnabled(false);
        setIntervalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int ret = -1;
                String toastStr = "";
                if (currentSelectedAlgo == NskAlgoType.NSK_ALGO_TYPE_AP) {
                    ret = nskAlgoSdk.NskAlgoSetConfig(currentSelectedAlgo.value, new NskAlgoConfig(apInterval));
                    if (ret == 0) {
                        toastStr = "Output interval of " + currentSelectedAlgo + " set to " + apInterval;
                    } else {
                        toastStr = "Failed to set output interval of " + currentSelectedAlgo + " to " + apInterval;
                    }
                }
                if (currentSelectedAlgo == NskAlgoType.NSK_ALGO_TYPE_ME) {
                    ret = nskAlgoSdk.NskAlgoSetConfig(currentSelectedAlgo.value, new NskAlgoConfig(meInterval));
                    if (ret == 0) {
                        toastStr = "Output interval of " + currentSelectedAlgo + " set to " + meInterval;
                    } else {
                        toastStr = "Failed to set output interval of " + currentSelectedAlgo + " to " + meInterval;
                    }
                }
                if (currentSelectedAlgo == NskAlgoType.NSK_ALGO_TYPE_ME2) {
                    ret = nskAlgoSdk.NskAlgoSetConfig(currentSelectedAlgo.value, new NskAlgoConfig(me2Interval));
                    if (ret == 0) {
                        toastStr = "Output interval of " + currentSelectedAlgo + " set to " + me2Interval;
                    } else {
                        toastStr = "Failed to set output interval of " + currentSelectedAlgo + " to " + me2Interval;
                    }
                }
                if (currentSelectedAlgo == NskAlgoType.NSK_ALGO_TYPE_F) {
                    ret = nskAlgoSdk.NskAlgoSetConfig(currentSelectedAlgo.value, new NskAlgoConfig(fInterval));
                    if (ret == 0) {
                        toastStr = "Output interval of " + currentSelectedAlgo + " set to " + fInterval;
                    } else {
                        toastStr = "Failed to set output interval of " + currentSelectedAlgo + " to " + fInterval;
                    }
                }
                if (currentSelectedAlgo == NskAlgoType.NSK_ALGO_TYPE_F2) {
                    ret = nskAlgoSdk.NskAlgoSetConfig(currentSelectedAlgo.value, new NskAlgoConfig(f2Interval));
                    if (ret == 0) {
                        toastStr = "Output interval of " + currentSelectedAlgo + " set to " + f2Interval;
                    } else {
                        toastStr = "Failed to set output interval of " + currentSelectedAlgo + " to " + me2Interval;
                    }
                }
                if (currentSelectedAlgo == NskAlgoType.NSK_ALGO_TYPE_CR) {
                    ret = nskAlgoSdk.NskAlgoSetConfig(currentSelectedAlgo.value, new NskAlgoConfig(crInterval, crThreshold, crWindow));
                    if (ret == 0) {
                        toastStr = currentSelectedAlgo.toString() + " set to Interval[" + crInterval + "] Threshold[" + crThreshold.toString() + "]" + " Window[" + crWindow + "]";
                    } else {
                        toastStr = "FAILED: " + currentSelectedAlgo.toString() + " set to Interval[" + crInterval + "] Threshold[" + crThreshold.toString() + "]" + " Window[" + crWindow + "]";
                    }
                }
                if (currentSelectedAlgo == NskAlgoType.NSK_ALGO_TYPE_AL) {
                    ret = nskAlgoSdk.NskAlgoSetConfig(currentSelectedAlgo.value, new NskAlgoConfig(alInterval, alThreshold, alWindow));
                    if (ret == 0) {
                        toastStr = currentSelectedAlgo.toString() + " set to Interval[" + alInterval + "] Threshold[" + alThreshold.toString() + "]" + " Window[" + alWindow + "]";
                    } else {
                        toastStr = "FAILED: " + currentSelectedAlgo.toString() + " set to Interval[" + alInterval + "] Threshold[" + alThreshold.toString() + "]" + " Window[" + alWindow + "]";
                    }
                }
                if (currentSelectedAlgo == NskAlgoType.NSK_ALGO_TYPE_CP) {
                    ret = nskAlgoSdk.NskAlgoSetConfig(currentSelectedAlgo.value, new NskAlgoConfig(cpInterval, cpThreshold, cpWindow));
                    if (ret == 0) {
                        toastStr = currentSelectedAlgo.toString() + " set to Interval[" + cpInterval + "] Threshold[" + cpThreshold.toString() + "]" + " Window[" + cpWindow + "]";
                    } else {
                        toastStr = "FAILED: " + currentSelectedAlgo.toString() + " set to Interval[" + cpInterval + "] Threshold[" + cpThreshold.toString() + "]" + " Window[" + cpWindow + "]";
                    }
                }
                if (currentSelectedAlgo == NskAlgoType.NSK_ALGO_TYPE_ET) {
                    ret = nskAlgoSdk.NskAlgoSetConfig(currentSelectedAlgo.value, new NskAlgoConfig(etInterval));
                    if (ret == 0) {
                        toastStr = "Output interval of " + currentSelectedAlgo + " set to " + etInterval;
                    } else {
                        toastStr = "Failed to set output interval of " + currentSelectedAlgo + " to " + etInterval;
                    }
                }
                if (currentSelectedAlgo == NskAlgoType.NSK_ALGO_TYPE_YY) {
                    ret = nskAlgoSdk.NskAlgoSetConfig(currentSelectedAlgo.value, new NskAlgoConfig(yyInterval));
                    if (ret == 0) {
                        toastStr = "Output interval of " + currentSelectedAlgo + " set to " + yyInterval;
                    } else {
                        toastStr = "Failed to set output interval of " + currentSelectedAlgo + " to " + yyInterval;
                    }
                }

                if (ret == 0) {
                    showToast(toastStr + ": success", Toast.LENGTH_SHORT);
                } else {
                    showToast(toastStr + ": fail", Toast.LENGTH_SHORT);
                }
            }
        });

        nskAlgoSdk.setOnSignalQualityListener(new NskAlgoSdk.OnSignalQualityListener() {
            @Override
            public void onSignalQuality(int level) {
                //Log.d(TAG, "NskAlgoSignalQualityListener: level: " + level);
                final int fLevel = level;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // change UI elements here
                        String sqStr = NskAlgoSignalQuality.values()[fLevel].toString();
                        sqText.setText(sqStr);
                    }
                });
            }
        });

        nskAlgoSdk.setOnStateChangeListener(new NskAlgoSdk.OnStateChangeListener() {
            @Override
            public void onStateChange(int state, int reason) {
                String stateStr = "";
                String reasonStr = "";
                for (NskAlgoState s : NskAlgoState.values()) {
                    if (s.value == state) {
                        stateStr = s.toString();
                    }
                }
                for (NskAlgoState r : NskAlgoState.values()) {
                    if (r.value == reason) {
                        reasonStr = r.toString();
                    }
                }
                Log.d(TAG, "NskAlgoSdkStateChangeListener: state: " + stateStr + ", reason: " + reasonStr);
                final String finalStateStr = stateStr + " | " + reasonStr;
                final int finalState = state;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // change UI elements here
                        stateText.setText(finalStateStr);

                        if (finalState == NskAlgoState.NSK_ALGO_STATE_RUNNING.value || finalState == NskAlgoState.NSK_ALGO_STATE_COLLECTING_BASELINE_DATA.value) {
                            bRunning = true;
                            startButton.setText("Pause");
                            startButton.setEnabled(true);
                            stopButton.setEnabled(true);
                        } else if (finalState == NskAlgoState.NSK_ALGO_STATE_STOP.value) {
                            bRunning = false;
                            raw_data = null;
                            raw_data_index = 0;
                            startButton.setText("Start");
                            startButton.setEnabled(true);
                            stopButton.setEnabled(false);

                            headsetButton.setEnabled(true);
                            cannedButton.setEnabled(true);

                            if (tgStreamReader != null && tgStreamReader.isBTConnected()) {

                                // Prepare for connecting
                                tgStreamReader.stop();
                                tgStreamReader.close();
                            }

                            output_data_count = 0;
                            output_data = null;
                            me_index = 0;
                            ap_index = 0;

                            System.gc();
                        } else if (finalState == NskAlgoState.NSK_ALGO_STATE_PAUSE.value) {
                            bRunning = false;
                            startButton.setText("Start");
                            startButton.setEnabled(true);
                            stopButton.setEnabled(true);
                        } else if (finalState == NskAlgoState.NSK_ALGO_STATE_ANALYSING_BULK_DATA.value) {
                            bRunning = true;
                            startButton.setText("Start");
                            startButton.setEnabled(false);
                            stopButton.setEnabled(true);
                        } else if (finalState == NskAlgoState.NSK_ALGO_STATE_INITED.value || finalState == NskAlgoState.NSK_ALGO_STATE_UNINTIED.value) {
                            bRunning = false;
                            startButton.setText("Start");
                            startButton.setEnabled(true);
                            stopButton.setEnabled(false);
                        }
                    }
                });
            }
        });

        nskAlgoSdk.setOnSignalQualityListener(new NskAlgoSdk.OnSignalQualityListener() {
            @Override
            public void onSignalQuality(final int level) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // change UI elements here
                        String sqStr = NskAlgoSignalQuality.values()[level].toString();
                        sqText.setText(sqStr);
                    }
                });
            }
        });
        nskAlgoSdk.setOnCRAlgoIndexListener(new NskAlgoSdk.OnCRAlgoIndexListener() {
            @Override
            public void onCRAlgoIndex(NskAlgoBCQType type, float value, boolean bValid) {
                boolean bAddToPlot = false;
                if (type == NskAlgoBCQType.NSK_ALGO_BCQ_TYPE_VALUE) {
                    Log.d(TAG, "NskAlgoCRAlgoIndexListener: CR: " + "[" + crCount + "]" + value);
                    crCount++;
                    bAddToPlot = true;
                } else if (type == NskAlgoBCQType.NSK_ALGO_BCQ_TYPE_VALID) {
                    crValid = bValid ? 1 : 0;
                    Log.d(TAG, "NskAlgoCRAlgoIndexListener: CR: " + "[" + crCount + "]" + (bValid ? "[VALID]" : "[INVALID]"));
                } else if (type == NskAlgoBCQType.NSK_ALGO_BCQ_TYPE_BOTH) {
                    crValid = bValid ? 1 : 0;
                    Log.d(TAG, "NskAlgoCRAlgoIndexListener: CR: " + "[" + crCount + "]" + value + (bValid ? "[VALID]" : "[INVALID]"));
                    crCount++;
                    bAddToPlot = true;
                }
                if (currentSelectedAlgo == NskAlgoType.NSK_ALGO_TYPE_CR) {
                    Paint paint = plot.getBackgroundPaint();
                    if (crValid == 1) {
                        paint.setColor(Color.rgb(0, 128, 0));
                    } else if (crValid == 0) {
                        paint.setColor(Color.rgb(128, 0, 0));
                    } else {
                        paint.setColor(Color.rgb(0, 0, 0));
                    }
                }

                if (bAddToPlot) {
                    final String crStr = "[" + value + "]";
                    final String finalCRStr = crStr;
                    final float fValue = value;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // change UI elements here
                            AddValueToPlot(crSeries, fValue);
                        }
                    });
                }
            }
        });
        nskAlgoSdk.setOnALAlgoIndexListener(new NskAlgoSdk.OnALAlgoIndexListener() {
            @Override
            public void onALAlgoIndex(NskAlgoBCQType type, float value, boolean bValid) {
                boolean bAddToPlot = false;
                if (type == NskAlgoBCQType.NSK_ALGO_BCQ_TYPE_VALUE) {
                    Log.d(TAG, "NskAlgoALAlgoIndexListener: AL: " + "[" + alCount + "]" + value);
                    alCount++;
                    bAddToPlot = true;
                } else if (type == NskAlgoBCQType.NSK_ALGO_BCQ_TYPE_VALID) {
                    alValid = bValid ? 1 : 0;
                    Log.d(TAG, "NskAlgoALAlgoIndexListener: AL: " + "[" + alCount + "]" + (bValid ? "[VALID]" : "[INVALID]"));
                } else if (type == NskAlgoBCQType.NSK_ALGO_BCQ_TYPE_BOTH) {
                    alValid = bValid ? 1 : 0;
                    Log.d(TAG, "NskAlgoALAlgoIndexListener: AL: " + "[" + alCount + "]" + value + (bValid ? "[VALID]" : "[INVALID]"));
                    alCount++;
                    bAddToPlot = true;
                }
                if (currentSelectedAlgo == NskAlgoType.NSK_ALGO_TYPE_AL) {
                    Paint paint = plot.getBackgroundPaint();
                    if (alValid == 1) {
                        paint.setColor(Color.rgb(0, 128, 0));
                    } else if (alValid == 0) {
                        paint.setColor(Color.rgb(128, 0, 0));
                    } else {
                        paint.setColor(Color.rgb(0, 0, 0));
                    }
                }
                if (bAddToPlot) {
                    final String alStr = "[" + value + "]";
                    final String finalCRStr = alStr;
                    final float fValue = value;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // change UI elements here
                            AddValueToPlot(alSeries, fValue);
                        }
                    });
                }
            }
        });
        nskAlgoSdk.setOnCPAlgoIndexListener(new NskAlgoSdk.OnCPAlgoIndexListener() {
            @Override
            public void onCPAlgoIndex(NskAlgoBCQType type, float value, boolean bValid) {
                boolean bAddToPlot = false;
                if (type == NskAlgoBCQType.NSK_ALGO_BCQ_TYPE_VALUE) {
                    Log.d(TAG, "NskAlgoCPAlgoIndexListener: CP: " + "[" + cpCount + "]" + value);
                    cpCount++;
                    bAddToPlot = true;
                } else if (type == NskAlgoBCQType.NSK_ALGO_BCQ_TYPE_VALID) {
                    cpValid = bValid ? 1 : 0;
                    Log.d(TAG, "NskAlgoCPAlgoIndexListener: CP: " + "[" + cpCount + "]" + (bValid ? "[VALID]" : "[INVALID]"));
                } else if (type == NskAlgoBCQType.NSK_ALGO_BCQ_TYPE_BOTH) {
                    cpValid = bValid ? 1 : 0;
                    Log.d(TAG, "NskAlgoCPAlgoIndexListener: CP: " + "[" + cpCount + "]" + value + (bValid ? "[VALID]" : "[INVALID]"));
                    cpCount++;
                    bAddToPlot = true;
                }
                if (currentSelectedAlgo == NskAlgoType.NSK_ALGO_TYPE_CP) {
                    Paint paint = plot.getBackgroundPaint();
                    if (cpValid == 1) {
                        paint.setColor(Color.rgb(0, 128, 0));
                    } else if (cpValid == 0) {
                        paint.setColor(Color.rgb(128, 0, 0));
                    } else {
                        paint.setColor(Color.rgb(0, 0, 0));
                    }
                }
                if (bAddToPlot) {
                    final String cpStr = "[" + value + "]";
                    final String finalCPStr = cpStr;
                    final float fValue = value;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // change UI elements here
                            AddValueToPlot(cpSeries, fValue);
                        }
                    });
                }
            }
        });
        nskAlgoSdk.setOnETAlgoIndexListener(new NskAlgoSdk.OnETAlgoIndexListener() {
            @Override
            public void onETAlgoIndex(float value) {
                Log.d(TAG, "NskAlgoETAlgoIndexListener: ET: " + value);
                final String etStr = "[" + value + "]";
                final String finalETStr = etStr;
                final float fValue = value;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // change UI elements here
                        AddValueToPlot(etSeries, fValue);
                    }
                });
            }
        });
        nskAlgoSdk.setOnYYAlgoIndexListener(new NskAlgoSdk.OnYYAlgoIndexListener() {
            @Override
            public void onYYAlgoIndex(float value) {
                Log.d(TAG, "NskAlgoYYAlgoIndexListener: YY: " + value);
                final String yyStr = "[" + value + "]";
                final String finalYYStr = yyStr;
                final float fValue = value;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // change UI elements here
                        AddValueToPlot(yySeries, fValue);
                    }
                });
            }
        });

        nskAlgoSdk.setOnAPAlgoIndexListener(new NskAlgoSdk.OnAPAlgoIndexListener() {
            @Override
            public void onAPAlgoIndex(float value) {
                Log.d(TAG, "NskAlgoAPAlgoIndexListener: AP: " + value);
                final String apStr = "[" + value + "]";
                final String finalAPStr = apStr;
                final float fValue = value;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // change UI elements here
                        AddValueToPlot(apSeries, fValue);
                    }
                });
            }
        });

        nskAlgoSdk.setOnBPAlgoIndexListener(new NskAlgoSdk.OnBPAlgoIndexListener() {
            @Override
            public void onBPAlgoIndex(float delta, float theta, float alpha, float beta, float gamma) {
                Log.d(TAG, "NskAlgoBPAlgoIndexListener: BP: D[" + delta + " dB] T[" + theta + " dB] A[" + alpha + " dB] B[" + beta + " dB] G[" + gamma + "]");

                final float fDelta = delta, fTheta = theta, fAlpha = alpha, fBeta = beta, fGamma = gamma;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // change UI elements here
                        AddValueToPlot(bp_deltaSeries, fDelta);
                        AddValueToPlot(bp_thetaSeries, fTheta);
                        AddValueToPlot(bp_alphaSeries, fAlpha);
                        AddValueToPlot(bp_betaSeries, fBeta);
                        AddValueToPlot(bp_gammaSeries, fGamma);
                    }
                });
            }
        });

        nskAlgoSdk.setOnMEAlgoIndexListener(new NskAlgoSdk.OnMEAlgoIndexListener() {
            @Override
            public void onMEAlgoIndex(final float abs_me, final float diff_me, float max_me, float min_me) {
                Log.d(TAG, "NskAlgoMEAlgoIndexListener: ME: abs:" + abs_me + ", diff:" + diff_me + "[" + min_me + ":" + max_me + "]");

                String meStr = "[abs:" + abs_me + "][diff: " + diff_me + "]";
                final String finalMEStr = meStr;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // change UI elements here
                        AddValueToPlot(abs_meSeries, abs_me);
                        AddValueToPlot(diff_meSeries, diff_me);
                    }
                });

                /*if (output_data_count > 0 && me_index < output_data_count) {
                    boolean bNotMatch = false;
                    if ((int) (abs_me * 1000) != (int) (output_data[me_index] * 1000)) {
                        Log.d(TAG, "Not match me[" + me_index + "][" + abs_me + "] != output[" + me_index + "][" + output_data[me_index] + "]");
                        bNotMatch = true;
                    }
                }*/
                me_index++;
            }
        });

        nskAlgoSdk.setOnME2AlgoIndexListener(new NskAlgoSdk.OnME2AlgoIndexListener() {
            @Override
            public void onME2AlgoIndex(float total_me, float me_rate, float changing_rate) {
                Log.d(TAG, "NskAlgoME2AlgoIndexListener: ME2: total:" + total_me + ", rate:" + me_rate + ", chg rate:" + changing_rate);
                String me2Str2 = "\n    Total ME: " + total_me + "\n" +
                        "    ME Rate: " + me_rate + "\n" +
                        "    Changing Rate: " + changing_rate;
                final String finalMe2Str2 = me2Str2;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // change UI elements here
                        String finalStr = Datetime() + ": " + finalMe2Str2 + "\n\n" + String.valueOf(text.getText());
                        text.setText(finalStr);
                    }
                });
            }
        });

        nskAlgoSdk.setOnFAlgoIndexListener(new NskAlgoSdk.OnFAlgoIndexListener() {
            @Override
            public void onFAlgoIndex(final float abs_f, final float diff_f, float max_f, float min_f) {
                Log.d(TAG, "NskAlgoFAlgoIndexListener: F: abs:" + abs_f + ", diff:" + diff_f + "[" + min_f + ":" + max_f + "]");

                String fStr = "[abs:" + abs_f + "][diff: " + diff_f + "]";
                final String finalFStr = fStr;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // change UI elements here
                        AddValueToPlot(abs_fSeries, abs_f);
                        AddValueToPlot(diff_fSeries, diff_f);
                    }
                });

                if (output_data_count > 0 && f_index < output_data_count) {
                    boolean bNotMatch = false;
                    if ((int) (abs_f * 1000) != (int) (output_data[f_index] * 1000)) {
                        Log.d(TAG, "Not match f[" + f_index + "][" + abs_f + "] != output[" + f_index + "][" + output_data[f_index] + "]");
                        bNotMatch = true;
                    }
                }
                f_index++;
            }
        });

        nskAlgoSdk.setOnF2AlgoIndexListener(new NskAlgoSdk.OnF2AlgoIndexListener() {
            @Override
            public void onF2AlgoIndex(final int progress_level, final float f_degree) {
                Log.d(TAG, "NskAlgoF2AlgoIndexListener: F2: Level: " + progress_level + " Degree: " + f_degree);
                final String f2Str = "\n    Progress Level: " + progress_level + "\n" +
                        "    F Degree      : " + f_degree;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // change UI elements here
                        String finalStr = Datetime() + ": " + f2Str + "\n\n" + String.valueOf(text.getText());
                        text.setText(finalStr);
                    }
                });
            }
        });

        nskAlgoSdk.setOnAttAlgoIndexListener(new NskAlgoSdk.OnAttAlgoIndexListener() {
            @Override
            public void onAttAlgoIndex(int value) {
                Log.d(TAG, "NskAlgoAttAlgoIndexListener: Attention:" + value);
                String attStr = "[" + value + "]";
                final String finalAttStr = attStr;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // change UI elements here
                        attValue.setText(finalAttStr);
                    }
                });
            }
        });

        nskAlgoSdk.setOnMedAlgoIndexListener(new NskAlgoSdk.OnMedAlgoIndexListener() {
            @Override
            public void onMedAlgoIndex(int value) {
                Log.d(TAG, "NskAlgoMedAlgoIndexListener: Meditation:" + value);
                String medStr = "[" + value + "]";
                final String finalMedStr = medStr;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // change UI elements here
                        medValue.setText(finalMedStr);
                    }
                });
            }
        });

        nskAlgoSdk.setOnEyeBlinkDetectionListener(new NskAlgoSdk.OnEyeBlinkDetectionListener() {
            @Override
            public void onEyeBlinkDetect(int strength) {
                Log.d(TAG, "NskAlgoEyeBlinkDetectionListener: Eye blink detected: " + strength);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        blinkImage.setImageResource(R.mipmap.led_on);
                        Timer timer = new Timer();

                        timer.schedule(new TimerTask() {
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        blinkImage.setImageResource(R.mipmap.led_off);
                                    }
                                });
                            }
                        }, 500);
                    }
                });
            }
        });

        // initialize our XYPlot reference:
        plot = (XYPlot) findViewById(R.id.myPlot);
        plot.setVisibility(View.INVISIBLE);
        text = (EditText) findViewById(R.id.myText);
        text.setVisibility(View.INVISIBLE);
    }

    private void removeAllSeriesFromPlot () {
        if (apSeries != null) {
            plot.removeSeries(apSeries);
        }
        if (abs_meSeries != null) {
            plot.removeSeries(abs_meSeries);
        }
        if (diff_meSeries != null) {
            plot.removeSeries(diff_meSeries);
        }
        if (abs_fSeries != null) {
            plot.removeSeries(abs_fSeries);
        }
        if (diff_fSeries != null) {
            plot.removeSeries(diff_fSeries);
        }
        if (crSeries != null) {
            plot.removeSeries(crSeries);
        }
        if (alSeries != null) {
            plot.removeSeries(alSeries);
        }
        if (cpSeries != null) {
            plot.removeSeries(cpSeries);
        }
        if (etSeries != null) {
            plot.removeSeries(etSeries);
        }
        if (yySeries != null) {
            plot.removeSeries(yySeries);
        }
        if (bp_deltaSeries != null) {
            plot.removeSeries(bp_deltaSeries);
        }
        if (bp_thetaSeries != null) {
            plot.removeSeries(bp_thetaSeries);
        }
        if (bp_alphaSeries != null) {
            plot.removeSeries(bp_alphaSeries);
        }
        if (bp_gammaSeries != null) {
            plot.removeSeries(bp_gammaSeries);
        }
        if (bp_betaSeries != null) {
            plot.removeSeries(bp_betaSeries);
        }
        System.gc();
    }

    private void clearAllSeries () {
        if (apSeries != null) {
            plot.removeSeries(apSeries);
            apSeries = null;
        }
        if (abs_meSeries != null) {
            plot.removeSeries(abs_meSeries);
            abs_meSeries = null;
        }
        if (diff_meSeries != null) {
            plot.removeSeries(diff_meSeries);
            diff_meSeries = null;
        }
        if (abs_fSeries != null) {
            plot.removeSeries(abs_fSeries);
            abs_fSeries = null;
        }
        if (diff_fSeries != null) {
            plot.removeSeries(diff_fSeries);
            diff_fSeries = null;
        }
        if (crSeries != null) {
            plot.removeSeries(crSeries);
            crSeries = null;
        }
        if (alSeries != null) {
            plot.removeSeries(alSeries);
            alSeries = null;
        }
        if (cpSeries != null) {
            plot.removeSeries(cpSeries);
            cpSeries = null;
        }
        if (etSeries != null) {
            plot.removeSeries(etSeries);
            etSeries = null;
        }
        if (yySeries != null) {
            plot.removeSeries(yySeries);
            yySeries = null;
        }
        if (bp_deltaSeries != null) {
            plot.removeSeries(bp_deltaSeries);
            bp_deltaSeries = null;
        }
        if (bp_thetaSeries != null) {
            plot.removeSeries(bp_thetaSeries);
            bp_thetaSeries = null;
        }
        if (bp_alphaSeries != null) {
            plot.removeSeries(bp_alphaSeries);
            bp_alphaSeries = null;
        }
        if (bp_gammaSeries != null) {
            plot.removeSeries(bp_gammaSeries);
            bp_gammaSeries = null;
        }
        if (bp_betaSeries != null) {
            plot.removeSeries(bp_betaSeries);
            bp_betaSeries = null;
        }
        plot.setVisibility(View.INVISIBLE);
        System.gc();
    }

    private XYPlot setupPlot (Number rangeMin, Number rangeMax, String title) {
        // initialize our XYPlot reference:
        plot = (XYPlot) findViewById(R.id.myPlot);

        plot.setDomainLeftMax(0);
        plot.setDomainRightMin(X_RANGE);
        plot.setDomainRightMax(X_RANGE);

        if ((rangeMax.intValue() - rangeMin.intValue()) < 10) {
            plot.setRangeStepValue((rangeMax.intValue() - rangeMin.intValue() + 1));
        } else {
            plot.setRangeStepValue(11);
        }
        plot.setRangeBoundaries(rangeMin.intValue(), rangeMax.intValue(), BoundaryMode.FIXED);

        plot.getGraphWidget().getGridBackgroundPaint().setColor(Color.WHITE);

        plot.setTicksPerDomainLabel(10);
        plot.getGraphWidget().setDomainLabelOrientation(-45);

        plot.setPlotPadding(0, 0, 0, 0);
        plot.setTitle(title);

        plot.setVisibility(View.VISIBLE);

        return plot;
    }

    private SimpleXYSeries createSeries (String seriesName) {
        // Turn the above arrays into XYSeries':
        SimpleXYSeries series = new SimpleXYSeries(
                null,          // SimpleXYSeries takes a List so turn our array into a List
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, // Y_VALS_ONLY means use the element index as the x value
                seriesName);                             // Set the display title of the series

        series.useImplicitXVals();

        return series;
    }

    private SimpleXYSeries addSeries (XYPlot plot, SimpleXYSeries series, int formatterId) {

        // Create a formatter to use for drawing a series using LineAndPointRenderer
        // and configure it from xml:
        LineAndPointFormatter seriesFormat = new LineAndPointFormatter();
        seriesFormat.setPointLabelFormatter(null);
        seriesFormat.configure(getApplicationContext(), formatterId);
        seriesFormat.setVertexPaint(null);
        series.useImplicitXVals();

        // add a new series' to the xyplot:
        plot.addSeries(series, seriesFormat);

        return series;
    }

    private int gcCount = 0;
    private void AddValueToPlot (SimpleXYSeries series, float value) {
        if (series.size() >= X_RANGE) {
            series.removeFirst();
        }
        Number num = value;
        series.addLast(null, num);
        plot.redraw();
        gcCount++;
        if (gcCount >= 20) {
            System.gc();
            gcCount = 0;
        }
    }

    private short [] readData(InputStream is, int size) {
        short data[] = new short[size];
        int lineCount = 0;
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        try {
            while (lineCount < size) {
                String line = reader.readLine();
                if (line == null || line.isEmpty()) {
                    Log.d(TAG, "lineCount=" + lineCount);
                    break;
                }
                data[lineCount] = Short.parseShort(line);
                lineCount++;
            }
            Log.d(TAG, "lineCount=" + lineCount);
        } catch (IOException e) {

        }
        return data;
    }

    @Override
    public void onBackPressed() {
        nskAlgoSdk.NskAlgoUninit();
        finish();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    public static String Datetime()
    {
        Calendar c = Calendar.getInstance();

        String sDate = "[" + c.get(Calendar.YEAR) + "/"
                + (c.get(Calendar.MONTH)+1)
                + "/" + c.get(Calendar.DAY_OF_MONTH)
                + " " + c.get(Calendar.HOUR_OF_DAY)
                + ":" + String.format("%02d", c.get(Calendar.MINUTE))
                + ":" + String.format("%02d", c.get(Calendar.SECOND)) + "]";
        return sDate;
    }
    
    private TgStreamHandler callback = new TgStreamHandler() {

        @Override
        public void onStatesChanged(int connectionStates) {
            // TODO Auto-generated method stub
            Log.d(TAG, "connectionStates change to: " + connectionStates);
            switch (connectionStates) {
                case ConnectionStates.STATE_CONNECTING:
                    // Do something when connecting
                    break;
                case ConnectionStates.STATE_CONNECTED:
                    // Do something when connected
                    tgStreamReader.start();
                    showToast("Connected", Toast.LENGTH_SHORT);
                    break;
                case ConnectionStates.STATE_WORKING:
                    // Do something when working

                    //(9) demo of recording raw data , stop() will call stopRecordRawData,
                    //or you can add a button to control it.
                    //You can change the save path by calling setRecordStreamFilePath(String filePath) before startRecordRawData
                    //tgStreamReader.startRecordRawData();

                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Button startButton = (Button) findViewById(R.id.startButton);
                            startButton.setEnabled(true);
                        }

                    });

                    break;
                case ConnectionStates.STATE_GET_DATA_TIME_OUT:
                    // Do something when getting data timeout

                    //(9) demo of recording raw data, exception handling
                    //tgStreamReader.stopRecordRawData();

                    showToast("Get data time out!", Toast.LENGTH_SHORT);

                    if (tgStreamReader != null && tgStreamReader.isBTConnected()) {
                        tgStreamReader.stop();
                        tgStreamReader.close();
                    }

                    break;
                case ConnectionStates.STATE_STOPPED:
                    // Do something when stopped
                    // We have to call tgStreamReader.stop() and tgStreamReader.close() much more than
                    // tgStreamReader.connectAndstart(), because we have to prepare for that.

                    break;
                case ConnectionStates.STATE_DISCONNECTED:
                    // Do something when disconnected
                    break;
                case ConnectionStates.STATE_ERROR:
                    // Do something when you get error message
                    break;
                case ConnectionStates.STATE_FAILED:
                    // Do something when you get failed message
                    // It always happens when open the BluetoothSocket error or timeout
                    // Maybe the device is not working normal.
                    // Maybe you have to try again
                    break;
            }
        }

        @Override
        public void onRecordFail(int flag) {
            // You can handle the record error message here
            Log.e(TAG,"onRecordFail: " +flag);

        }

        @Override
        public void onChecksumFail(byte[] payload, int length, int checksum) {
            // You can handle the bad packets here.
        }

        @Override
        public void onDataReceived(int datatype, int data, Object obj) {
            // You can handle the received data here
            // You can feed the raw data to algo sdk here if necessary.
            //Log.i(TAG,"onDataReceived");
            switch (datatype) {
                case MindDataType.CODE_ATTENTION:
                    short attValue[] = {(short)data};
                    nskAlgoSdk.NskAlgoDataStream(NskAlgoDataType.NSK_ALGO_DATA_TYPE_ATT.value, attValue, 1);
                    break;
                case MindDataType.CODE_MEDITATION:
                    short medValue[] = {(short)data};
                    nskAlgoSdk.NskAlgoDataStream(NskAlgoDataType.NSK_ALGO_DATA_TYPE_MED.value, medValue, 1);
                    break;
                case MindDataType.CODE_POOR_SIGNAL:
                    short pqValue[] = {(short)data};
                    nskAlgoSdk.NskAlgoDataStream(NskAlgoDataType.NSK_ALGO_DATA_TYPE_PQ.value, pqValue, 1);
                    break;
                case MindDataType.CODE_RAW:
                    raw_data[raw_data_index++] = (short)data;
                    if (raw_data_index == 512) {
                        nskAlgoSdk.NskAlgoDataStream(NskAlgoDataType.NSK_ALGO_DATA_TYPE_EEG.value, raw_data, raw_data_index);
                        raw_data_index = 0;
                    }
                    break;
                default:
                    break;
            }
        }

    };

    public void showToast(final String msg, final int timeStyle) {
        MainActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), msg, timeStyle).show();
            }

        });
    }

    private void showDialog (String message) {
        new AlertDialog.Builder(this)
                .setTitle("")
                .setMessage(message)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}
