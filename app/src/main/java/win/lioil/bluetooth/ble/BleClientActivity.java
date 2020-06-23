package win.lioil.bluetooth.ble;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import win.lioil.bluetooth.app.APP;
import win.lioil.bluetooth.R;
import win.lioil.bluetooth.util.AssistStatic;

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
    private BleDevAdapter mBleDevAdapter;
    private BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bleclient);
        RecyclerView rv = findViewById(R.id.rv_ble);
        rv.setLayoutManager(new LinearLayoutManager(this));
        initBlueTooth();
        mBleDevAdapter = new BleDevAdapter(mBluetoothAdapter, new BleDevAdapter.Listener() {
            @Override
            public void onItemClick(BluetoothDevice dev) {
                Intent intent = new Intent(BleClientActivity.this, BleClientDetailActivity.class);
                intent.putExtra(BleClientDetailActivity.EXTRA_TAG, dev);
                startActivity(intent);
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

    public void reScan(View view) {
        if (mBleDevAdapter.isScanning) {
            APP.toast("正在扫描...", 0);
        } else {
            mBleDevAdapter.reScan();
        }
    }
}