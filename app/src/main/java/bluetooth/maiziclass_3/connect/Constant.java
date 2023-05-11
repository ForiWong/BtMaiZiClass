package bluetooth.maiziclass_3.connect;

/**
 * 传统蓝牙采用的是SPP（Serial Port Profile 串行端口配置文件）协议进行数据传输。
 * SPP的UUID：00001101-0000-1000-8000-00805F9B34FB。
 * 手机一般以客户端的角色主动连接SPP协议设备。
 *
 * BluetoothAdapter:
 * 本地蓝牙适配器，是所有蓝牙交互的入口，表示蓝牙设备自身的一个蓝牙适配器，整个系统只有一个蓝牙适配器，
 * 通过他可以发现其他蓝牙设备，查询绑定（配对）设备列表，使用MAC地址实例化BluetoothDevice以及创建
 * BluetoothServerSocket用来侦听来自其他设备的通信。
 * myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();//获取默认的蓝牙Adapter
 */
public class Constant {
    public static final String CONNECTTION_UUID = "00001101-0000-1000-8000-00805F9B34FB";

    /**
     * 开始监听
     */
    public static final int MSG_START_LISTENING = 1;

    /**
     * 结束监听
     */
    public static final int MSG_FINISH_LISTENING = 2;

    /**
     * 有客户端连接
     */
    public static final int MSG_GOT_A_CLINET = 3;

    /**
     * 连接到服务器
     */
    public static final int MSG_CONNECTED_TO_SERVER = 4;

    /**
     * 获取到数据
     */
    public static final int MSG_GOT_DATA = 5;

    /**
     * 出错
     */
    public static final int MSG_ERROR = -1;
}
