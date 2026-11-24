package com.freemocktest.app;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private LinearLayout rootLayout;
    private FrameLayout bannerContainer;
    private AdView activeBannerView;
    private String currentBannerUnitId = "";
    private InterstitialAd interstitialAd;

    // Aapki Netlify website ka link
    private static final String WEBSITE_URL =
            "https://reliable-otter-eec9e4.netlify.app";

    // Test page ke URL mein jo keyword aata ho
    private static final String TEST_URL_KEYWORD = "test";

    // Home_Banner
    private static final String HOME_BANNER_ID =
            "ca-app-pub-4009702350721862/8418510001";

    // Test_Banner
    private static final String TEST_BANNER_ID =
            "ca-app-pub-4009702350721862/3343050918";

    // Submit_Interstitial
    private static final String INTERSTITIAL_ID =
            "ca-app-pub-4009702350721862/3151465229";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MobileAds.initialize(this, status -> {});
        loadInterstitialAd();

        rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        webView = new WebView(this);
        LinearLayout.LayoutParams webParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1.0f
        );
        webView.setLayoutParams(webParams);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                updateBannerForUrl(url);
                if (url != null && url.toLowerCase().contains("result")) {
                    showInterstitialAd();
                }
            }
        });

        bannerContainer = new FrameLayout(this);
        LinearLayout.LayoutParams bannerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        bannerParams.gravity = Gravity.CENTER_HORIZONTAL;
        bannerContainer.setLayoutParams(bannerParams);

        rootLayout.addView(webView);
        rootLayout.addView(bannerContainer);
        setContentView(rootLayout);

        updateBannerForUrl(WEBSITE_URL);
        webView.loadUrl(WEBSITE_URL);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (webView != null && webView.canGoBack()) {
                    webView.goBack();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    private void updateBannerForUrl(String url) {
        String targetUnitId = (url != null && url.toLowerCase().contains(TEST_URL_KEYWORD))
                ? TEST_BANNER_ID
                : HOME_BANNER_ID;

        if (targetUnitId.equals(currentBannerUnitId)) {
            return;
        }
        currentBannerUnitId = targetUnitId;

        bannerContainer.removeAllViews();
        if (activeBannerView != null) {
            activeBannerView.destroy();
        }

        activeBannerView = new AdView(this);
        activeBannerView.setAdUnitId(targetUnitId);
        activeBannerView.setAdSize(AdSize.BANNER);
        bannerContainer.addView(activeBannerView);
        activeBannerView.loadAd(new AdRequest.Builder().build());
    }

    private void loadInterstitialAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(this, INTERSTITIAL_ID, adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd ad) {
                interstitialAd = ad;
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                interstitialAd = null;
            }
        });
    }

    private void showInterstitialAd() {
        if (interstitialAd != null) {
            interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    interstitialAd = null;
                    loadInterstitialAd();
                }
            });
            interstitialAd.show(this);
        }
    }

    @Override
    protected void onDestroy() {
        if (activeBannerView != null) {
            activeBannerView.destroy();
        }
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }
}
