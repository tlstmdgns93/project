package org.androidtown.sleepdrive_cognition;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;


public class VoiceRecord extends AppCompatActivity  implements View.OnClickListener,OnCompletionListener, AdapterView.OnItemSelectedListener {

    ArrayList<String> path_list;
    private static final int REC_STOP = 0;
    private static final int RECORDING = 1;
    private static final int PLAY_STOP = 0;
    private static final int PLAYING = 1;
    private static final int PLAY_PAUSE = 2;
    private File[] files;
    private Spinner sp;

    private MediaRecorder mRecorder = null;
    private MediaPlayer mPlayer = null;
    private int mRecState = REC_STOP;
    private int mPlayerState = PLAY_STOP;
    private SeekBar mRecProgressBar, mPlayProgressBar;
    private Button mBtnStartRec, mBtnStartPlay, mBtnDelRec, mBtnSetVoice,mBtnHelp;
    private String mFilePath, mFileName;
    private TextView mtvPlayStartPoint,mTvPlayMaxPoint;
    private TextView mtvRecStartPoint;
    private int mCurRecTimeMs = 0;
    private int mCurProgressTimeDisplay = 0;
    private int rec_start_time, play_start_time=0;



    //녹음시 현재 시간표시 ex)00:00 -> 00:01 1초씩 증가
    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if(rec_start_time<10)
                mtvRecStartPoint.setText("00:0"+Integer.toString(rec_start_time));
            else
                mtvRecStartPoint.setText("00:"+Integer.toString(rec_start_time));

