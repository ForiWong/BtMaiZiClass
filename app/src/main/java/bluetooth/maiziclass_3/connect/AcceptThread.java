package bluetooth.maiziclass_3.connect;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import java.io.IOException;
import java.util.UUID;

/**
 * 处理接收连接
 */
public class AcceptThread extends Thread {
    private static final String NAME = "BlueToothClass";
    private static final UUID MY_UUID = UUID.fromString(Constant.CONNECTTION_UUID);//这是个常量，就是这个

    private final BluetoothServerSocket mmServerSocket;
    private final BluetoothAdapter mBluetoothAdapter;
    private final Handler mHandler;
    private ConnectedThread mConnectedThread;//新起一个线程来作通信，已连接的通信

    public AcceptThread(BluetoothAdapter adapter, Handler handler) {
        // 使用一个临时对象，该对象稍后被分配给mmServerSocket，因为mmServerSocket是最终的
        mBluetoothAdapter = adapter;
        mHandler = handler;
        BluetoothServerSocket tmp = null;
        try {
            //MY_UUID是应用程序的UUID，客户端代码使用相同的UUID Rfcomm 串口仿真协议
            //创建一个服务端的socket
            tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
        } catch (IOException e) {
        }
        mmServerSocket = tmp;
    }

    //这个线程的作用就是，等待连接，然后接受它，之后开启一个新的线程进行通信
    public void run() {
        BluetoothSocket socket;
        //持续监听，直到出现异常或返回socket
        while (true) {
            try {
                mHandler.sendEmptyMessage(Constant.MSG_START_LISTENING);
                socket = mmServerSocket.accept();//
            } catch (IOException e) {
                mHandler.sendMessage(mHandler.obtainMessage(Constant.MSG_ERROR, e));
                break;
            }
            // 如果一个连接被接受
            if (socket != null) {
                // 在单独的线程中完成管理连接的工作
                manageConnectedSocket(socket);
                try {
                    mmServerSocket.close();
                    mHandler.sendEmptyMessage(Constant.MSG_FINISH_LISTENING);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    private void manageConnectedSocket(BluetoothSocket socket) {
        //只支持同时处理一个连接
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
        }
        mHandler.sendEmptyMessage(Constant.MSG_GOT_A_CLINET);
        mConnectedThread = new ConnectedThread(socket, mHandler);
        mConnectedThread.start();
    }

    /**
     * 取消监听socket，使此线程关闭
     */
    public void cancel() {
        try {
            mmServerSocket.close();
            mHandler.sendEmptyMessage(Constant.MSG_FINISH_LISTENING);
        } catch (IOException e) {
        }
    }

    public void sendData(byte[] data) {
        if (mConnectedThread != null) {
            mConnectedThread.write(data);
        }
    }
}