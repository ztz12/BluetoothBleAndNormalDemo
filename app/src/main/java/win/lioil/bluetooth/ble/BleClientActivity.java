package win.lioil.bluetooth.ble;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

import win.lioil.bluetooth.APP;
import win.lioil.bluetooth.R;
import win.lioil.bluetooth.util.AssistStatic;
import win.lioil.bluetooth.util.Util;
import win.lioil.bluetooth.util.Utils;

/**
 * BLE客户端(主机/中心设备/Central)
 * 蓝牙4.0 结构
 * BLE是基于GATT实现的，BLE分为三个部分Service、Characteristic、Descriptor，每个部分都拥有不同的 UUID来标识。
 * 一个BLE设备可以拥有多个Service，一个Service可以包含多个Characteristic， 一个Characteristic包含一个Value和多个Descriptor，
 * 一个Descriptor包含一个Value。 通信数据一般存储在Characteristic内，目前一个Characteristic中存储的数据最大为20 byte。
 * 与Characteristic相关的权限字段主要有READ、WRITE、WRITE_NO_RESPONSE、NOTIFY。 Characteristic具有的权限属性可以有一个或者多个。
 * BLE4.0蓝牙发送数据，单次最大传输20个byte,如果是一般的协议命令，如：开关灯、前进左右等等，是不需要
 * 分包的，如果是需要发送如：图片、BIN文档、音乐等大数据量的文件，则必须进行分包发送，BLE库中已经提
 * 供了发送大数据包的接口。
 */
public class BleClientActivity extends Activity {
    private static final String TAG = BleClientActivity.class.getSimpleName();
    private EditText mWriteET;
    private TextView mTips;
    private BleDevAdapter mBleDevAdapter;
    private BluetoothGatt mBluetoothGatt;
    private boolean isConnected = false;
    private BluetoothAdapter mBluetoothAdapter;
    //    private HashMap<String, Map<String, BluetoothGattCharacteristic>> servicesMap = new HashMap<>();
    private BluetoothGattCharacteristic mNotifyCharacteristic1;
    //根据具体硬件进行设置
//    public static String DEVICEA_UUID_SERVICE = "000001801-0000-1000-8000-00805f9b34fb";
//    public static String DEVICEA_UUID_CHARACTERISTIC = "00002a05-0000-1000-8000-00805f9b34fb";
//    //一般不用修改
//    public static String DEVICEA_UUID_DESCRIPTOR = "00002902-0000-1000-8000-00805f9b34fb";