            rec_start_time++;
            // 메세지를 처리하고 또다시 핸들러에 메세지 전달 (1000ms 지연)
            mHandler.sendEmptyMessageDelayed(0,1000);
        }
    };

    //재생시 현재 시간표시
    Handler mHandler2 = new Handler() {
        public void handleMessage(Message msg) {

            if(play_start_time<10)
                mtvPlayStartPoint.setText("00:0"+Integer.toString(play_start_time));
            else
                mtvPlayStartPoint.setText("00:"+Integer.toString(play_start_time));

            play_start_time++;
            // 메세지를 처리하고 또다시 핸들러에 메세지 전달 (1000ms 지연)
            mHandler2.sendEmptyMessageDelayed(0,1000);
        }
    };




    // 녹음시 SeekBar처리
    @SuppressLint("HandlerLeak")
    Handler mProgressHandler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            mCurRecTimeMs = mCurRecTimeMs + 100;
            mCurProgressTimeDisplay = mCurProgressTimeDisplay + 100;

            // 녹음시간이 음수이면 정지버튼을 눌러 정지시켰음을 의미하므로
            // SeekBar는 그대로 정지시키고 레코더를 정지시킨다.
            if (mCurRecTimeMs < 0)
            {}
            // 녹음시간이 아직 최대녹음제한시간보다 작으면 녹음중이라는 의미이므로
            // SeekBar의 위치를 옮겨주고 0.1초 후에 다시 체크하도록 한다.
            else if (mCurRecTimeMs < 60000)
            {
                mRecProgressBar.setProgress(mCurProgressTimeDisplay);
                mProgressHandler.sendEmptyMessageDelayed(0, 100);
            }
            // 녹음시간이 최대 녹음제한 시간보다 크면 녹음을 정지 시킨다.
            else
            {
                mBtnStartRecOnClick();
            }
        }
    };

    // 재생시 SeekBar 처리
    @SuppressLint("HandlerLeak")
    Handler mProgressHandler2 = new Handler()
    {
        public void handleMessage(Message msg)
        {
            if (mPlayer == null) return;

            try
            {
                if (mPlayer.isPlaying())
                {
                    mPlayProgressBar.setProgress(mPlayer.getCurrentPosition());
                    mProgressHandler2.sendEmptyMessageDelayed(0, 100);
                }
                else
                {
                    mPlayer.release();
                    mPlayer = null;

                    updateUI();
                }
            }
            catch (IllegalStateException e)
            {}
            catch (Exception e)
            {}
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_record);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // SD카드에 디렉토리를 만든다.
        mFilePath = SunUtil.makeDir("progress_recorder");
        Log.d("S1",mFilePath);
        mBtnStartRec = (Button) findViewById(R.id.btnStartRec);
        mBtnStartPlay = (Button) findViewById(R.id.btnStartPlay);
        mBtnDelRec = (Button) findViewById(R.id.btnDelRec);
        mBtnSetVoice = (Button) findViewById(R.id.btnSetVoice);
        mBtnHelp = (Button) findViewById(R.id.btnHelp);
        mRecProgressBar = (SeekBar) findViewById(R.id.recProgressBar);
        mPlayProgressBar = (SeekBar) findViewById(R.id.playProgressBar);
        mTvPlayMaxPoint = (TextView) findViewById(R.id.tvPlayMaxPoint);
        mtvRecStartPoint = (TextView) findViewById(R.id.tvRecStartPoint);
        mtvPlayStartPoint = (TextView) findViewById(R.id.tvPlayStartPoint);

        // 버튼에 리스너 할당
        mBtnStartRec.setOnClickListener(this);
        mBtnStartPlay.setOnClickListener(this);
        mBtnDelRec.setOnClickListener(this);
        mBtnSetVoice.setOnClickListener(this);
        mBtnHelp.setOnClickListener(this);

        mRecProgressBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        mPlayProgressBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        sp = (Spinner) this.findViewById(R.id.voice_list);
        voice_list_update();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRec();
    }

    public void voice_list_update(){
        path_list = new ArrayList<String>();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, path_list);
        //스피너 속성
        File f = new File(mFilePath);
        files = f.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().toLowerCase(Locale.US).endsWith(".mp3"); //확장자
            }
        });


        for(int i=0;i<files.length; i++) {
            Log.d("S1", "파일리스트 : " + files[i].toString()+ " 파일길이 : "+ files[i].toString().length() );
            if( !(files[i].toString().length()==42))
                path_list.add(files[i].toString().substring(38, files[i].toString().length() ) ); //파일이 아예 없을때 스피너가 갱신되는 것을 방지
        }

        if(path_list.isEmpty())
            Toast.makeText(VoiceRecord.this,"녹음된 파일이 존재하지않습니다.",Toast.LENGTH_SHORT).show();

        sp.setPrompt("녹음 파일"); // 스피너 제목
        sp.setAdapter(adapter);
        sp.setOnItemSelectedListener(this);

    }
    // 버튼의 OnClick 이벤트 리스너
    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.btnStartRec:
                mBtnStartRecOnClick();
                break;
            case R.id.btnStartPlay:
                mBtnStartPlayOnClick();
                break;
            case R.id.btnDelRec:
                mBtnDelRecOnClick();
                break;
            case R.id.btnSetVoice:
                mBtnSetVoiceOnClick();
                break;
            case R.id.btnHelp:
                mBtnHelpOnClick();
                break;
            default:
                break;
        }
    }

    private void  mBtnHelpOnClick() {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(VoiceRecord.this);
        alertDialogBuilder.setTitle("도움말");

        alertDialogBuilder
                .setMessage("다운받은 파일을 설정하고 싶은경우 음악파일을 \"progress_recorder\" 폴더에 저장하시면 됩니다.")
                .setCancelable(false)
                .setNegativeButton("확인",
                        new DialogInterface.OnClickListener() {
                            public void onClick(
                                    DialogInterface dialog, int id) {

                            }
                });

         // 다이얼로그 생성
         AlertDialog alertDialog = alertDialogBuilder.create();
        // 다이얼로그 보여주기
        alertDialog.show();

    }




    private void mBtnSetVoiceOnClick() {
        try {
            Log.d("S1", "셋버튼 클릭");
            SharedPreferences sV = getSharedPreferences("setVoice", 0);
            SharedPreferences.Editor editor = sV.edit();
            editor.putString("fFilepath", mFilePath + sp.getSelectedItem().toString()); //First라는 key값으로 infoFirst 데이터를 저장한다.
            editor.putString("sFilepath", mFilePath + "air-raid-siren-alert.mp3");
            editor.commit();
            Toast.makeText(VoiceRecord.this, "설정이 완료되었습니다.", Toast.LENGTH_SHORT).show();
        } catch(NullPointerException e) {
            Toast.makeText(VoiceRecord.this, "선택된 파일이 없습니다.", Toast.LENGTH_SHORT).show();
        } catch(Exception e) {
            Toast.makeText(VoiceRecord.this, "에러 발생.", Toast.LENGTH_SHORT).show();
        }
    }
    private void mBtnStartRecOnClick()
    {
        if (mRecState == REC_STOP)
        {
            Log.d("S1","레코딩 시작");
            startRec();
        }
        else if (mRecState == RECORDING)
        {
            stopRec();
            rec_start_time=0;
            mtvRecStartPoint.setText("00:00");
            mHandler.removeMessages(0);
            voice_list_update();
            mRecState = REC_STOP;
            updateUI();
        }
    }

    // 녹음시작
    @SuppressLint("WrongConstant")
    private void startRec()
    {
        mCurRecTimeMs = 0;
        mCurProgressTimeDisplay = 0;

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(VoiceRecord.this);
        alertDialogBuilder.setTitle("녹음");

        final EditText FN_inputtext = new EditText(VoiceRecord.this);
        alertDialogBuilder.setView(FN_inputtext);


        alertDialogBuilder
                .setMessage("저장될 파일의 이름을 입력하세요.")
                .setCancelable(false)
                .setNegativeButton("확인",
                        new DialogInterface.OnClickListener() {
                            public void onClick(
                                    DialogInterface dialog, int id) {
                                mFileName = FN_inputtext.getText().toString() + ".mp3";
                                Log.d("S1","패치 : " + mFilePath + "네임 " + mFileName);
                                // 공백검사 -> 중복검사(for문) -> 다이얼로그 ( 덮어씌울지 , 이름바꿀지) ->
                                //
                                    if ( !(FN_inputtext.getText().toString().replace(" ", "").equals(""))) {
                                        Log.d("S1", mFileName.toString());
                                        // SeekBar의 상태를 0.1초후 체크 시작
                                        mProgressHandler.sendEmptyMessageDelayed(0, 100);

                                        if (mRecorder == null) {
                                            mRecorder = new MediaRecorder();
                                            mRecorder.reset();
                                        } else {
                                            mRecorder.reset();
                                        }
                                        try {
                                            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                                            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
                                            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
                                            mRecorder.setOutputFile(mFilePath + mFileName);
                                            mRecorder.prepare();
                                            mRecorder.start();

                                            mHandler.sendEmptyMessage(0);
                                            mRecState = RECORDING;
                                            updateUI();
                                        } catch (IOException e) {
                                            Toast.makeText(VoiceRecord.this, e.toString(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                   else {
                                        Toast.makeText(VoiceRecord.this, "저장될 파일의 이름을 입력하세요.", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                            }
                        })
                .setPositiveButton("취소",
                        new DialogInterface.OnClickListener() {
                            public void onClick(
                                    DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // 다이얼로그 생성
        AlertDialog alertDialog = alertDialogBuilder.create();
        // 다이얼로그 보여주기
        alertDialog.show();


    }

    // 녹음정지
    private void stopRec()
    {
        boolean rec_check=true;
        try
        {
            mRecorder.stop();

        }
        catch(Exception e)
        {
            rec_check=false;
        }
        finally
        {
            if(mRecorder!=null)
              mRecorder.release();
            mRecorder = null;
            if(rec_check)
                 Toast.makeText(getApplicationContext(),mFileName.substring(0,mFileName.length()-4) + " 파일 녹음이 완료되었습니다.",Toast.LENGTH_SHORT).show();
        }
        mCurRecTimeMs = -999;
        // SeekBar의 상태를 즉시 체크
        mProgressHandler.sendEmptyMessageDelayed(0, 0);
    }

    private void mBtnStartPlayOnClick()
    {
        if (mPlayerState == PLAY_STOP)
        { // 처음에 재생할때

            try{
                Log.d("S1", "파일 다시재생 파일 패치와 파일 " + mFilePath  +sp.getSelectedItem().toString());
                mPlayerState = PLAYING;
                }
             catch (NullPointerException e) {
                Toast.makeText(VoiceRecord.this, "선택된 파일이 없습니다.", Toast.LENGTH_SHORT).show();
                }
            initMediaPlayer();
            startPlay();
            updateUI();
        }
        else if (mPlayerState == PLAYING)
        {
            mPlayerState = PLAY_STOP;
            play_start_time=0;
            mtvPlayStartPoint.setText("00:00");
            mHandler2.removeMessages(0);
            //pausePlay();
            //updateUI();
            stopPlay();
            releaseMediaPlayer();
            updateUI();
        }
    }


    private void initMediaPlayer()
    {
        // 미디어 플레이어 생성
        if (mPlayer == null)
            mPlayer = new MediaPlayer();
        else
            mPlayer.reset();

        mPlayer.setOnCompletionListener(this);
//        Log.d("S1", "미디어 처음생성 파일명은 : " +sp.getSelectedItem().toString());



        try
        {
            String fullFilePath = mFilePath + sp.getSelectedItem().toString();

            mPlayer.setDataSource(fullFilePath);
            mPlayer.prepare();
            int point = mPlayer.getDuration();
            mPlayProgressBar.setMax(point);

            int maxMinPoint = point / 1000 / 60;
            int maxSecPoint = (point / 1000) % 60;
            String maxMinPointStr = "";
            String maxSecPointStr = "";

            if (maxMinPoint < 10)
                maxMinPointStr = "0" + maxMinPoint + ":";
            else
                maxMinPointStr = maxMinPoint + ":";

            if (maxSecPoint < 10)
                maxSecPointStr = "0" + maxSecPoint;
            else
                maxSecPointStr = String.valueOf(maxSecPoint);

            mTvPlayMaxPoint.setText(maxMinPointStr + maxSecPointStr);

            mPlayProgressBar.setProgress(0);
        }
        catch (NullPointerException e) {
            //Toast.makeText(VoiceRecord.this, "선택된 파일이 없습니다.", Toast.LENGTH_SHORT).show(); 미디어 생성시 에러출력하기때문에 필요x
        }
        catch(Exception e)
        {
            Log.v("ProgressRecorder", "미디어 플레이어 Prepare Error ==========> " + e);
        }
    }

    // 재생 시작
    @SuppressLint("WrongConstant")
    private void startPlay()
    {
        Log.v("ProgressRecorder", "startPlay().....");

        try
        {
            Log.d("S1", "파일재생 - 패치와 스피너에서 선택한 파일 체크 " + mFilePath  +sp.getSelectedItem().toString());
            mPlayer.start();
            mHandler2.sendEmptyMessage(0);
            // SeekBar의 상태를 0.1초마다 체크
            mProgressHandler2.sendEmptyMessageDelayed(0, 100);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            mProgressHandler2.sendEmptyMessageDelayed(0, 0);
            Toast.makeText(this, "재생에러 error : " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void pausePlay()
    {
        Log.v("ProgressRecorder", "pausePlay().....");

        // 재생을 일시 정지하고
        mPlayer.pause();

        // 재생이 일시정지되면 즉시 SeekBar 메세지 핸들러를 호출한다.
        mProgressHandler2.sendEmptyMessageDelayed(0, 0);
    }

    private void stopPlay()
    {
        Log.v("ProgressRecorder", "stopPlay().....");

        // 재생을 중지하고
        mPlayer.stop();

        // 즉시 SeekBar 메세지 핸들러를 호출한다.
        mProgressHandler2.sendEmptyMessageDelayed(0, 0);
    }

    private void releaseMediaPlayer()
    {
        Log.v("ProgressRecorder", "releaseMediaPlayer().....");
        mPlayer.release();
        mPlayer = null;
        mPlayProgressBar.setProgress(0);
    }

    public void onCompletion(MediaPlayer mp)
    {
        mPlayerState = PLAY_STOP; // 재생이 종료됨

        // 재생이 종료되면 즉시 SeekBar 메세지 핸들러를 호출한다.
        mProgressHandler2.sendEmptyMessageDelayed(0, 0);
        mHandler2.removeMessages(0);
        updateUI();
    }

    private void mBtnDelRecOnClick()
    {
        try {
            File f=new File(mFilePath + sp.getSelectedItem().toString());
            if (f.delete()) {
                Toast.makeText(VoiceRecord.this, "삭제 성공", Toast.LENGTH_SHORT).show();
                voice_list_update();
            }
            else
                Toast.makeText(VoiceRecord.this, "삭제 실패", Toast.LENGTH_SHORT).show();
        } catch (NullPointerException e) {
            Toast.makeText(VoiceRecord.this, "선택된 파일이 없습니다.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(VoiceRecord.this, e.toString() , Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUI()
    {
        if (mRecState == REC_STOP)
        {
            // rec로
            mBtnStartRec.setBackgroundDrawable(getResources().getDrawable(R.drawable.voice_record_rec));

            mRecProgressBar.setProgress(0);
        }
        else if (mRecState == RECORDING) {
            //mBtnStartRec.setText("Stop");
            //mBtnStartRec.setBackgroundDrawable(getResources().getDrawable(R.drawable.stop_button2));
            mBtnStartRec.setBackgroundDrawable(getResources().getDrawable(R.drawable.voice_record_stop));
        }
        if (mPlayerState == PLAY_STOP)
        {
           // mBtnStartPlay.setText("Play");

            //mBtnStartPlay.setBackgroundDrawable(getResources().getDrawable(R.drawable.play_button2));
            mBtnStartPlay.setBackgroundDrawable(getResources().getDrawable(R.drawable.voice_record_play));
            mPlayProgressBar.setProgress(0);
        }
        else if (mPlayerState == PLAYING) {
            //mBtnStartPlay.setText("Stop");
            mBtnStartPlay.setBackgroundDrawable(getResources().getDrawable(R.drawable.voice_record_stop));
           // mBtnStartPlay.setBackgroundDrawable(getResources().getDrawable(R.drawable.stop_button2));
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                               long arg3) {
        // TODO Auto-generated method stub
      //  Toast.makeText(this, path_list.get(arg2), Toast.LENGTH_LONG).show();//해당목차눌렸을때

    }
    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }

}
