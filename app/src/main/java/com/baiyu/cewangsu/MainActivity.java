package com.baiyu.cewangsu;

import android.Manifest;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.GnssAntennaInfo;
import android.net.Uri;
import android.os.*;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.net.URI;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener,RadioGroup.OnCheckedChangeListener {
    //下载链接
    static EditText downloadUrl;
    //开始按钮
    static Button startButton;
    //停止按钮
    static Button stopButton;
    //后台运行
    static Button backrun;

    //线程数目
    static TextView xiancChengShu;
    //拖拽进度条
    static SeekBar seekBar;
    //单选框
    static RadioGroup radioGroup1;
    //文件路径
    static TextView filePath;
    //选择文件按钮
    static Button chooseFileButton;


    static TextView wangSu;

    static RadioButton radioButtonUp;
    static RadioButton radioButtonTCP;


    private Handler wsHnadler ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        downloadUrl=findViewById(R.id.download_url);
        startButton= findViewById(R.id.start_button);
        startButton.setOnClickListener(this);
        xiancChengShu =findViewById(R.id.xain_cheng_shu);
        chooseFileButton=findViewById(R.id.chooss_file_button);
        chooseFileButton.setOnClickListener(this);
        filePath=findViewById(R.id.file_path);
        stopButton=findViewById(R.id.stop_button);
        stopButton.setOnClickListener(this);
        stopButton.setEnabled(false);

        seekBar=findViewById(R.id.seek_bar_menu);
        seekBar.setOnClickListener(this);
        seekBar.setOnSeekBarChangeListener(this);
        backrun=findViewById(R.id.back_run);
        backrun.setOnClickListener(this);
        radioButtonUp=findViewById(R.id.up);//上传单选
        wangSu=findViewById(R.id.wang_su);

        radioGroup1 = findViewById(R.id.is_down);
        radioGroup1.setOnCheckedChangeListener(this);//监听选中上传还是下载

        radioButtonTCP=findViewById(R.id.TCP);


    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start_button:
                //openNetWork();
                downloadDisribution();//开始下载
                break;
            case R.id.seek_bar_menu:
                break;
            case R.id.chooss_file_button:
                Intent myFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
                myFileIntent.setType("*/*");
                startActivityForResult(myFileIntent,10);//最后会返回数据到在本类的onActivityResult方法（也就是调用这个方法）
                break;
            case R.id.stop_button:
                stopNetwork();
                break;
            case R.id.back_run:
                backRun();
        }
    }

    private void openNetWork(){
        int hasPermissions= ContextCompat.checkSelfPermission(getApplication(), Manifest.permission.INTERNET);
        if(hasPermissions!= PackageManager.PERMISSION_GRANTED){//没有权限
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.INTERNET},1);
        }
    }
    private void backRun(){
        finish();
    }
    private void stopNetwork(){
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        //stopService(new Intent(MainActivity.this,DownloadServer.class));
        DownloadThread.isRun=false;
        DownloadTeadUDPUp.isRun=false;

    }

    private void downloadDisribution(){
        String url=downloadUrl.getText().toString();
        String path=filePath.getText().toString();

        if(url.length()==0&&path.length() == 0){
            Toast.makeText(this, "请输入链接或点击选择文件",Toast.LENGTH_SHORT).show();
            return;
        }
        CharSequence nums=xiancChengShu.getText();
        //int num=Integer.valueOf(nums.toString()).intValue();
        Intent intent=new Intent(this,DownloadServer.class);
        intent.putExtra("线程数",nums);
        intent.putExtra("url",url);//下载链接
        intent.putExtra("TCP",radioButtonTCP.isChecked());//协议
        intent.putExtra("UP",radioButtonUp.isChecked());//上传或下载
        intent.putExtra("path",path);//文件路径

        boolean TCP=radioButtonTCP.isChecked();
        boolean UP=radioButtonTCP.isChecked();
        DownloadThread.url =url;//下载链接
        DownloadThread.path=path;//文件路径
        if(TCP) {
            if(UP){
               // downloadThread = new DownloadThread();//TCP 下载
                DownloadThread.isRun=true;//为true 要求线程运行
            }
            else {
                //downloadThread=new DownloadThreadTCPUp();//TCP 上传

                Toast.makeText(this,"暂时不支持TCP 上传",Toast.LENGTH_SHORT).show();
                return;
            }

        }

        else {
            if (!UP) {//UDP上传
                DownloadTeadUDPUp.isRun=true;//要求线程运行
                if(DownloadThread.path.length()==0){//没有选择文件
                    Toast.makeText(this, "请选择文件",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (DownloadThread.url.split(":")[0].length() == 0) {
                    Toast.makeText(this, "还需要在输入框输入ip和端口呢!!!",Toast.LENGTH_SHORT).show();
                    return;
                }

            }
            else {
                Toast.makeText(this,"暂时不支持UDP 下载",Toast.LENGTH_SHORT).show();
                return;
            }
        }

        startService(intent);
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        wsHnadler= new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 100:
                    wangSu.setText("系统网速:" + msg.obj.toString());
                    break;
                }
                super.handleMessage(msg);
            }
        };

        new NetWorkListenerUtils(this,wsHnadler).startShowNetSpeed();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        int num=seekBar.getProgress();
        xiancChengShu.setText(String.valueOf(num));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        int num=seekBar.getProgress();
        xiancChengShu.setText(String.valueOf(num));
       // Toast.makeText(this,""+num, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        System.out.println("yy:"+radioButtonTCP.isChecked());

    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri uri=null;
        String path=null;
        if (requestCode ==10 ) {
            if(data!=null&&data.getData()!=null){
                uri=data.getData();
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){//安卓4.4之后
                    path=GetFilePath.getPath(this,uri);//获取文件路径
                    filePath.setText(path);//输入到文本显示里面
                }
                else {
                    System.out.println("安卓4.4以下没有写获取路径");
                }
              }

    }
    }






}