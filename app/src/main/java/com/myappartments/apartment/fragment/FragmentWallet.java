package com.myappartments.apartment.fragment;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.myappartments.apartment.R;
import com.myappartments.apartment.activity.MainActivity;
import com.myappartments.apartment.api.Api;
import com.myappartments.apartment.api.ApiClient;
import com.myappartments.apartment.model.paytm.ModelChecksum;
import com.myappartments.apartment.model.paytm.ModelTransactionHistory;
import com.myappartments.apartment.storage.SharedPrefManager;
import com.myappartments.apartment.utils.Constant;
import com.myappartments.apartment.utils.CustomLog;
import com.myappartments.apartment.utils.CustomToast;
import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPGService;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentWallet extends Fragment {
    private Context tContext;
    private SharedPrefManager tSharedPrefManager;
    private PaytmPGService mPaymentService;
    private PaytmOrder mOrder;
    private String MID = "rsIFPL71143711550832";//rsIFPL71143711550832//LVNRji48342448716443
    private String mCheckSum = null;
    private String mCallBackUrl = "https://securegw.paytm.in/theia/paytmCallback?ORDER_ID=";
    //private String mCallBackUrl = "https://securegw-stage.paytm.in/theia/paytmCallback?ORDER_ID=";
    private String mOrderId = null;
    private String mCustId = null;
    private String mAmount = null;
    private String mIndustryId = null;
    private String mChannelId = null;
    private String mWebsite = null;
    private String mEmail = null;
    private String mMobile = null;



    @BindView(R.id.tv_wallet_amount)
    protected TextView tv_wallet_amount;
    @BindView(R.id.et_adding_amount)
    protected EditText et_adding_amount;
    @BindView(R.id.btn_addAmount)
    protected Button btn_addAmount;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_wallet, container, false);
        ButterKnife.bind(this, view);
        initFrag();

        return view;
    }

    private void initFrag(){
        tContext = getContext();
        tSharedPrefManager = new SharedPrefManager(tContext);
       MainActivity tActivity = (MainActivity) getActivity();
        if (tActivity != null){
            tActivity.setTextToolbar("My Wallet");
        }
        tv_wallet_amount.setText(tSharedPrefManager.getUserWallet());
        checkReadSmsPermission();
        mCustId =tSharedPrefManager.getUserId();
        mEmail = tSharedPrefManager.getUserEmail();
        mMobile = tSharedPrefManager.getUserPhone();
        String mTemp= mCustId.substring(0, Math.min(mCustId.length(), 6));
        mOrderId = mTemp + getOrderId();
        mIndustryId = "Retail";
        mChannelId = "WAP";
        mWebsite = "DEFAULT";
        mCallBackUrl = "https://securegw.paytm.in/theia/paytmCallback?ORDER_ID="+mOrderId;
        updateWalletAmount();
//        btn_addAmount.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//
//                }
//        });

    }

    @OnClick(R.id.btn_addAmount)
    public void clickBtnAddAmount(View view){
        mAmount = et_adding_amount.getText().toString().trim();
        CustomLog.d(Constant.TAG,"onStartPayment amount to pay : "+mAmount);
        generateChecksumApi(mAmount);
    }
    private void updateWalletAmount() {

        int wallet = Integer.parseInt(tSharedPrefManager.getUserWallet());
        tv_wallet_amount.setText(String.valueOf(wallet));
//        mAmount = et_adding_amount.getText().toString();

    }

    private void callTansactionHistroyApi(Bundle inResponse) {
        String status = inResponse.getString("STATUS");
        String checksum = inResponse.getString("CHECKSUMHASH");
        String bankName = inResponse.getString("BANKNAME");
        String orderId = inResponse.getString("ORDERID");
        String txnAmount = inResponse.getString("TXNAMOUNT");
        String txnDate = inResponse.getString("TXNDATE");
        String mid = inResponse.getString("MID");
        String txnId = inResponse.getString("TXNID");
        String respcode = inResponse.getString("RESPCODE");
        String paymentMode = inResponse.getString("PAYMENTMODE");
        String banktxnid = inResponse.getString("BANKTXNID");
        String currency = inResponse.getString("CURRENCY");
        String gatewayname = inResponse.getString("GATEWAYNAME");
        String respmsg = inResponse.getString("RESPMSG");
        CustomLog.d(Constant.TAG,"callTransactionHistoryApi send data :"+ inResponse.toString());

        Api api = ApiClient.getApiClients().create(Api.class);
        Call<ModelTransactionHistory> call = api.paymentTransactionHistory(status, checksum, bankName, orderId, txnAmount, txnDate, mid,
                txnId, respcode, paymentMode, banktxnid, currency, gatewayname, respmsg, mCustId);
        call.enqueue(new Callback<ModelTransactionHistory>() {
            @Override
            public void onResponse(Call<ModelTransactionHistory> call, Response<ModelTransactionHistory> response) {
                assert response.body() != null;
                CustomLog.d(Constant.TAG,"callTransactionHistoryApi onResonse :"+response.body().toString());
                if (!response.body().getError()){
                    CustomToast.tToastBottom(tContext, "Amount added in wallet successfully");

//                    Log.d("danny","callTransactionHistoryApi onResonse success ,, previous wallet amount :"+mList.get(0).getWalletAmount());
//                    User user= mList.get(0);
//                    user.setWalletAmount(response.body().getWallet());
//                    user.save();
                    tSharedPrefManager.clearWallet();
                    String strUpdatedWallet = String.valueOf(response.body().getWallet());
                    tSharedPrefManager.setUserWallet(strUpdatedWallet);

                    CustomLog.d(Constant.TAG,"callTransactionHistoryApi onResonse success ,, current wallet amount :"+response.body().getWallet());
                    updateWalletAmount();
                }
            }
            @Override
            public void onFailure(Call<ModelTransactionHistory> call, Throwable t) {
            }
        });
    }
    private void initPaymentMethod() {
        mPaymentService = PaytmPGService.getProductionService();// TODO for production environment: pro key : rsIFPL71143711550832
        HashMap<String, String> paramMap = new HashMap<String,String>();
        paramMap.put(Constant.MID, MID);
        paramMap.put(Constant.ORDER_ID , mOrderId);
        paramMap.put( Constant.CUST_ID , mCustId);
        paramMap.put( Constant.MOBILE_NO , mMobile);
        paramMap.put( Constant.EMAIL ,mEmail);
        paramMap.put(Constant.CHANNEL_ID , mChannelId);
        paramMap.put(Constant.TXN_AMOUNT , mAmount);
        paramMap.put( Constant.WEBSITE , mWebsite);
        paramMap.put(Constant.INDUSTRY_TYPE_ID ,mIndustryId);
        paramMap.put( Constant.CALLBACK_URL, mCallBackUrl);
        paramMap.put( Constant.CHECKSUMHASH , mCheckSum);
        mOrder = new PaytmOrder(paramMap);
        startPaymentService();
    }

    private void startPaymentService() {
        mPaymentService.initialize(mOrder, null);//TODO pass certificate if have else pass null

        mPaymentService.startPaymentTransaction(tContext, true, true, new PaytmPaymentTransactionCallback() {
            /*Call Backs*/
            public void someUIErrorOccurred(String inErrorMessage) {
                /*Display the error message as below */
                CustomToast.tToastBottom(tContext, "UI Error " + inErrorMessage);
            }
            public void onTransactionResponse(Bundle inResponse) {
                /*Display the message as below */
                CustomLog.d(Constant.TAG,"onTransactionResponse inResponse :"+inResponse.toString());
                if (inResponse.get(Constant.STATUS).equals(Constant.TXN_SUCCESS) && inResponse.get(Constant.RESPMSG).equals(Constant.Txn_Success)) {
                    callTansactionHistroyApi(inResponse);
                } else {
                    CustomToast.tToastBottom(tContext, "Payment Transaction Error occur");
                }
            }
            public void networkNotAvailable() {
                /*Display the message as below */
                CustomToast.tToastBottom(tContext, "Network connection error: Check your internet connectivity");
            }
            public void clientAuthenticationFailed(String inErrorMessage) {
                /*Display the message as below */
                CustomToast.tToastBottom(tContext, "Authentication failed: Server error");
            }
            public void onErrorLoadingWebPage(int iniErrorCode, String inErrorMessage, String inFailingUrl) {
                /*Display the message as below */
                CustomToast.tToastBottom(tContext, "Unable to load web page");
            }
            public void onBackPressedCancelTransaction() {
                /*Display the message as below */
                CustomToast.tToastBottom(tContext, "Transaction cancelled back press");

            }
            public void onTransactionCancel(String inErrorMessage, Bundle inResponse) {
                /*Display the message as below */
                CustomToast.tToastBottom(tContext, "Payment Transaction cancel");

            }
        });
    }
    private void checkReadSmsPermission() {
        if (ContextCompat.checkSelfPermission(tContext, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS}, 101);
        }
    }

    private void generateChecksumApi(String strAmount) {
        Api api = ApiClient.getApiClients().create(Api.class);
        Call<ModelChecksum> call = api.generateCheckSum(MID, mOrderId,mCustId,mIndustryId,mChannelId,
                strAmount,mWebsite,mEmail,mMobile);
        call.enqueue(new Callback<ModelChecksum>() {
            @Override
            public void onResponse(Call<ModelChecksum> call, Response<ModelChecksum> response) {
                assert response.body() != null;
                mCheckSum = response.body().getCHECKSUMHASH();
                CustomLog.d(Constant.TAG,"generate checksum onResponse :"+ mCheckSum);
                initPaymentMethod();
            }
            @Override
            public void onFailure(Call<ModelChecksum> call, Throwable t) {
                CustomLog.d(Constant.TAG,"generate checksum onFailure :"+ call.toString());
            }
        });
    }
    private String getOrderId()
    {
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789"
                + "abcdefghijklmnopqrstuvxyz";
        StringBuilder sb = new StringBuilder(10);

        for (int i = 0; i < 10; i++) {
            int index
                    = (int)(AlphaNumericString.length()
                    * Math.random());
            sb.append(AlphaNumericString
                    .charAt(index));
        }
        return sb.toString();
    }

}