    public Queue<byte[]> dataInfoQueue = new LinkedList<>();
    private StringBuilder mBuilder;
    private final Object locker = new Object();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bleclient);
        RecyclerView rv = findViewById(R.id.rv_ble);
        mWriteET = findViewById(R.id.et_write);
        mTips = findViewById(R.id.tv_tips);
        rv.setLayoutManager(new LinearLayoutManager(this));
        initBlueTooth();
        mBleDevAdapter = new BleDevAdapter(mBluetoothAdapter, new BleDevAdapter.Listener() {
            @Override
            public void onItemClick(BluetoothDevice dev) {
                closeConn();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    //autoContent 是直接连接到远程设备（false）还是直接连接到 远程设备一可用就自动连接（true） BluetoothDevice设置蓝牙传输层模式，报133错误，
                    //也可能传输层问题，设置不同的传输层模式解决
                    mBluetoothGatt = dev.connectGatt(BleClientActivity.this, false, mBluetoothGattCallback, BluetoothDevice.TRANSPORT_BREDR);
                } else {
                    mBluetoothGatt = dev.connectGatt(BleClientActivity.this, false, mBluetoothGattCallback);
                }
                logTv(String.format("与[%s]开始连接............", dev));
            }

            @Override
            public void onScanning() {
                AssistStatic.showToast(BleClientActivity.this, "扫描中");
            }

            @Override
            public void onScannSuccess() {
                AssistStatic.showToast(BleClientActivity.this, "扫描完成");
            }
        });
        rv.setAdapter(mBleDevAdapter);
    }

    private Handler mHandler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            getBlueData();
        }
    };

    /**
     * 获取蓝牙数据
     */
    public void getBlueData() {
        if (dataInfoQueue != null && !dataInfoQueue.isEmpty()) {
            if (dataInfoQueue.peek() != null) {
                //移除并返回队列头部元素
                byte[] bytes = dataInfoQueue.poll();
                for (byte byteChar : bytes) {
                    mBuilder.append(String.format("%d ", byteChar));
                }
            }
            //检测还有数据，继续获取
            if (dataInfoQueue.peek() != null) {
                mHandler.post(runnable);
            }
        }
    }

    // 与服务端连接的Callback
    public BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            BluetoothDevice dev = gatt.getDevice();
            Log.i(TAG, String.format("onConnectionStateChange:%s,%s,%s,%s", dev.getName(), dev.getAddress(), status, newState));
            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
                isConnected = true;
                gatt.discoverServices(); //启动服务发现
            } else {
                isConnected = false;
                closeConn();
            }
            logTv(String.format(status == 0 ? (newState == 2 ? "与[%s]连接成功" : "与[%s]连接断开") : ("与[%s]连接出错,错误码:" + status), dev));
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.i(TAG, String.format("onServicesDiscovered:%s,%s,%s", gatt.getDevice().getName(), gatt.getDevice().getAddress(), status));
            if (status == BluetoothGatt.GATT_SUCCESS) { //BLE服务发现成功
                // 遍历获取BLE服务Services/Characteristics/Descriptors的全部UUID
                for (BluetoothGattService service : gatt.getServices()) {
                    StringBuilder allUUIDs = new StringBuilder();
                    if (Utils.attributes.get(service.getUuid().toString()) != null) {
                        allUUIDs.append("UUIDs={\n").append(Utils.attributes.get(service.getUuid().toString())).append(" Service：\n").append(service.getUuid().toString());
                    } else {
                        allUUIDs.append("UUIDs={\nUnKnow Service：\n").append(service.getUuid().toString());
                    }
                    for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                        if (Utils.attributes.get(characteristic.getUuid().toString()) != null) {
                            allUUIDs.append(",\n").append(Utils.attributes.get(characteristic.getUuid().toString())).append(" Characteristic：\n").append(characteristic.getUuid());
                        } else {
                            allUUIDs.append(",\nUnKnow Characteristic：\n").append(characteristic.getUuid());
                        }
                        for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {
                            if (Utils.attributes.get(descriptor.getUuid().toString()) != null) {
                                allUUIDs.append(",\n").append(Utils.attributes.get(descriptor.getUuid().toString())).append(" Descriptor：\n").append(descriptor.getUuid());
                            } else {
                                allUUIDs.append(",\nUnKnow Descriptor：").append(descriptor.getUuid());
                            }
                        }
                    }
                    allUUIDs.append("}");
                    Log.i(TAG, "onServicesDiscovered:" + allUUIDs.toString());
                    logTv("发现服务" + allUUIDs);
//                    //根据服务来获取每个UUID的值，每个属性都是通过UUID来确定的
//                    String serviceUuid = service.getUuid().toString();
//                    HashMap<String, BluetoothGattCharacteristic> charMap = new HashMap<>();
//                    //获取所有特征服务的集合
//                    List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
//                    //获取服务的所有特征集合
//                    for (int j = 0; j < characteristics.size(); j++) {
//                        charMap.put(characteristics.get(j).getUuid().toString(), characteristics.get(j));
//                    }
//                    servicesMap.put(serviceUuid, charMap);
                }
