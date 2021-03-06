package com.myappartments.laundry.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.myappartments.laundry.R;
import com.myappartments.laundry.activity.MainActivity;
import com.myappartments.laundry.adapter.AdapterSubCat;
import com.myappartments.laundry.adapter.AdapterBanner;
import com.myappartments.laundry.api.Api;
import com.myappartments.laundry.api.ApiClient;
import com.myappartments.laundry.model.ModelBanner;
import com.myappartments.laundry.model.ModelSubCat;
import com.myappartments.laundry.storage.SharedPrefManager;
import com.myappartments.laundry.utils.Constant;
import com.myappartments.laundry.utils.CustomLog;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentSubCat extends Fragment {

    private SharedPrefManager tSharedPrefManager;
    private Context tContext;
    private int currentPage = 0;
    private Timer tTimer;
    private long DELAY_MS = 1500;
    private long PERIOD_MS = 2500;
    private List<ModelBanner> tModes;
    private List<ModelSubCat> tModelSubCats;
    private AdapterSubCat tAdapterSubCat;
    private FragmentManager tFragmentManager;
    private MainActivity tActivity;
    @BindView(R.id.vp_water)
    protected ViewPager vpWater;
    @BindView(R.id.rv_water)
    protected RecyclerView rvWater;

    private String mainCatId;
    public static FragmentSubCat newInstance(String mainCatId) {


        FragmentSubCat fragment = new FragmentSubCat();
        fragment.mainCatId = mainCatId;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_main_water, container, false);
        ButterKnife.bind(this, view);
        setTitle();
        setSlider();
        tFragmentManager = getFragmentManager();
        RecyclerView.LayoutManager tLayoutManager = new GridLayoutManager(getContext(), 2);
        rvWater.setLayoutManager(tLayoutManager);
        callApiSubCat();
        return view;
    }

    private void setTitle(){
        tContext = getContext();
        tSharedPrefManager = new SharedPrefManager(tContext);
         tActivity = (MainActivity) getActivity();
        if (tActivity != null){
            switch (mainCatId){
                case "1":
                    tActivity.setTextToolbar("Mineral Water");
                    break;
            case "2":
                    tActivity.setTextToolbar("My Laundry");
                    break;
            case "3":
                    tActivity.setTextToolbar("Car Wash");
                    break;
            case "4":
                    tActivity.setTextToolbar("Daily Fresh");
                    break;
            }
        }
    }
    private void callApiSubCat(){
        try {
            tActivity.uiThreadHandler.sendEmptyMessage(Constant.SHOW_PROGRESS_DIALOG);
            String strUserId = tSharedPrefManager.getUserId();
            Api api = ApiClient.getApiClients().create(Api.class);
            Call<List<ModelSubCat>> call = api.getSubCat(mainCatId, strUserId);
            call.enqueue(new Callback<List<ModelSubCat>>() {
                @Override
                public void onResponse(Call<List<ModelSubCat>> call, Response<List<ModelSubCat>> response) {
                    tModelSubCats = response.body();
                    tAdapterSubCat = new AdapterSubCat(getContext(), tModelSubCats, tFragmentManager);
                    rvWater.setAdapter(tAdapterSubCat);
                    tActivity.uiThreadHandler.sendMessageDelayed(tActivity.uiThreadHandler.obtainMessage(Constant.HIDE_PROGRESS_DIALOG),Constant.HIDE_PROGRESS_DIALOG_DELAY);
                }

                @Override
                public void onFailure(Call<List<ModelSubCat>> call, Throwable t) {
                    CustomLog.e(Constant.TAG, "Sub Category not Responding ... ");
                    tActivity.uiThreadHandler.sendMessageDelayed(tActivity.uiThreadHandler.obtainMessage(Constant.HIDE_PROGRESS_DIALOG),Constant.HIDE_PROGRESS_DIALOG_DELAY);


                }
            });
        } catch (Exception e) {
            CustomLog.e(Constant.TAG, "Sub Category not Responding ... "+e);
        }

    }
    private void setSlider(){

        Api api = ApiClient.getApiClients().create(Api.class);
        Call<List<ModelBanner>> call = api.getBanner(mainCatId);
        call.enqueue(new Callback<List<ModelBanner>>() {
            @Override
            public void onResponse(Call<List<ModelBanner>> call, Response<List<ModelBanner>> response) {
                tModes = response.body();
                PagerAdapter tAdapter = new AdapterBanner(getActivity(), tModes);
                vpWater.setAdapter(tAdapter);
            }

            @Override
            public void onFailure(Call<List<ModelBanner>> call, Throwable t) {

            }
        });




        final Handler tHandler = new Handler();
        final Runnable tRunnable = new Runnable() {
            @Override
            public void run() {
                if (currentPage == 4){
                    currentPage = 0;
                }
                vpWater.setCurrentItem(currentPage++, true);
            }
        };

        tTimer = new Timer();
        tTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                tHandler.post(tRunnable);
            }
        },DELAY_MS, PERIOD_MS);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        tTimer.cancel();
    }
}
