package com.example.hiephoangvan.weather.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hiephoangvan.weather.R;
import com.example.hiephoangvan.weather.Utils.Config;
import com.example.hiephoangvan.weather.Utils.UtilDate;
import com.example.hiephoangvan.weather.Utils.UtilPref;
import com.example.hiephoangvan.weather.adapters.HourlyAdapter;
import com.example.hiephoangvan.weather.api.RetrofitInstance;
import com.example.hiephoangvan.weather.api.Service;
import com.example.hiephoangvan.weather.models.CurrentlyWeather;
import com.example.hiephoangvan.weather.models.HourlyWeather;
import com.example.hiephoangvan.weather.models.Lists;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static com.example.hiephoangvan.weather.Utils.Config.FORMAT_1;

public class FragmentHourly extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    @BindView(R.id.tv_currently_temp) TextView mCurrentTemp;
    @BindView(R.id.image_weather) ImageView mCurrentImageWeather;
    @BindView(R.id.tv_description) TextView mCurrentDescription;
    @BindView(R.id.tv_currently_time) TextView mCurrentTime;
    @BindView(R.id.recyclerViewHourly) RecyclerView mRecyclerView;
    @BindView(R.id.refreshlayoutHourly) SwipeRefreshLayout mRefreshLayout;
    private HourlyAdapter mHourlyAdapter;
    private List<Lists> list = new ArrayList<>();
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hourly,container,false);
        ButterKnife.bind(this,view);
        mHourlyAdapter = new HourlyAdapter(list,getContext());
        mRecyclerView.setAdapter(mHourlyAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));
        mRecyclerView.setNestedScrollingEnabled(false);
        getCurrentWeather();
        getHourlyWeather();
        mRefreshLayout.setOnRefreshListener(this::onRefresh);
        return view;
    }

    public void getCurrentWeather(){
        Service service = RetrofitInstance.getRetrofitInstance().create(Service.class);
        Map<String, String> data = new HashMap<>();
        data.put("lat",UtilPref.getInstance().getFloat("lat",0)+"");
        data.put("lon",UtilPref.getInstance().getFloat("lon",0)+"");
        data.put("units",UtilPref.getInstance().getString("unit","metric"));
        data.put("lang","vi");
        data.put("APPID",Config.API_KEY);
        Observable<CurrentlyWeather> observable = service.getCurrentWeather(data);
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response->updateView(response),
                        error->Toast.makeText(this.getContext(),error.getLocalizedMessage(),Toast.LENGTH_SHORT).show());
    }

    public void getHourlyWeather(){
        Service service = RetrofitInstance.getRetrofitInstance().create(Service.class);
        Map<String, String> data = new HashMap<>();
        data.put("lat",UtilPref.getInstance().getFloat("lat",0)+"");
        data.put("lon",UtilPref.getInstance().getFloat("lon",0)+"");
        data.put("units",UtilPref.getInstance().getString("unit","metric"));
        data.put("lang","vi");
        data.put("APPID",Config.API_KEY);
        Observable<HourlyWeather> observable = service.getHourlyWeather(data);
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response->updateList(response),
                        error->Toast.makeText(this.getContext(),error.getLocalizedMessage(),Toast.LENGTH_LONG).show());

    }

    public void updateView(CurrentlyWeather currentlyWeather){
        mCurrentTemp.setText(Math.round(currentlyWeather.getMain().getTemp())+"");
        mCurrentImageWeather.setImageDrawable(getDrawable(getContext(),"ic_"+currentlyWeather.getWeather().get(0).getIcon()));
        StringBuilder sb = new StringBuilder(currentlyWeather.getWeather().get(0).getDescription());
        sb.setCharAt(0,Character.toUpperCase(sb.charAt(0)));
        mCurrentDescription.setText(sb.toString());
        mCurrentTime.setText("Cập nhật gần nhất: "+UtilDate.getInstance().getDateFormat(FORMAT_1)
                .format(new Date(currentlyWeather.getDt()*1000L)));
        }

    public Drawable getDrawable(Context context, String name){
        Resources resources = context.getResources();
        final int resourceId = resources.getIdentifier(name, "drawable",
                context.getPackageName());
        Drawable drawable = resources.getDrawable(resourceId);
        return drawable;
    }

    public void updateList(HourlyWeather hourlyWeather) {
        this.list.clear();
        this.list.addAll(hourlyWeather.getList());
        this.mHourlyAdapter = new HourlyAdapter(this.list,getContext());
        mRecyclerView.setAdapter(mHourlyAdapter);
    }

    @Override
    public void onRefresh() {
        mRefreshLayout.setRefreshing(true);
        getCurrentWeather();
        getHourlyWeather();
        mRefreshLayout.setRefreshing(false);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        onRefresh();
    }
}
