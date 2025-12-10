package com.alphawallet.app.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.alphawallet.app.R;
import com.alphawallet.app.entity.NetworkInfo;
import com.alphawallet.app.entity.Wallet;
import com.alphawallet.app.viewmodel.BaseViewModel;
import com.alphawallet.app.viewmodel.DappBrowserViewModel;
import com.alphawallet.app.web3.Web3View;
import com.alphawallet.app.web3.entity.Address;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ChatFragment extends BaseFragment
{
    private Web3View web3;
    private DappBrowserViewModel viewModel;
    private Wallet wallet;
    private NetworkInfo activeNetwork;
    private static final String MUMBLECHAT_URL = "https://mumblechat.com/conversations";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        toolbar(view);
        setToolbarTitle(R.string.chat_label);
        initViewModel();
        initWebView(view);
        return view;
    }

    private void initViewModel()
    {
        viewModel = new ViewModelProvider(this).get(DappBrowserViewModel.class);
        viewModel.defaultWallet().observe(getViewLifecycleOwner(), this::onDefaultWallet);
        viewModel.activeNetwork().observe(getViewLifecycleOwner(), this::onNetworkChanged);
    }

    private void onDefaultWallet(Wallet w)
    {
        wallet = w;
        setupWeb3();
    }

    private void onNetworkChanged(NetworkInfo networkInfo)
    {
        activeNetwork = networkInfo;
        setupWeb3();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView(View view)
    {
        web3 = view.findViewById(R.id.chat_webview);
        if (web3 != null)
        {
            WebSettings webSettings = web3.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setDomStorageEnabled(true);
            webSettings.setDatabaseEnabled(true);
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
            webSettings.setLoadWithOverviewMode(true);
            webSettings.setUseWideViewPort(true);
            webSettings.setSupportZoom(true);
            webSettings.setBuiltInZoomControls(false);
            webSettings.setDisplayZoomControls(false);
            webSettings.setAllowContentAccess(true);
            webSettings.setAllowFileAccess(false);
            webSettings.setGeolocationEnabled(false);
            webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
            webSettings.setUserAgentString(webSettings.getUserAgentString()
                    + "AlphaWallet(Platform=Android)");
        }
    }

    private void setupWeb3()
    {
        if (web3 == null || wallet == null || activeNetwork == null)
        {
            return;
        }

        web3.setChainId(activeNetwork.chainId, false);
        web3.setWalletAddress(new Address(wallet.address));

        web3.setWebChromeClient(new WebChromeClient()
        {
            @Override
            public boolean onConsoleMessage(ConsoleMessage msg)
            {
                return super.onConsoleMessage(msg);
            }
        });

        // Load the chat URL
        web3.loadUrl(MUMBLECHAT_URL);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (web3 != null)
        {
            web3.onResume();
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (web3 != null)
        {
            web3.onPause();
        }
    }

    @Override
    public void onDestroy()
    {
        if (web3 != null)
        {
            web3.destroy();
        }
        super.onDestroy();
    }
}
