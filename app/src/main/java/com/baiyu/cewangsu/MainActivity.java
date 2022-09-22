package com.baiyu.cewangsu;

import android.Manifest;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.*;

import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener,RadioGroup.OnCheckedChangeListener,AdapterView.OnItemSelectedListener {
    //下载链接
     EditText downloadUrl;
    //开始按钮
     Button startButton;
    //停止按钮
     Button stopButton;
    //后台运行
     Button backrun;

    //线程数目
     TextView xiancChengShu;
    //拖拽进度条
     SeekBar seekBar;
    //单选框
     RadioGroup radioGroup1;
    //文件路径
     TextView filePath;
    //选择文件按钮
     Button chooseFileButton;

    //显示网速
     TextView wangSu;

     RadioButton radioButtonUp;//上传选择
     RadioButton radioButtonDown;//下载选择
     RadioButton radioButtonTCP;//TCP协议选择

     RadioButton radioButtonUDP;//UDP 协议选择
     Handler wsHnadler ;

     Spinner spinnerURL;//连接下拉列表

    List<String> spinnerDate;

    ArrayAdapter<String> arrayAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        downloadUrl=findViewById(R.id.download_url);
        startButton= findViewById(R.id.start_button);
        startButton.setOnClickListener(this);//开始按钮

        xiancChengShu =findViewById(R.id.xain_cheng_shu);
        chooseFileButton=findViewById(R.id.chooss_file_button);
        chooseFileButton.setOnClickListener(this);
        filePath=findViewById(R.id.file_path);
        stopButton=findViewById(R.id.stop_button);
        stopButton.setOnClickListener(this);

        spinnerURL = findViewById(R.id.spinnerURLRecord);
        spinnerDate=new ArrayList<>();//

        seekBar=findViewById(R.id.seek_bar_menu);
        seekBar.setOnClickListener(this);
        seekBar.setOnSeekBarChangeListener(this);
        backrun=findViewById(R.id.back_run);
        backrun.setOnClickListener(this);
        backrun.setEnabled(false);
        radioButtonUp=findViewById(R.id.up);//上传单选
        radioButtonDown=findViewById(R.id.down);//下载
        wangSu=findViewById(R.id.wang_su);

        radioGroup1 = findViewById(R.id.is_down);
        radioGroup1.setOnCheckedChangeListener(this);//监听选中上传还是下载

        radioButtonTCP=findViewById(R.id.TCP);
        radioButtonUDP=findViewById(R.id.UDP);

        ApplicationFile.createConfigurationFile(this);//创建配置文件 记录用过的连接

        //文件 获取按钮状态
        boolean[] bool=ApplicationFile.getSwitchCondiction();
        if (bool[0]) {
            radioButtonTCP.setChecked(true);
        } else {
            radioButtonUDP.setChecked(true);
        }
        radioButtonUp.setChecked(bool[1]);


        if (bool[1]) {
            radioButtonUp.setChecked(true);
        }
        else {
            radioButtonDown.setChecked(true);
        }
        // startButton.setEnabled(bool[0]);
        //stopButton.setEnabled(bool[1]);//停止按钮
       boolean boolSwitch=ApplicationFile.getStartAndStop();//获取开始和停止按钮信息
        if (boolSwitch) {
            startButton.setEnabled(true);stopButton.setEnabled(false);//true 开始可以
        }
        else {
            startButton.setEnabled(false);stopButton.setEnabled(true);//false 开始不可以
        }

        ApplicationFile.getArraySpinnerFromFile(spinnerDate);//获取json 对象数组连接 添加到列表
        arrayAdapter=new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,spinnerDate);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinnerURL.setAdapter(arrayAdapter);//下拉列表添加适配器
    //为下拉列表设置监听器
        spinnerURL.setOnItemSelectedListener(this);
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
                backRun();//后台运行
        }
    }

    private void openNetWork(){
        int hasPermissions= ContextCompat.checkSelfPermission(getApplication(), Manifest.permission.INTERNET);
        if(hasPermissions!= PackageManager.PERMISSION_GRANTED){//没有权限
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.INTERNET},1);
        }
    }
    private void backRun(){//后台运行
        ApplicationFile.setStartAndStop(false);
        finish();
    }
    private void stopNetwork(){//点击停止按钮执行该函数
        ApplicationFile.setStartAndStop(true);//开始按钮起用
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
                ApplicationFile.setSwitchCondiction(true,false);
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
                System.out.println("zhixingdaol");
                ApplicationFile.removeSwitchCondiction();
                ApplicationFile.setSwitchCondiction(false,true);

            }
            else {
                Toast.makeText(this,"暂时不支持UDP 下载",Toast.LENGTH_SHORT).show();
                return;
            }
        }

        startService(intent);

        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        backrun.setEnabled(true);//点击开始按钮才可以点击后台执行
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
                //if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){//安卓4.4之后
                    path=GetFilePath.getPath(this,uri);//获取文件路径
                    filePath.setText(path);//输入到文本显示里面
                //}
                //else {
                 //   System.out.println("安卓4.4以下没有写获取路径");
               // }
              }

    }
    }


    @Override//下拉列表的监听
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        downloadUrl.setText(arrayAdapter.getItem(i));

    }

    @Override//下拉列表的监听
    public void onNothingSelected(AdapterView<?> adapterView) {
        downloadUrl.setText("hello");
    }
}