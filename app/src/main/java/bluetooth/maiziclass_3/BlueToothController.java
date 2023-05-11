package bluetooth.maiziclass_3;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.List;

public class BlueToothController {

    private BluetoothAdapter mAdapter;

    public BlueToothController() {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public BluetoothAdapter getAdapter() {
        return mAdapter;
    }

    /**
     * 是否支持蓝牙
     */
    public boolean isSupportBlueTooth() {
        return mAdapter != null;
    }

    /**
     * 判断蓝牙开启状态
     * true 打开，false 关闭
     */
    public boolean getBlueToothStatus() {
        assert (mAdapter != null);
        return mAdapter.isEnabled();

        //在代码中打开蓝牙，不经过人为操作下打开蓝牙。google不建议使用
        //mAdapter.enable();
        //关闭
        //mAdapter.disable();
    }

    /**
     * 在某个界面中，打开蓝牙
     */
    public void turnOnBlueTooth(Activity activity, int requestCode) {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(intent, requestCode);
        //对应的,在onActivityResult()方法可以知道是否成功打开蓝牙
    }

    /**
     * 打开蓝牙可见性
     * 会弹出询问对话框，是否允许可见
     */
    public void enableVisibility(Context context) {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        context.startActivity(intent);
    }

    /**
     * 查找设备
     */
    public void findDevice() {
        assert (mAdapter != null);
        mAdapter.startDiscovery();
    }

    /**
     * 获取已绑定设备
     */
    public List<BluetoothDevice> getBondedDeviceList() {
        return new ArrayList<>(mAdapter.getBondedDevices());
    }
}
