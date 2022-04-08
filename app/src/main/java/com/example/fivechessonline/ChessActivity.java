package com.example.fivechessonline;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class ChessActivity extends AppCompatActivity {
    private static final String TAG = ChessActivity.class.getSimpleName();
    private int tableId;
    private transient boolean stopFlag = false;
    private StepTurn stepTurn = StepTurn.NONE;
    private String blackOrWhite;
    private Runnable runnable;
    private enum StepTurn {
        BLACK {
            public String toString() {
                return "black";
            }

            public int chessDrawableId() {
                return R.drawable.black;
            }
        }, WHITE {
            public String toString() {
                return "white";
            }

            public int chessDrawableId() {
                return R.drawable.white;
            }
        }, NONE;

        public int chessDrawableId() {
            return 0;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_chess );

        Intent intent = getIntent();
        this.tableId = intent.getIntExtra( "tableId",0 );
        this.blackOrWhite = intent.getStringExtra( "blackOrWhite" );

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics( metrics );
        LinearLayout.LayoutParams tableContainerLayoutParams = new LinearLayout.LayoutParams( metrics.widthPixels,metrics.widthPixels );
        ((FrameLayout) findViewById( R.id.tableContainer )).setLayoutParams( tableContainerLayoutParams  );

        LinearLayout chessButtons = (LinearLayout) findViewById( R.id.chessButtons );
        for(int i = 0;i < 9;i++) {
            LinearLayout oneLine = new LinearLayout( this );
            for(int j = 0;j < 9;j++) {
                Button button = new Button( this );
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams( metrics.widthPixels/9,metrics.widthPixels /9);
                button.setLayoutParams( layoutParams );
                button.setBackgroundColor( Color.TRANSPARENT );
                int finalJ = j;
                int finalI = i;
                button.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(stepTurn.toString().equals( blackOrWhite ) ) {
                            button.setBackground( ActivityCompat.getDrawable( getApplicationContext(),stepTurn.chessDrawableId() ) );
                            String url = "https://fivechess.tzchenyu.com/walk/" + tableId + "/" + finalJ + "/" + finalI;
                            final Request request = new Request.Builder().url( url ).build();
                            MainActivity.okHttpClient.newCall( request ).enqueue( new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    Log.i( TAG,"onFailure: "+call.toString(),e );
                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {

                                }
                            } );
                            stepTurn = StepTurn.NONE;
                        }
                    }
                } );
                oneLine.addView( button );
            }
            chessButtons.addView( oneLine );
        }
        runnable = new Runnable() {
            @Override
            public void run() {
                while(!stopFlag) {
                    String url = "https://fivechess.tzchenyu.com/table_status/" + tableId;
                    final Request request = new Request.Builder().url( url ).build();
                    MainActivity.okHttpClient.newCall( request ).enqueue( new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.i( TAG, "onFailure: " + call.toString(), e );
                        }
                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String content = response.body().string();
                            Log.i(TAG,content);
                            Gson gson = new Gson();
                            Map<String,Object> result = gson.fromJson(content, Map.class);
                            ///String status = (String) result.get("status");
                            Log.i( TAG,"onResponse: " +  result.get( "table" ));
                            final Map<String, Object> tableInfo = (Map<String, Object>) result.get( "table" );
                            Runnable runnable = new Runnable() {
                                public void run() {
                                    if(tableInfo.get( "black_user_info" )!=null) {
                                        Map<String,Object> blackUserInfo = (Map<String, Object>) tableInfo.get( "black_user_info" );
                                        String blackUserName = (String) blackUserInfo.get( "name" );
                                        ((TextView) findViewById( R.id.userBlack )).setText( blackUserName );
                                    }
                                    if(tableInfo.get( "white_user_info" )!=null) {
                                        Map<String,Object> whiteUserInfo = (Map<String, Object>) tableInfo.get( "white_user_info" );
                                        String whiteUserName = (String) whiteUserInfo.get( "name" );
                                        ((TextView) findViewById( R.id.userWhite )).setText( whiteUserName );
                                    }
                                    if(result.get( "game" )!=null) {
                                        Map<String,Object> gameInfo = (Map<String, Object>) result.get( "game" );
                                        String next = (String) gameInfo.get( "next" );
                                        if("black".equals( next )) {
                                            stepTurn = StepTurn.BLACK;
                                            ((TextView) findViewById( R.id.tableStatus )).setText( "黑棋走子" );
                                        } else if("white".equals( next )){
                                            stepTurn = StepTurn.WHITE;
                                            ((TextView) findViewById( R.id.tableStatus )).setText( "白棋走子" );
                                        }
                                        if(gameInfo.get( "winner" )!=null) {
                                            String winner = (String) gameInfo.get("winner");
                                            ((TextView) findViewById( R.id.tableStatus )).setText( "对局结束，胜者：" + winner );
                                            stepTurn = StepTurn.NONE;
                                        }
                                    }
                                    if(result.get( "steps" )!=null) {
                                        List<Map<String,Object>> steps = (List<Map<String, Object>>) result.get( "steps" );
                                        for (Map<String,Object> step:steps) {
                                            int stepNum = (int) (double) step.get( "step_num" );
                                            int x = (int) (double) step.get( "x" );
                                            int y = (int) (double) step.get( "y" );
                                            LinearLayout oneLine = (LinearLayout) ((LinearLayout) findViewById( R.id.chessButtons )).getChildAt( y );
                                            Button chess = (Button) oneLine.getChildAt( x );
                                            if((stepNum&1)==1) {
                                                chess.setBackground( ActivityCompat.getDrawable( getApplicationContext(),StepTurn.BLACK.chessDrawableId() ) );
                                            } else {
                                                chess.setBackground( ActivityCompat.getDrawable( getApplicationContext(),StepTurn.WHITE.chessDrawableId() ) );
                                            }
                                        }
                                    }
                                }
                            };
                            runOnUiThread( runnable );

                        }
                    } );
                    try {
                        TimeUnit.SECONDS.sleep( 1 );
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        } ;
    }
    public void leaveTable(View view) {
        String url = "https://fivechess.tzchenyu.com/leave_table/"+tableId;
        Request request = new Request.Builder().url(url).build();
        MainActivity.okHttpClient.newCall(request).enqueue( new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                finish();
            }
        } );
    }

    @Override
    protected void onResume() {
        super.onResume();
        stopFlag = false;
        new Thread(runnable).start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopFlag = true;
    }


}