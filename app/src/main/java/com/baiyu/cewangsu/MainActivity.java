package com.baiyu.cewangsu;

import android.Manifest;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.*;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 欠流量统计功能 ✔
 * http 服务器响应数据异常 更新ui 未解决  ✔
 * 下拉列表无删除功能
 * 下拉列表 无限增加连接 ✔
 * app 再次打开 无法恢复上次使用的url  ✔
 * 实时网速有问题 ✔
 * 后台运行按钮需要调整 ✔
 * 重要一点 app 需要重构 代码沉余验证
 * 作者对于应用权限 也是迷迷糊糊 不知道要不要申请
 * */
public class MainActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener,RadioGroup.OnCheckedChangeListener,AdapterView.OnItemSelectedListener , TextWatcher {
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
     Handler wsHnadler ;//网速handler
    Handler logHandler;

    Spinner spinnerURL;//连接下拉列表

    List<String> spinnerDate;

    ArrayAdapter<String> arrayAdapter;

    static TextView logError;

    TextView totalData;//历史总流量

    NetWorkSpeedUtils netWorkSpeedUtils;//为了能结束实时网速 没有版本

    /**下拉列表第一次打开 自动就执行了监听方法 气死了 试了其他方法都不行， 只能这样了*/
    static boolean isFirst=true;//true 表示第一次打开app
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        logError=findViewById(R.id.logArea);
        downloadUrl=findViewById(R.id.download_url);
        downloadUrl.addTextChangedListener(this);

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
        //backrun.setEnabled(false);
        radioButtonUp=findViewById(R.id.up);//上传单选
        radioButtonDown=findViewById(R.id.down);//下载
        wangSu=findViewById(R.id.wang_su);
        totalData=findViewById(R.id.totalData);
        radioGroup1 = findViewById(R.id.is_down);
        radioGroup1.setOnCheckedChangeListener(this);//监听选中上传还是下载

        radioButtonTCP=findViewById(R.id.TCP);
        radioButtonUDP=findViewById(R.id.UDP);

        ApplicationFile.createConfigurationFile(this);//创建配置文件 记录用过的连接



        if(ApplicationFile.getTotalData()<=1048576){
            String s = new DecimalFormat("0.00").format(ApplicationFile.getTotalData() / 1024d) + "MB";
            totalData.setText(s);
        } else if (ApplicationFile.getTotalData()<=1073741824) {
            String s = new DecimalFormat("0.00").format(ApplicationFile.getTotalData() / 1024 / 1024d) + "GB";
            totalData.setText(s);
        }
        else {
            String s = new DecimalFormat("0.00").format(ApplicationFile.getTotalData() / 1024 /1024/ 1024d) + "TB";
            totalData.setText(s);
        }


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
        NetWorkSpeedUtils.totalData = ApplicationFile.getTotalData();//KB 一开始就要文件获取总流量

        //System.out.println("hhheee:"+NetWorkSpeedUtils.totalData);
        ApplicationFile.getArraySpinnerFromFile(spinnerDate);//获取json 对象数组连接 添加到列表
        arrayAdapter=new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,spinnerDate);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);

        spinnerURL.setAdapter(arrayAdapter);//下拉列表添加适配器


    //为下拉列表设置监听器
        spinnerURL.setOnItemSelectedListener(this);

        if(!ApplicationFile.getBackGroupRun()){//true 为起用
            startCheckSpeed();
            netWorkSpeedUtils=new NetWorkSpeedUtils(this,wsHnadler);
            netWorkSpeedUtils.startShowNetSpeed();
        }
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
                stopNetwork();//停止下载
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
       // System.out.println("12345789"+startButton.isClickable());

        ApplicationFile.setBackGroupRun(ApplicationFile.getStartAndStop());//开始按钮决定是否开启网速
        finish();
    }
    private void stopNetwork(){//点击停止按钮执行该函数
        netWorkSpeedUtils.task.cancel();//结束网速检测 但是网速没有置0

        wangSu.setText("网速:无网络传输");
        ApplicationFile.setStartAndStop(true);//开始按钮起用
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        //stopService(new Intent(MainActivity.this,DownloadServer.class));
        DownloadThread.isRun=false;
        DownloadTeadUDPUp.isRun=false;

        ApplicationFile.setTotalData(NetWorkSpeedUtils.totalData*1024);

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

        //异常更新ui
        this.logHandler=new Handler() {
            @Override
            public void handleMessage(Message msg){
                logError.setText(msg.obj.toString());
                startButton.setEnabled(true);
                stopButton.setEnabled(false);
            }
        };

        ApplicationFile.setStartAndStop(false);//false 开始按钮不起用

        if(TCP) {
            if(UP){
               // downloadThread = new DownloadThread();//TCP 下载
                DownloadThread.isRun=true;//为true 要求线程运行
                DownloadThread.loghandler=this.logHandler;//让子线程能通知主线程修改ui
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


                DownloadThread.loghandler=this.logHandler;

                ApplicationFile.removeSwitchCondiction();
                ApplicationFile.setSwitchCondiction(false,true);

                //ApplicationFile.setBackGroupRun(false);//记录开始按钮状态 false 开始按钮不可用
            }
            else {
                Toast.makeText(this,"暂时不支持UDP 下载",Toast.LENGTH_SHORT).show();
                return;
            }
        }
        if (ApplicationFile.isExisitInArray(downloadUrl.getText().toString())) {//true 表示连接不存在  要加连接？
            ApplicationFile.insertUrlRecord(spinnerDate.size());//下拉列表长度加1
        }




        startService(intent);


        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        //backrun.setEnabled(true);//点击开始按钮才可以点击后台执行
         startCheckSpeed();




        netWorkSpeedUtils=new NetWorkSpeedUtils(this,wsHnadler);
        netWorkSpeedUtils.startShowNetSpeed();
    }

    private void startCheckSpeed(){
        wsHnadler= new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 100:
                        wangSu.setText("网速:" + msg.obj.toString());
                        totalData.setText(NetWorkSpeedUtils.total);
                        break;
                }
                //super.handleMessage(msg);
            }
        };
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

        if(isFirst){
            downloadUrl.setText(arrayAdapter.getItem(ApplicationFile.getUrlRecord()));
            isFirst=false;
            }//第一次 在文件读取 位置
        else {


            //其他靠点击 下拉列表获取位置
            ApplicationFile.insertUrlRecord(i);


            downloadUrl.setText(arrayAdapter.getItem(i));
        }
    }

    @Override//下拉列表的监听
    public void onNothingSelected(AdapterView<?> adapterView) {//Adapter 为空时候执行这个方法

    }


    @Override
    protected void onDestroy() {
        ApplicationFile.setTotalData(NetWorkSpeedUtils.totalData);
        super.onDestroy();
    }

    @Override//url 文本监听
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override//url 文本监听
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override//url 文本监听
    public void afterTextChanged(Editable editable) {
        //downloadUrl.setHint("");
    }
}