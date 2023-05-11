package bluetooth.maiziclass_3;

import android.Manifest;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import bluetooth.maiziclass_3.connect.AcceptThread;
import bluetooth.maiziclass_3.connect.ConnectThread;
import bluetooth.maiziclass_3.connect.Constant;

public class MainActivity extends AppCompatActivity {
    public static final int REQUEST_CODE = 0;
    private List<BluetoothDevice> mDeviceList = new ArrayList<>();
    private List<BluetoothDevice> mBondedDeviceList = new ArrayList<>();

    private BlueToothController mController = new BlueToothController();
    private Handler mUIHandler = new MyHandler();

    private ListView mListView;
    private DeviceAdapter mAdapter;
    private Toast mToast;

    private boolean isServer = false;
    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        initActionBar();
        initUI();

        registerBluetoothReceiver();//蓝牙广播

        //软件运行时直接申请打开蓝牙
        mController.turnOnBlueTooth(this, REQUEST_CODE);

//        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
//        registerReceiver(receiver0, filter);
    }

    //通过监听器来监听操作结果
    private BroadcastReceiver receiver0 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
            switch (state) {
                case BluetoothAdapter.STATE_OFF://蓝牙关闭
                    Toast.makeText(MainActivity.this, "STATE_OFF", Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothAdapter.STATE_ON://蓝牙打开
                    Toast.makeText(MainActivity.this, "STATE_ON", Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothAdapter.STATE_TURNING_ON://正在打开
                    Toast.makeText(MainActivity.this, "STATE_TURNING_ON", Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF://正在关闭
                    Toast.makeText(MainActivity.this, "STATE_TURNING_OFF", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(MainActivity.this, "STATE_UNKNOWN", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private void registerBluetoothReceiver() {
        IntentFilter filter = new IntentFilter();
        //开始查找
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        //结束查找
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        //查找设备
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        //设备扫描模式改变
        filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        //绑定状态
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);

        registerReceiver(receiver, filter);
    }

    //注册广播监听搜索结果
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                //setProgressBarIndeterminateVisibility(true);
                //初始化数据列表
                mDeviceList.clear();
                mAdapter.notifyDataSetChanged();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {//查找结束
                setProgressBarIndeterminateVisibility(false);
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mDeviceList.add(device);
                mAdapter.notifyDataSetChanged();

            } else if (BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(action)) {
                int scanMode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, 0);
                if (scanMode == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {//可见的
                    setProgressBarIndeterminateVisibility(true);
                } else {//不可见的
                    setProgressBarIndeterminateVisibility(false);
                }

            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                BluetoothDevice remoteDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (remoteDevice == null) {
                    showToast("无设备");
                    return;
                }
                int status = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, 0);
                if (status == BluetoothDevice.BOND_BONDED) {
                    showToast("已绑定" + remoteDevice.getName());
                } else if (status == BluetoothDevice.BOND_BONDING) {
                    showToast("正在绑定" + remoteDevice.getName());
                } else if (status == BluetoothDevice.BOND_NONE) {
                    showToast("未绑定" + remoteDevice.getName());
                }
            }
        }
    };

    //初始化用户界面
    private void initUI() {
        mListView = findViewById(R.id.device_list);
        mAdapter = new DeviceAdapter(mDeviceList, this);

        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(bondDeviceClick);
    }
//    private void initActionBar() {
//        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
//        getActionBar().setDisplayUseLogoEnabled(false);
//        setProgressBarIndeterminate(true);
//        try {
//            ViewConfiguration config = ViewConfiguration.get(this);
//            Field menuKeyField = ViewConfiguration.class
//                    .getDeclaredField("sHasPermanentMenuKey");
//            if (menuKeyField != null) {
//                menuKeyField.setAccessible(true);
//                menuKeyField.setBoolean(config, false);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.enable_visibility) {
            mController.enableVisibility(this);
        }
        //查找设备
        else if (id == R.id.find_device) {
            checkPermission();
        }
        //查看已绑定设备
        else if (id == R.id.bonded_device) {
            mBondedDeviceList = mController.getBondedDeviceList();//获取绑定设备
            mAdapter.refresh(mBondedDeviceList);
            mListView.setOnItemClickListener(bondedDeviceClick);
        } else if (id == R.id.listening) {
            if (mAcceptThread != null) {
                mAcceptThread.cancel();
            }
            mAcceptThread = new AcceptThread(mController.getAdapter(), mUIHandler);
            mAcceptThread.start();
            isServer = true;
        } else if (id == R.id.stop_listening) {
            if (mAcceptThread != null) {
                mAcceptThread.cancel();
            }
            isServer = false;
        } else if (id == R.id.disconnect) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
            }
        } else if (id == R.id.say_hello) {
            say("Hello");
        } else if (id == R.id.say_hi) {
            say("Hi");
        }

        return super.onOptionsItemSelected(item);
    }

    private void doFindDevice() {
        mAdapter.refresh(mDeviceList);
        mController.findDevice();//高版本系统调用该方法，需要动态请求权限
        mListView.setOnItemClickListener(bondDeviceClick);
    }

    private void checkPermission() {
        //权限是否已经赋予
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            //未赋予权限，申请权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
        } else {
            //权限已赋予
            doFindDevice();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            //权限的申请结果返回
            case REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //已授权
                    Toast.makeText(this, "授权成功！", Toast.LENGTH_SHORT).show();
                    doFindDevice();
                } else {
                    //未授权
                    Toast.makeText(this, "授权被拒绝！", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void say(String word) {
        if (isServer) {
            if (mAcceptThread != null) {
                try {
                    mAcceptThread.sendData(word.getBytes("utf-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        } else {
            if (mConnectThread != null) {
                try {
                    mConnectThread.sendData(word.getBytes("utf-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private AdapterView.OnItemClickListener bondDeviceClick = new AdapterView.OnItemClickListener() {
        @TargetApi(Build.VERSION_CODES.KITKAT)
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            BluetoothDevice device = mDeviceList.get(i);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                device.createBond();
            }
        }
    };

    private AdapterView.OnItemClickListener bondedDeviceClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            BluetoothDevice device = mBondedDeviceList.get(i);
            if (mConnectThread != null) {
                mConnectThread.cancel();
            }
            mConnectThread = new ConnectThread(device, mController.getAdapter(), mUIHandler);
            mConnectThread.start();
        }
    };

    private class MyHandler extends Handler {
        public void handleMessage(Message message) {
            super.handleMessage(message);
            switch (message.what) {
                case Constant.MSG_GOT_DATA:
                    showToast("data:" + String.valueOf(message.obj));
                    break;
                case Constant.MSG_ERROR:
                    showToast("error:" + String.valueOf(message.obj));
                    break;
                case Constant.MSG_CONNECTED_TO_SERVER:
                    showToast("连接到服务端");
                    isServer = false;
                    break;
                case Constant.MSG_GOT_A_CLINET:
                    showToast("找到服务端");
                    break;
            }
        }
    }

    //设置toast的标准格式
    private void showToast(String text) {
        if (mToast == null) {
            mToast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
            mToast.show();
        } else {
            mToast.setText(text);
            mToast.show();
        }

    }

    /**
     * 退出时注销广播、注销连接过程、注销等待连接的监听
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAcceptThread != null) {
            mAcceptThread.cancel();
        }
        if (mConnectThread != null) {
            mConnectThread.cancel();
        }

        unregisterReceiver(receiver);
    }
}
