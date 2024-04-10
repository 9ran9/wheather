package com.coolweather.coolweather;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.coolweather.coolweather.db.City;
import com.coolweather.coolweather.db.County;
import com.coolweather.coolweather.db.Province;
import com.coolweather.coolweather.util.HttpUtil;
import com.coolweather.coolweather.util.Utility;

import org.litepal.LitePal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment {
    private static final String TAG = "ChooseAreaFragment";

    //标记得到对应的层级：
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;


    private ProgressDialog progressDialog;
    //进度对话框
    private TextView titleText;
    private ImageButton backButton;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();
    //记录已经加载的数据

    private List<Province> provinceList;//省列表
    private List<City> cityList;//市列表
    private List<County> countyList;//县列表
    private Province selectedProvince;//选中的省份
    private City selectedCity;//选中的城市
    private int currentLevel;//当前选中的级别


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        titleText = view.findViewById(R.id.title_text);
        backButton = view.findViewById(R.id.back_button);
        listView = view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            //表格都能自己响应对应的点击方法，要按钮何用？做个样子？？
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //onItemClick:方法，自带的点击方法，不错不错。
                if (currentLevel == LEVEL_PROVINCE) {
                    selectedProvince = provinceList.get(position);
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    selectedCity = cityList.get(position);
                    queryCounties();
                }else if(currentLevel == LEVEL_COUNTY){ //对应区了
                    String weatherId = countyList.get(position).getWeatherId();//下放到区id
                    if(getActivity() instanceof MainActivity) {
                        Intent intent = new Intent(getActivity(), WeatherActivity.class);//传入对应信息
                        intent.putExtra("weather_id", weatherId);
                        startActivity(intent);
                        getActivity().finish();//关闭当前活动？？
                    }else if(getActivity() instanceof  WeatherActivity) {
                        //直接判断当前所在活动，6啊
                        WeatherActivity activity = (WeatherActivity) getActivity();
                        //我之前没想过，原来活动本来就可以得到，就像之前的下线那一节一样。
                        activity.drawerLayout.closeDrawers();
                        activity.swipeRefreshLayout.setRefreshing(true); //刷新数据
                        activity.requestWeather(weatherId);
                    }
                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLevel == LEVEL_COUNTY) {
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    queryProvinces();
                }
            }
        });
        queryProvinces();
    }

    //这个格式可以变个色，我也不知是干嘛用的：
    /**
     * 查询全国所有的省，优先从数据库查询，如果没有查询到再去服务器上查询。
     */
    private void queryProvinces() {
        titleText.setText("中国");
        backButton.setVisibility(View.GONE);
        provinceList = LitePal.findAll(Province.class);
        //得到数据库里的信息。
        if (provinceList.size() > 0) {
            dataList.clear();  //清掉dataList。
            for (Province province : provinceList) {
                dataList.add(province.getProvinceName()); //添加省的名字进入对应数据表
            }
            adapter.notifyDataSetChanged();  //更新适配器表数据用的，以前还傻傻的
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        } else {
            String address = "http://guolin.tech/api/china";
            queryFromServer(address, "province");
        }
    }

    /**
     * 查询选中省内所有的市，优先从数据库查询，如果没有查询到再去服务器上查询。
     */
    private void queryCities() {
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);//返回按钮可见
        //这个搜索可信？？
        cityList = LitePal.where("provinceid = ?", String.valueOf(selectedProvince.getId()))
                .find(City.class);
        if (cityList.size() > 0) {
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/" + provinceCode;
            queryFromServer(address, "city");
        }
    }

    /**
     * 查询选中市内所有的县，优先从数据库查询，如果没有查询到再去服务器上查询。
     */
    private void queryCounties() {
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList = LitePal.where("cityid = ?", String.valueOf(selectedCity.getId())).find(County.class);
        if (countyList.size() > 0) {
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);  //定位到第一个内容
            currentLevel = LEVEL_COUNTY;
        } else {
            //获得选中的省份，城市的对应id（Code）
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            //这里的网址看情况为：https.  如下写法只有关于对应的网址：
            String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
            queryFromServer(address, "county");//county：每一次都，具体到了区的信息。
        }
    }

    /**
     * 根据传入的地址和类型从服务器上查询省市县数据。
     */
    private void queryFromServer(String address, final String type) {
        showProgressDialog();  //展示进程对话框
        //对应请求调用：
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //onResponse:响应。
                String responseText = response.body().string();
                boolean result = false;
                //数据在这个实用方法中更新进数据库
                if ("province".equals(type)) {
                    result = Utility.handleProvinceResponse(responseText);
                } else if ("city".equals(type)) {
                    result = Utility.handleCityResponse(responseText, selectedProvince.getId());
                } else if ("county".equals(type)) {
                    result = Utility.handleCountyResponse(responseText, selectedCity.getId());
                }
                //合着服务器查到了还是得返回查询方法呗
                if (result) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)) {
                                queryProvinces();
                            } else if ("city".equals(type)) {
                                queryCities();
                            } else if ("county".equals(type)) {
                                queryCounties();
                            }
                        }
                    });
                }
            }

            //失败了...
            @Override
            public void onFailure(Call call, IOException e) {
                // 通过runOnUiThread()方法回到主线程处理逻辑
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /**
     * 显示进度对话框
     */
    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    /**
     * 关闭进度对话框
     */
    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}