//                BluetoothGattCharacteristic bluetoothGattCharacteristic = getBluetoothGattCharacteristic(DEVICEA_UUID_SERVICE, DEVICEA_UUID_CHARACTERISTIC);
//                if (bluetoothGattCharacteristic == null) {
//                    return;
//                }
//                enableGattServicesNotification(bluetoothGattCharacteristic);
            }
        }

        /**
         * 读取从设备中传过来的数据
         * @param gatt
         * @param characteristic
         * @param status
         */
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            UUID uuid = characteristic.getUuid();
            byte[] value = characteristic.getValue();
            if (dataInfoQueue != null) {
                dataInfoQueue.clear();
                mBuilder = new StringBuilder();
                dataInfoQueue = Utils.splitPacketFor20Byte(value);
            }
            getBlueData();
            String info = String.format("value: %s%s", "(0x)", Utils.bytes2HexStr(characteristic.getValue()));
            String returnedPacket = mBuilder.toString().replace(" ", "");
            logTv("读取Characteristic[" + uuid + "]:\n" + info);
            Log.i(TAG, String.format("onCharacteristicRead:%s,%s,%s,%s,%s", gatt.getDevice().getName(), gatt.getDevice().getAddress(), uuid, returnedPacket, status));
        }

        /**
         * 向特征设备中写入数据，特征中最多只能存放20个字节，超过需要循环接收或者发送
         * boolean 1个字节 char byte 1个字节 int float 4个字节 long double 8个字节
         * @param gatt
         * @param characteristic
         * @param status
         */
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            synchronized (locker) {
                UUID uuid = characteristic.getUuid();
                String valueStr = new String(characteristic.getValue());
                Log.i(TAG, String.format("onCharacteristicWrite:%s,%s,%s,%s,%s", gatt.getDevice().getName(), gatt.getDevice().getAddress(), uuid, valueStr, status));
                logTv("写入Characteristic[" + uuid + "]:\n" + valueStr);
            }
        }

        /*
         * 蓝牙返回的数据回调
         * 当订阅的Characteristic接收到消息时回调 连接成功回调
         * 当设备上某个特征发送改变的时候就需要通知APP，通过以下方法进行设置通知
         * 一旦接收到通知那么远程设备发生改变的时候就会回调 onCharacteristicChanged
         * when connected successfully will callback this method
         * this method can dealwith send password or data analyze
         * 不能够做耗时操作，否则会出现100%丢包
         * */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            synchronized (locker) {
                UUID uuid = characteristic.getUuid();
                byte[] value = characteristic.getValue();
                if (dataInfoQueue != null) {
                    dataInfoQueue.clear();
                    mBuilder = new StringBuilder();
                    dataInfoQueue = Utils.splitPacketFor20Byte(value);
                }
                getBlueData();
                String info = String.format("value: %s%s", "(0x)", Utils.bytes2HexStr(characteristic.getValue()));
                String returnedPacket = mBuilder.toString().replace(" ", "");
                logTv("通知Characteristic[" + uuid + "]:\n" + info);
            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            if (descriptor != null) {
                UUID uuid = descriptor.getUuid();
                String valueStr = Arrays.toString(descriptor.getValue());
                Log.i(TAG, String.format("onDescriptorRead:%s,%s,%s,%s,%s", gatt.getDevice().getName(), gatt.getDevice().getAddress(), uuid, valueStr, status));
                logTv("读取Descriptor[" + uuid + "]:\n" + valueStr);
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            synchronized (locker) {
                if (descriptor != null) {
                    UUID uuid = descriptor.getUuid();
                    String valueStr = Arrays.toString(descriptor.getValue());
                    Log.i(TAG, String.format("onDescriptorWrite:%s,%s,%s,%s,%s", gatt.getDevice().getName(), gatt.getDevice().getAddress(), uuid, valueStr, status));
                    logTv("写入Descriptor[" + uuid + "]:\n" + valueStr);
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeConn();
    }

    // BLE中心设备连接外围设备的数量有限(大概2~7个)，在建立新连接之前必须释放旧连接资源，否则容易出现连接错误133
    private void closeConn() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }
    }

    // 扫描BLE
    public void reScan(View view) {
        if (mBleDevAdapter.isScanning) {
            APP.toast("正在扫描...", 0);
        } else {
            mBleDevAdapter.reScan();
        }
    }

    // 注意：连续频繁读写数据容易失败，读写操作间隔最好200ms以上，或等待上次回调完成后再进行下次读写操作！
    // 读取数据成功会回调->onCharacteristicChanged()
    public void read(View view) {
        BluetoothGattService service = getGattService(BleServerActivity.UUID_SERVICE);
        if (service != null) {
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(BleServerActivity.UUID_CHAR_READ_NOTIFY);//通过UUID获取可读的Characteristic
            mBluetoothGatt.readCharacteristic(characteristic);
        }
    }

    // 注意：连续频繁读写数据容易失败，读写操作间隔最好200ms以上，或等待上次回调完成后再进行下次读写操作！
    // 写入数据成功会回调->onCharacteristicWrite()
    public void write(View view) {
        BluetoothGattService service = getGattService(BleServerActivity.UUID_SERVICE);
        if (service != null) {
            String text = mWriteET.getText().toString();
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(BleServerActivity.UUID_CHAR_WRITE);//通过UUID获取可写的Characteristic
            if (!TextUtils.isEmpty(text)) {
                characteristic.setValue(text.getBytes()); //单次最多20个字节
                mBluetoothGatt.writeCharacteristic(characteristic);
            }
        }
    }

    public void clear(View view) {
        mTips.setText("");
    }

    // 设置通知Characteristic变化会回调->onCharacteristicChanged()
    public void setNotify(View view) {
        BluetoothGattService service = getGattService(BleServerActivity.UUID_SERVICE);
        if (service != null) {
            // 设置Characteristic通知
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(BleServerActivity.UUID_CHAR_READ_NOTIFY);//通过UUID获取可通知的Characteristic

            if (characteristic.getDescriptors().size() > 0) {
                //Filter descriptors based on the uuid of the descriptor
                List<BluetoothGattDescriptor> descriptors = characteristic.getDescriptors();
                for (BluetoothGattDescriptor descriptor : descriptors) {
                    if (descriptor != null) {
                        //Write the description value
                        if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        } else if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0) {
                            //两个都是通知的意思，notify和indication的区别在于，notify只是将你要发的数据发送给手机，没有确认机制，
                            //不会保证数据发送是否到达。而indication的方式在手机收到数据时会主动回一个ack回来。即有确认机制，只有收
                            //到这个ack你才能继续发送下一个数据。这保证了数据的正确到达，也起到了流控的作用。所以在打开通知的时候，需要设置一下。
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                        }
                        mBluetoothGatt.writeDescriptor(descriptor);
                    }
                }
            }

            //设置通知方式放在处理描述符后面，否则设置通知无法回调 onCharacteristicChanged 方法，放在描述符之前，调用readCharacteristic会直接调用
            //onCharacteristicRead 而不调用onCharacteristicChanged
            final int charaProp = characteristic.getProperties();
            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                // If there is an active notification on a characteristic, clear
                // it first so it doesn't update the data field on the user interface.
                //有活的特征通知，先清除，赋值为空，在重新设置通知获取
                if (mNotifyCharacteristic1 != null) {
                    setCharacteristicNotification(
                            mNotifyCharacteristic1, false);
                    mNotifyCharacteristic1 = null;
                }
                readCharacteristic(characteristic);
            }
            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                mNotifyCharacteristic1 = characteristic;
                setCharacteristicNotification(
                        characteristic, true);
            }

        }
    }

    // 获取Gatt服务
    private BluetoothGattService getGattService(UUID uuid) {
        if (!isConnected) {
            APP.toast("没有连接", 0);
            return null;
        }
        BluetoothGattService service = mBluetoothGatt.getService(uuid);
        if (service == null)
            APP.toast("没有找到服务UUID=" + uuid, 0);
        return service;
    }

    // 输出日志
    private void logTv(final String msg) {
        if (isDestroyed())
            return;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                APP.toast(msg, 0);
                mTips.append(msg + "\n\n");
            }
        });
    }

    /**
     * 初始化蓝牙适配器
     */
    private void initBlueTooth() {
        // Android从4.3开始增加支持BLE技术（即蓝牙4.0及以上版本）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            //从系统服务中获取蓝牙管理器
            BluetoothManager manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (manager != null) {
                mBluetoothAdapter = manager.getAdapter();
            }
        } else {
            //获取系统默认的蓝牙适配器
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
    }

