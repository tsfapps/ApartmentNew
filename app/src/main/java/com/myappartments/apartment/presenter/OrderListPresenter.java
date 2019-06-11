package com.myappartments.apartment.presenter;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;

import com.myappartments.apartment.activity.MainActivity;
import com.myappartments.apartment.api.Api;
import com.myappartments.apartment.api.ApiClient;
import com.myappartments.apartment.fragment.FragmentOrderList;
import com.myappartments.apartment.model.ModelOrderList;
import com.myappartments.apartment.utils.Constant;
import com.myappartments.apartment.utils.CustomLog;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderListPresenter {
    public static void callApiOrderList(String strUserId, final FragmentOrderList tFragment){
        Api api = ApiClient.getApiClients().create(Api.class);
        Call<List<ModelOrderList>> call = api.cartOrderList(strUserId);
        call.enqueue(new Callback<List<ModelOrderList>>() {
            @Override
            public void onResponse(Call<List<ModelOrderList>> call, Response<List<ModelOrderList>> response) {
               tFragment.onResponseApiOrderList(response);
            }

            @Override
            public void onFailure(Call<List<ModelOrderList>> call, Throwable t) {
                tFragment.onFailureApiOrderList(call);

            }
        });

    }

}