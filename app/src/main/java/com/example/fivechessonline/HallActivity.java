package com.example.fivechessonline;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.fivechessonline.bean.ChessTable;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class HallActivity extends AppCompatActivity {

    private final static String TAG = HallActivity.class.getSimpleName() ;
    private transient boolean stopFlag = false;
    private Runnable runnable;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hall);

         runnable = new Runnable() {
            @Override
            public void run() {
                while (!stopFlag) {
                    setupHall();
                    try {
                        TimeUnit.SECONDS.sleep( 2 );
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        } ;
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.stopFlag = false;
        new Thread(runnable).start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.stopFlag = true;
    }

    private void setupHall() {
        String url = "https://fivechess.tzchenyu.com/chess_table";
        final Request request = new Request.Builder().url(url).build();
        MainActivity.okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(getApplicationContext(),"Error in get chess table",Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                SharedPreferences sp = getSharedPreferences( "user",MODE_PRIVATE );
                int userId = sp.getInt( "id", 0);
                String content = response.body().string();
                Log.i(TAG,content);
                Gson gson = new Gson();
                final ChessTable[] chessTables = gson.fromJson(content,ChessTable[].class);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        LinearLayout linearLayout = findViewById(R.id.hall);
                        linearLayout.removeAllViews();
                        LinearLayout oneLine = null;
                        for(int i = 0;i < chessTables.length;i++) {
                            ChessTable chessTable = chessTables[i];
                            if((i&1)==0) {
                                oneLine = new LinearLayout(getApplicationContext());
                                oneLine.setOrientation( LinearLayout.HORIZONTAL );
                                linearLayout.addView(oneLine);
                            }
                            View view = getLayoutInflater().inflate(R.layout.table,null);
                            Button blackButton = (Button) view.findViewById( R.id.black );
                            if(chessTable.getUser_black() > 0) {
                                blackButton.setBackground( ActivityCompat.getDrawable( getApplicationContext(),R.drawable.people ) );
                                if(chessTable.getUser_black()==userId) {
                                    sitDownAgain( chessTable, blackButton ,"black");
                                }
                            } else {
                                blackButton.setBackground( ActivityCompat.getDrawable( getApplicationContext(),R.drawable.down ) );
                                blackButton.setOnClickListener( new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        sitDown( chessTable ,"black");

                                    }
                                } );
                            }
                            Button whiteButton = (Button) view.findViewById( R.id.white );
                            if(chessTable.getUser_white() > 0) {
                                whiteButton.setBackground( ActivityCompat.getDrawable( getApplicationContext(),R.drawable.people ) );
                                if(chessTable.getUser_white()==userId) {
                                    sitDownAgain( chessTable, whiteButton ,"white");
                                }
                            } else {
                                whiteButton.setBackground( ActivityCompat.getDrawable( getApplicationContext(),R.drawable.down ) );
                                whiteButton.setOnClickListener( new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        sitDown( chessTable ,"white");
                                    }
                                } );
                            }
                            oneLine.addView(view);
                        }
                    }
                });
            }
        });
    }
    private void sitDown(ChessTable chessTable,String s) {
        String url = "https://fivechess.tzchenyu.com/join_table/" + chessTable.getId() + "/" + s;
        final Request request = new Request.Builder().url( url ).build();
        MainActivity.okHttpClient.newCall( request ).enqueue( new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i( TAG, "onFailure: " + call.toString(), e );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Intent intent = new Intent( getApplicationContext(), ChessActivity.class );
                intent.putExtra( "tableId", chessTable.getId() );
                intent.putExtra( "blackOrWhite",s );
                startActivity( intent );
            }
        } );
    }

    private void sitDownAgain(ChessTable chessTable, Button whiteButton,String blackOrWhite) {
        whiteButton.setBackground( ActivityCompat.getDrawable( getApplicationContext(), R.drawable.flower ) );
        whiteButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent( getApplicationContext(), ChessActivity.class );
                intent.putExtra( "tableId", chessTable.getId() );
                intent.putExtra( "blackOrWhite",blackOrWhite );
                startActivity( intent );
            }
        } );
    }


}