//    /**
//     * 根据服务UUID和特征UUID,获取一个特征{@link BluetoothGattCharacteristic}
//     *
//     * @param serviceUUID   服务UUID
//     * @param characterUUID 特征UUID
//     */
//    private BluetoothGattCharacteristic getBluetoothGattCharacteristic(String serviceUUID, String characterUUID) {
//        if (null == mBluetoothGatt) {
//            Log.e(TAG, "mBluetoothGatt is null");
//            return null;
//        }
//
//        //找服务
//        Map<String, BluetoothGattCharacteristic> bluetoothGattCharacteristicMap = servicesMap.get(serviceUUID);
//        if (null == bluetoothGattCharacteristicMap) {
//            Log.e(TAG, "Not found the serviceUUID!");
//            return null;
//        }
//
//        //找特征
//        Set<Map.Entry<String, BluetoothGattCharacteristic>> entries = bluetoothGattCharacteristicMap.entrySet();
//        BluetoothGattCharacteristic gattCharacteristic = null;
//        for (Map.Entry<String, BluetoothGattCharacteristic> entry : entries) {
//            if (characterUUID.equals(entry.getKey())) {
//                gattCharacteristic = entry.getValue();
//                break;
//            }
//        }
//        return gattCharacteristic;
//    }
//
//
//    private void enableGattServicesNotification(BluetoothGattCharacteristic gattCharacteristic) {
//        if (gattCharacteristic == null) return;
//        setNotify(gattCharacteristic);
//    }
//
//    private void setNotify(BluetoothGattCharacteristic characteristic) {
//
//        final int charaProp = characteristic.getProperties();
//        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
//            // If there is an active notification on a characteristic, clear
//            // it first so it doesn't update the data field on the user interface.
//            //有活的特征通知，先清除，赋值为空，在重新设置通知获取
//            if (mNotifyCharacteristic1 != null) {
//                setCharacteristicNotification(
//                        mNotifyCharacteristic1, false);
//                mNotifyCharacteristic1 = null;
//            }
//            readCharacteristic(characteristic);
//        }
//        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
//            mNotifyCharacteristic1 = characteristic;
//            setCharacteristicNotification(
//                    characteristic, true);
//        }
//    }

    /**
     * 读取特征值，会回调onCharacteristicRead方法
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, " --------- BluetoothAdapter not initialized --------- ");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled        If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, " --------- BluetoothAdapter not initialized --------- ");
            return;
        }
        boolean isSuccess = mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        if (isSuccess) {
            Log.d(TAG, " --------- setCharacteristicNotification --------- Success");
        } else {
            Log.d(TAG, " --------- setCharacteristicNotification --------- Fail");
        }

    }
}