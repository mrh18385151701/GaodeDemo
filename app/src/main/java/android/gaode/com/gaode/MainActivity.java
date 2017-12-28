package android.gaode.com.gaode;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.UiSettings;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements LocationSource ,AMapLocationListener{
    //创建实例
    private MapView mMapView = null;
    //地图对象
    private AMap aMap;
    //定位需求的声明
    private AMapLocationClient mapLocationClient = null;
    private AMapLocationClientOption mapLocationOption = null;
    private OnLocationChangedListener mListener = null;

    //标识。用于判断是否只显示一次定位信息和用户从新定位
    private boolean isFirstLoc = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //获取地图控键
        mMapView = findViewById(R.id.map_view);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，实现地图生命周期管理
        mMapView.onCreate(savedInstanceState);
        //获取地图对象
        aMap = mMapView.getMap();

        //设置显示定位按钮 并且可以点击
        UiSettings settings = aMap.getUiSettings();
        //设置定位监听
        aMap.setLocationSource(this);

        //是否显示定位按钮
        settings.setMyLocationButtonEnabled(true);

        //是否可触发定位显示定位层
        aMap.setMyLocationEnabled(true);

        //开始定位
        initLoc();
    }

    /**
     * 初始化定位
     */
    private void initLoc() {
        //初始化定位
        mapLocationClient = new AMapLocationClient(getApplicationContext());

        //设置定位回掉监听
        mapLocationClient.setLocationListener((AMapLocationListener) this);

        //设置初始化定位参数
        mapLocationOption = new AMapLocationClientOption();

        //设置 定位精度，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mapLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);

        //设置是否返回地址信息
        mapLocationOption.setNeedAddress(true);

        //设置是否只定位一次
        mapLocationOption.setOnceLocation(false);

        //是否默认强制刷新wifi
        mapLocationOption.setWifiActiveScan(true);

        //设置是否允许模拟位置,默认为false，不允许模拟位置
        mapLocationOption.setMockEnable(false);
        //设置定位间隔,单位毫秒,默认为2000ms
        mapLocationOption.setInterval(2000);
        //给定位客户端对象设置定位参数
        mapLocationClient.setLocationOption(mapLocationOption);
        //启动定位
        mapLocationClient.startLocation();

    }


    /**
     * 定义回调函数
     */
    @Override
    public void onLocationChanged(AMapLocation amapLoction) {
        if (amapLoction != null) {
            if (amapLoction.getErrorCode() == 0) {
                //定位成功回调信息，设置相关消息
                amapLoction.getLocationType();//获取当前定位结果来源，如网络定位结果，详见官方定位类型表

                amapLoction.getLatitude();//获取纬度
                amapLoction.getLongitude();//获取精度
                SimpleDateFormat df = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
                //获取定位时间
                Date date = new Date(amapLoction.getTime());
                df.format(date);

                //地址，如果option中设置isNeedAddress为false，则没有此结果，网络定位结果中会有地址信息，GPS定位不返回地址信息。
                amapLoction.getAddress();
                amapLoction.getProvince();
                amapLoction.getCity();
                amapLoction.getDistrict();
                amapLoction.getStreetNum();
                amapLoction.getCityCode();
                amapLoction.getAdCode();

                // 如果不设置标志位，此时再拖动地图时，它会不断将地图移动到当前的位置
                if (isFirstLoc) {

                    //设置缩放级别
                    aMap.moveCamera(CameraUpdateFactory.zoomTo(17));
                    //将地图移到当前位置
                    aMap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(amapLoction.getLatitude(), amapLoction.getLatitude())));

                    //点击定位按钮，能够将地图的中心移动到定位点
                    mListener.onLocationChanged(amapLoction);

                    //添加图钉
                    aMap.addMarker(getMarkerOptions(amapLoction));

                    //获取定位信息
                    StringBuffer buffer=new StringBuffer();
                    buffer.append(amapLoction.getCountry()
                            +""+amapLoction.getProvince()
                            +""+ amapLoction.getCity()
                            +""+amapLoction.getDistrict()
                            +""+amapLoction.getStreet()
                            +""+amapLoction.getStreetNum());
                    Toast.makeText(getApplicationContext(),buffer.toString(),Toast.LENGTH_SHORT).show();
                    isFirstLoc=false;
                }
            }else {
                //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                Log.e("AmapError","location Error,ErrCode"
                +amapLoction.getErrorCode()+",eerInfo:"+amapLoction.getErrorInfo());
                Toast.makeText(getApplicationContext(),"定位失败",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private MarkerOptions getMarkerOptions(AMapLocation amapLoction) {
        //设置图钉选项
        MarkerOptions options = new MarkerOptions();
        //图标
        options.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher));
        //位置
        options.position(new LatLng(amapLoction.getLatitude(), amapLoction.getLongitude()));
        StringBuffer buffer = new StringBuffer();
        buffer.append(amapLoction.getCountry()
                + "" + amapLoction.getProvince()
                + "" + amapLoction.getCity()
                + "" + amapLoction.getDistrict()
                + "" + amapLoction.getStreet()
                + "" + amapLoction.getStreetNum());
        //标题
        options.title(buffer.toString());
        //子标题
        options.snippet("这里好火");
        //设置多少帧刷新一次图片资源
        options.period(60);

        return options;

    }
//    //激活定位
//    @Override
//    public void activate(OnLocationChangedListener listener) {
//        mLister = listener;
//
//    }
//
//    //停止定位
//    @Override
//    public void deactivate() {
//        mLister = null;
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
        mapLocationClient.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause()，实现地图生命周期管理
        mMapView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume()()，实现地图生命周期管理
        mMapView.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {

    }

    @Override
    public void deactivate() {

    }
}
