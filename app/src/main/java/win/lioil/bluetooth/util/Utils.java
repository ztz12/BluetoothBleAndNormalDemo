package win.lioil.bluetooth.util;

import android.util.Log;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class Utils {
    public static byte[] hexStringToByteArray(String s) {
        if (s == null) return null;
        if (s.trim().length() == 1) {
            s = "0" + s;
        }
        if (s.trim().length() == 3) {
            s = "0" + s;
        }
        String dataString = s.replace(" ", "");
        int len = dataString.length();
        byte[] data = new byte[len / 2];

        if (len == 1) {
            data = s.getBytes();
        } else {
            for (int i = 0; i < len; i += 2) {
                data[i / 2] = (byte) ((Character.digit(dataString.charAt(i), 16) << 4) + Character.digit(dataString.charAt(i + 1), 16));
            }
        }
        return data;
    }

    public static int getLengthFromToken(byte[] bytes) {
        if (bytes.length >= 3) {
            return (bytes[1] << 8) | (bytes[2] & 0xff);
        }
        return 0;
    }


    //数据分包处理
    public static Queue<byte[]> splitPacketFor20Byte(byte[] data) {
        Queue<byte[]> dataInfoQueue = new LinkedList<>();
        if (data != null) {
            int index = 0;
            do {
                byte[] surplusData = new byte[data.length - index];
                byte[] currentData;
                System.arraycopy(data, index, surplusData, 0, data.length - index);
                if (surplusData.length <= 20) {
                    currentData = new byte[surplusData.length];
                    System.arraycopy(surplusData, 0, currentData, 0, surplusData.length);
                    index += surplusData.length;
                } else {
                    currentData = new byte[20];
                    System.arraycopy(data, index, currentData, 0, 20);
                    index += 20;
                }
                Log.e("mcy_分包数据", "" + Arrays.toString(currentData));
                dataInfoQueue.offer(currentData);
            } while (index < data.length);
        }
        return dataInfoQueue;
    }

    private static final double A_Value = 60; // A - 发射端和接收端相隔1米时的信号强度
    private static final double n_Value = 2.0; //  n - 环境衰减因子

    public static double getDistance(int rssi) { //根据Rssi获得返回的距离,返回数据单位为m
        int iRssi = Math.abs(rssi);
        double power = (iRssi - A_Value) / (10 * n_Value);
        return Math.pow(10, power);
    }

    public static Map<String, String> attributes = new HashMap<String, String>();

    static {
        attributes.put("0000180d-0000-1000-8000-00805f9b34fb", "Heart Rate Service");
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");
        attributes.put("00002a37-0000-1000-8000-00805f9b34fb", "Heart Rate Measurement");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
        attributes.put("00001800-0000-1000-8000-00805f9b34fb", "GenericAccess");
        attributes.put("00001801-0000-1000-8000-00805f9b34fb", "GenericAttribute");
        attributes.put("00002800-0000-1000-8000-00805f9b34fb", "Primary Service");
        attributes.put("00002801-0000-1000-8000-00805f9b34fb", "Secondary Service");
        attributes.put("00002802-0000-1000-8000-00805f9b34fb", "Include");
        attributes.put("00002803-0000-1000-8000-00805f9b34fb", "Characteristic");
        attributes.put("00002900-0000-1000-8000-00805f9b34fb", "Characteristic Extended Properties");
        attributes.put("00002901-0000-1000-8000-00805f9b34fb", "Characteristic User Description");
        attributes.put("00002902-0000-1000-8000-00805f9b34fb", "Client Characteristic Configuration");
        attributes.put("00002903-0000-1000-8000-00805f9b34fb", "Server Characteristic Configuration");
        attributes.put("00002904-0000-1000-8000-00805f9b34fb", "Characteristic Presentation Format");
        attributes.put("00002905-0000-1000-8000-00805f9b34fb", "Characteristic Aggregate Format");
        attributes.put("00002906-0000-1000-8000-00805f9b34fb", "Valid Range");
        attributes.put("00002907-0000-1000-8000-00805f9b34fb", "External Report Reference Descriptor");
        attributes.put("00002908-0000-1000-8000-00805f9b34fb", "Report Reference Descriptor");
        attributes.put("00002a00-0000-1000-8000-00805f9b34fb", "Device Name");
        attributes.put("00002a01-0000-1000-8000-00805f9b34fb", "Appearance");
        attributes.put("00002a02-0000-1000-8000-00805f9b34fb", "Peripheral Privacy Flag");
        attributes.put("00002a03-0000-1000-8000-00805f9b34fb", "Reconnection Address");
        attributes.put("00002a04-0000-1000-8000-00805f9b34fb", "PPCP");
        attributes.put("00002a05-0000-1000-8000-00805f9b34fb", "Service Changed");
        attributes.put("00001802-0000-1000-8000-00805f9b34fb", "Immediate Alert");
        attributes.put("00001803-0000-1000-8000-00805f9b34fb", "Link Loss");
        attributes.put("00001804-0000-1000-8000-00805f9b34fb", "Tx Power");
        attributes.put("00001805-0000-1000-8000-00805f9b34fb", "Current Time Service");
        attributes.put("00001806-0000-1000-8000-00805f9b34fb", "Reference Time Update Service");
        attributes.put("00001807-0000-1000-8000-00805f9b34fb", "Next DST Change Service");
        attributes.put("00001808-0000-1000-8000-00805f9b34fb", "Glucose");
        attributes.put("00001809-0000-1000-8000-00805f9b34fb", "Health Thermometer");
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information");
        attributes.put("0000180b-0000-1000-8000-00805f9b34fb", "Network Availability");
        attributes.put("0000180d-0000-1000-8000-00805f9b34fb", "Heart Rate");
        attributes.put("0000180e-0000-1000-8000-00805f9b34fb", "Phone Alert Status Service");
        attributes.put("0000180f-0000-1000-8000-00805f9b34fb", "Battery Service");
        attributes.put("00001810-0000-1000-8000-00805f9b34fb", "Blood Pressure");
        attributes.put("00001811-0000-1000-8000-00805f9b34fb", "Alert Notification Service");
        attributes.put("00001812-0000-1000-8000-00805f9b34fb", "Human Interface Device");
        attributes.put("00001813-0000-1000-8000-00805f9b34fb", "Scan Parameters");
        attributes.put("00001814-0000-1000-8000-00805f9b34fb", "Running Speed and Cadence");
        attributes.put("00001816-0000-1000-8000-00805f9b34fb", "Cycling Speed and Cadence");
        attributes.put("00001818-0000-1000-8000-00805f9b34fb", "Cycling Power");
        attributes.put("00001819-0000-1000-8000-00805f9b34fb", "Location and Navigation");
        attributes.put("00002700-0000-1000-8000-00805f9b34fb", "GATT_UNITLESS");
        attributes.put("00002701-0000-1000-8000-00805f9b34fb", "GATT_UNIT_LENGTH_METER");
        attributes.put("00002702-0000-1000-8000-00805f9b34fb", "GATT_UNIT_MASS_KGRAM");
        attributes.put("00002703-0000-1000-8000-00805f9b34fb", "GATT_UNIT_TIME_SECOND");
        attributes.put("00002704-0000-1000-8000-00805f9b34fb", "GATT_UNIT_ELECTRIC_CURRENT_A");
        attributes.put("00002705-0000-1000-8000-00805f9b34fb", "GATT_UNIT_THERMODYNAMIC_TEMP_K");
        attributes.put("00002706-0000-1000-8000-00805f9b34fb", "GATT_UNIT_AMOUNT_SUBSTANCE_M");
        attributes.put("00002707-0000-1000-8000-00805f9b34fb", "GATT_UNIT_LUMINOUS_INTENSITY_C");
        attributes.put("00002710-0000-1000-8000-00805f9b34fb", "GATT_UNIT_AREA_SQ_MTR");
        attributes.put("00002711-0000-1000-8000-00805f9b34fb", "GATT_UNIT_VOLUME_CUBIC_MTR");
        attributes.put("00002712-0000-1000-8000-00805f9b34fb", "GATT_UNIT_VELOCITY_MPS");
        attributes.put("00002713-0000-1000-8000-00805f9b34fb", "GATT_UNIT_ACCELERATION_MPS_SQ");
        attributes.put("00002714-0000-1000-8000-00805f9b34fb", "GATT_UNIT_WAVENUMBER_RM");
        attributes.put("00002715-0000-1000-8000-00805f9b34fb", "GATT_UNIT_DENSITY_KGPCM");
        attributes.put("00002716-0000-1000-8000-00805f9b34fb", "GATT_UNIT_SURFACE_DENSITY_KGPSM");
        attributes.put("00002717-0000-1000-8000-00805f9b34fb", "GATT_UNIT_SPECIFIC_VOLUME_CMPKG");
        attributes.put("00002718-0000-1000-8000-00805f9b34fb", "GATT_UNIT_CURRENT_DENSITY_APSM");
        attributes.put("00002719-0000-1000-8000-00805f9b34fb", "GATT_UNIT_MAGNETIC_FIELD_STRENGTH");
        attributes.put("0000271a-0000-1000-8000-00805f9b34fb", "GATT_UNIT_AMOUNT_CONCENTRATE_MPCM");
        attributes.put("0000271b-0000-1000-8000-00805f9b34fb", "GATT_UNIT_MASS_CONCENTRATE_KGPCM");
        attributes.put("0000271d-0000-1000-8000-00805f9b34fb", "GATT_UNIT_LUMINANCE_CPSM");
        attributes.put("0000271d-0000-1000-8000-00805f9b34fb", "GATT_UNIT_REFRACTIVE_INDEX");
        attributes.put("0000271e-0000-1000-8000-00805f9b34fb", "GATT_UNIT_RELATIVE_PERMEABLILTY");
        attributes.put("00002720-0000-1000-8000-00805f9b34fb", "GATT_UNIT_PLANE_ANGLE_RAD");
        attributes.put("00002721-0000-1000-8000-00805f9b34fb", "GATT_UNIT_SOLID_ANGLE_STERAD");
        attributes.put("00002722-0000-1000-8000-00805f9b34fb", "GATT_UNIT_FREQUENCY_HTZ");
        attributes.put("00002723-0000-1000-8000-00805f9b34fb", "GATT_UNIT_FORCE_NEWTON");
        attributes.put("00002724-0000-1000-8000-00805f9b34fb", "GATT_UNIT_PRESSURE_PASCAL");
        attributes.put("00002725-0000-1000-8000-00805f9b34fb", "GATT_UNIT_ENERGY_JOULE");
        attributes.put("00002726-0000-1000-8000-00805f9b34fb", "GATT_UNIT_POWER_WATT");
        attributes.put("00002727-0000-1000-8000-00805f9b34fb", "GATT_UNIT_ELECTRIC_CHARGE_C");
        attributes.put("00002728-0000-1000-8000-00805f9b34fb", "GATT_UNIT_ELECTRIC_POTENTIAL_DIF_V");
        attributes.put("0000272f-0000-1000-8000-00805f9b34fb", "GATT_UNIT_CELSIUS_TEMP_DC");
        attributes.put("00002760-0000-1000-8000-00805f9b34fb", "GATT_UNIT_TIME_MINUTE");
        attributes.put("00002761-0000-1000-8000-00805f9b34fb", "GATT_UNIT_TIME_HOUR");
        attributes.put("00002762-0000-1000-8000-00805f9b34fb", "GATT_UNIT_TIME_DAY");
        attributes.put("00002763-0000-1000-8000-00805f9b34fb", "GATT_UNIT_PLANE_ANGLE_DEGREE");
        attributes.put("00002764-0000-1000-8000-00805f9b34fb", "GATT_UNIT_PLANE_ANGLE_MINUTE");
        attributes.put("00002765-0000-1000-8000-00805f9b34fb", "GATT_UNIT_PLANE_ANGLE_SECOND");
        attributes.put("00002766-0000-1000-8000-00805f9b34fb", "GATT_UNIT_AREA_HECTARE");
        attributes.put("00002767-0000-1000-8000-00805f9b34fb", "GATT_UNIT_VOLUME_LITRE");
        attributes.put("00002768-0000-1000-8000-00805f9b34fb", "GATT_UNIT_MASS_TONNE");
        attributes.put("000027a0-0000-1000-8000-00805f9b34fb", "GATT_UINT_LENGTH_YARD");
        attributes.put("000027a1-0000-1000-8000-00805f9b34fb", "GATT_UNIT_LENGTH_PARSEC");
        attributes.put("000027a2-0000-1000-8000-00805f9b34fb", "GATT_UNIT_LENGTH_INCH");
        attributes.put("000027a3-0000-1000-8000-00805f9b34fb", "GATT_UNIT_LENGTH_FOOT");
        attributes.put("000027a4-0000-1000-8000-00805f9b34fb", "GATT_UNIT_LENGTH_MILE");
        attributes.put("000027a5-0000-1000-8000-00805f9b34fb", "GATT_UNIT_PRESSURE_PFPSI");
        attributes.put("000027a6-0000-1000-8000-00805f9b34fb", "GATT_UNIT_VELOCITY_KMPH");
        attributes.put("000027a7-0000-1000-8000-00805f9b34fb", "GATT_UNIT_VELOCITY_MPH");
        attributes.put("000027a8-0000-1000-8000-00805f9b34fb", "GATT_UNIT_ANGULAR_VELOCITY_RPM");
        attributes.put("000027a9-0000-1000-8000-00805f9b34fb", "GATT_UNIT_ENERGY_GCAL");
        attributes.put("000027aa-0000-1000-8000-00805f9b34fb", "GATT_UNIT_ENERGY_KCAL");
        attributes.put("000027ab-0000-1000-8000-00805f9b34fb", "GATT_UNIT_ENERGY_KWH");
        attributes.put("000027ac-0000-1000-8000-00805f9b34fb", "GATT_UNIT_THERMODYNAMIC_TEMP_DF");
        attributes.put("000027ad-0000-1000-8000-00805f9b34fb", "GATT_UNIT_PERCENTAGE");
        attributes.put("000027ae-0000-1000-8000-00805f9b34fb", "GATT_UNIT_PER_MILE");
        attributes.put("000027af-0000-1000-8000-00805f9b34fb", "GATT_UNIT_PERIOD_BPM");
        attributes.put("000027b0-0000-1000-8000-00805f9b34fb", "GATT_UNIT_ELECTRIC_CHARGE_AH");
        attributes.put("000027b1-0000-1000-8000-00805f9b34fb", "GATT_UNIT_MASS_DENSITY_MGPD");
        attributes.put("000027b2-0000-1000-8000-00805f9b34fb", "GATT_UNIT_MASS_DENSITY_MMPL");
        attributes.put("000027b3-0000-1000-8000-00805f9b34fb", "GATT_UNIT_TIME_YEAR");
        attributes.put("000027b4-0000-1000-8000-00805f9b34fb", "GATT_UNIT_TIME_MONTH");
        attributes.put("00002a06-0000-1000-8000-00805f9b34fb", "Alert Level");
        attributes.put("00002a07-0000-1000-8000-00805f9b34fb", "Tx Power Level");
        attributes.put("00002a08-0000-1000-8000-00805f9b34fb", "Date Time");
        attributes.put("00002a09-0000-1000-8000-00805f9b34fb", "Day of Week");
        attributes.put("00002a0a-0000-1000-8000-00805f9b34fb", "Day Date Time");
        attributes.put("00002a0c-0000-1000-8000-00805f9b34fb", "Exact Time 256");
        attributes.put("00002a0d-0000-1000-8000-00805f9b34fb", "DST Offset");
        attributes.put("00002a0e-0000-1000-8000-00805f9b34fb", "Time Zone");
        attributes.put("00002a0f-0000-1000-8000-00805f9b34fb", "Local Time Information");
        attributes.put("00002a11-0000-1000-8000-00805f9b34fb", "Time with DST");
        attributes.put("00002a12-0000-1000-8000-00805f9b34fb", "Time Accuracy");
        attributes.put("00002a13-0000-1000-8000-00805f9b34fb", "Time Source");
        attributes.put("00002a14-0000-1000-8000-00805f9b34fb", "Reference Time Information");
        attributes.put("00002a16-0000-1000-8000-00805f9b34fb", "Time Update Control Point");
        attributes.put("00002a17-0000-1000-8000-00805f9b34fb", "Time Update State");
        attributes.put("00002a18-0000-1000-8000-00805f9b34fb", "Glucose Measurement");
        attributes.put("00002a19-0000-1000-8000-00805f9b34fb", "Battery Level");
        attributes.put("00002a1c-0000-1000-8000-00805f9b34fb", "Temperature Measurement");
        attributes.put("00002a1d-0000-1000-8000-00805f9b34fb", "Temperature Type");
        attributes.put("00002a1e-0000-1000-8000-00805f9b34fb", "Intermediate Temperature");
        attributes.put("00002a21-0000-1000-8000-00805f9b34fb", "Measurement Interval");
        attributes.put("00002a22-0000-1000-8000-00805f9b34fb", "Boot Keyboard Input Report");
        attributes.put("00002a23-0000-1000-8000-00805f9b34fb", "System ID");
        attributes.put("00002a24-0000-1000-8000-00805f9b34fb", "Model Number String");
        attributes.put("00002a25-0000-1000-8000-00805f9b34fb", "Serial Number String");
        attributes.put("00002a26-0000-1000-8000-00805f9b34fb", "Firmware Revision String");
        attributes.put("00002a27-0000-1000-8000-00805f9b34fb", "Hardware Revision String");
        attributes.put("00002a28-0000-1000-8000-00805f9b34fb", "Software Revision String");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
        attributes.put("00002a2a-0000-1000-8000-00805f9b34fb", "IEEE 11073-20601 Regulatory Certification Data List");
        attributes.put("00002a2b-0000-1000-8000-00805f9b34fb", "Current Time");
        attributes.put("00002a31-0000-1000-8000-00805f9b34fb", "Scan Refresh");
        attributes.put("00002a32-0000-1000-8000-00805f9b34fb", "Boot Keyboard Output Report");
        attributes.put("00002a33-0000-1000-8000-00805f9b34fb", "Boot Mouse Input Report");
        attributes.put("00002a34-0000-1000-8000-00805f9b34fb", "Glucose Measurement Context");
        attributes.put("00002a35-0000-1000-8000-00805f9b34fb", "Blood Pressure Measurement");
        attributes.put("00002a36-0000-1000-8000-00805f9b34fb", "Intermediate Cuff Pressure");
        attributes.put("00002a37-0000-1000-8000-00805f9b34fb", "Heart Rate Measurement");
        attributes.put("00002a38-0000-1000-8000-00805f9b34fb", "Body Sensor Location");
        attributes.put("00002a39-0000-1000-8000-00805f9b34fb", "Heart Rate Control Point");
        attributes.put("00002a3e-0000-1000-8000-00805f9b34fb", "Network Availability");
        attributes.put("00002a3f-0000-1000-8000-00805f9b34fb", "Alert Status");
        attributes.put("00002a40-0000-1000-8000-00805f9b34fb", "Ringer Control Point");
        attributes.put("00002a41-0000-1000-8000-00805f9b34fb", "Ringer Setting");
        attributes.put("00002a42-0000-1000-8000-00805f9b34fb", "Alert Category ID Bit Mask");
        attributes.put("00002a43-0000-1000-8000-00805f9b34fb", "Alert Category ID");
        attributes.put("00002a44-0000-1000-8000-00805f9b34fb", "Alert Notification Control Point");
        attributes.put("00002a45-0000-1000-8000-00805f9b34fb", "Unread Alert Status");
        attributes.put("00002a46-0000-1000-8000-00805f9b34fb", "New Alert");
        attributes.put("00002a47-0000-1000-8000-00805f9b34fb", "Supported New Alert Category");
        attributes.put("00002a48-0000-1000-8000-00805f9b34fb", "Supported Unread Alert Category");
        attributes.put("00002a49-0000-1000-8000-00805f9b34fb", "Blood Pressure Feature");
        attributes.put("00002a4a-0000-1000-8000-00805f9b34fb", "HID Information");
        attributes.put("00002a4b-0000-1000-8000-00805f9b34fb", "Report Map");
        attributes.put("00002a4c-0000-1000-8000-00805f9b34fb", "HID Control Point");
        attributes.put("00002a4d-0000-1000-8000-00805f9b34fb", "Report");
        attributes.put("00002a4e-0000-1000-8000-00805f9b34fb", "Protocol Mode");
        attributes.put("00002a4f-0000-1000-8000-00805f9b34fb", "Scan Interval Window");
        attributes.put("00002a50-0000-1000-8000-00805f9b34fb", "PnP ID");
        attributes.put("00002a51-0000-1000-8000-00805f9b34fb", "Glucose Feature");
        attributes.put("00002a52-0000-1000-8000-00805f9b34fb", "Record Access Control Point");
        attributes.put("00002a53-0000-1000-8000-00805f9b34fb", "RSC Measurement");
        attributes.put("00002a54-0000-1000-8000-00805f9b34fb", "RSC Feature");
        attributes.put("00002a55-0000-1000-8000-00805f9b34fb", "SC Control Point");
        attributes.put("00002a5b-0000-1000-8000-00805f9b34fb", "CSC Measurement");
        attributes.put("00002a5c-0000-1000-8000-00805f9b34fb", "CSC Feature");
        attributes.put("00002a5d-0000-1000-8000-00805f9b34fb", "Sensor Location");
        attributes.put("00002a63-0000-1000-8000-00805f9b34fb", "Cycling Power Measurement");
        attributes.put("00002a64-0000-1000-8000-00805f9b34fb", "Cycling Power Vector");
        attributes.put("00002a65-0000-1000-8000-00805f9b34fb", "Cycling Power Feature");
        attributes.put("00002a66-0000-1000-8000-00805f9b34fb", "Cycling Power Control Point");
        attributes.put("00002a67-0000-1000-8000-00805f9b34fb", "Location and Speed");
        attributes.put("00002a68-0000-1000-8000-00805f9b34fb", "Navigation");
        attributes.put("00002a69-0000-1000-8000-00805f9b34fb", "Position Quality");
        attributes.put("00002a6a-0000-1000-8000-00805f9b34fb", "LN Feature");
        attributes.put("00002a6b-0000-1000-8000-00805f9b34fb", "LN Control Point");
        attributes.put("0000aa00-0000-1000-8000-00805f9b34fb", "IRTEMPERATURE_SERV");
        attributes.put("0000aa01-0000-1000-8000-00805f9b34fb", "IRTEMPERATURE_DATA");
        attributes.put("0000aa02-0000-1000-8000-00805f9b34fb", "IRTEMPERATURE_CONF");
        attributes.put("0000aa10-0000-1000-8000-00805f9b34fb", "ACCELEROMETER_SERV");
        attributes.put("0000aa11-0000-1000-8000-00805f9b34fb", "ACCELEROMETER_DATA");
        attributes.put("0000aa12-0000-1000-8000-00805f9b34fb", "ACCELEROMETER_CONF");
        attributes.put("0000aa13-0000-1000-8000-00805f9b34fb", "ACCELEROMETER_PERI");
        attributes.put("0000aa30-0000-1000-8000-00805f9b34fb", "MAGNETOMETER_SERV");
        attributes.put("0000aa31-0000-1000-8000-00805f9b34fb", "MAGNETOMETER_DATA");
        attributes.put("0000aa32-0000-1000-8000-00805f9b34fb", "MAGNETOMETER_CONF");
        attributes.put("0000aa33-0000-1000-8000-00805f9b34fb", "MAGNETOMETER_PERI");
        attributes.put("0000aa40-0000-1000-8000-00805f9b34fb", "BAROMETER_SERV");
        attributes.put("0000aa41-0000-1000-8000-00805f9b34fb", "BAROMETER_DATA");
        attributes.put("0000aa42-0000-1000-8000-00805f9b34fb", "BAROMETER_CONF");
        attributes.put("0000aa43-0000-1000-8000-00805f9b34fb", "BAROMETER_CALI");
        attributes.put("0000aa50-0000-1000-8000-00805f9b34fb", "GYROSCOPE_SERV");
        attributes.put("0000aa51-0000-1000-8000-00805f9b34fb", "GYROSCOPE_DATA");
        attributes.put("0000aa52-0000-1000-8000-00805f9b34fb", "GYROSCOPE_CONF");
        attributes.put("0000aa60-0000-1000-8000-00805f9b34fb", "TEST_SERV");
        attributes.put("0000aa61-0000-1000-8000-00805f9b34fb", "TEST_DATA");
        attributes.put("0000aa62-0000-1000-8000-00805f9b34fb", "TEST_CONF");
        attributes.put("0000ffe0-0000-1000-8000-00805f9b34fb", "SK Service");
        attributes.put("0000ffe1-0000-1000-8000-00805f9b34fb", "SK_KEYPRESSED");
        attributes.put("0000ffa0-0000-1000-8000-00805f9b34fb", "Accelerometer Service");
        attributes.put("0000ffa1-0000-1000-8000-00805f9b34fb", "ACCEL_ENABLER");
        attributes.put("0000ffa2-0000-1000-8000-00805f9b34fb", "ACCEL_RANGE");
        attributes.put("0000ffa3-0000-1000-8000-00805f9b34fb", "ACCEL_X");
        attributes.put("0000ffa4-0000-1000-8000-00805f9b34fb", "ACCEL_Y");
        attributes.put("0000ffa5-0000-1000-8000-00805f9b34fb", "ACCEL_Z");
    }

}
