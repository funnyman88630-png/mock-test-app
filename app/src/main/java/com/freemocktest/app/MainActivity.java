package com.freemocktest.app;

import android.os.Bundle;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

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
    private AdView activeBannerView;
    private String currentBannerUnitId = "";
    private InterstitialAd interstitialAd;

    // Apni website ka URL yahan daalein
    private static final String WEBSITE_URL =
            "https://YOUR-NETLIFY-SITE.netlify.app";

    // Test page ke URL mein jo keyword aata ho (jaise "test", "mock", "exam")
    private static final String TEST_URL_KEYWORD = "test";

    // Home_Banner
    private static final String HOME_BANNER_ID =
            "ca-app-pub-4089702550721662/8418510985";

    // Test_Banner
    private static final String TEST_BANNER_ID =
            "ca-app-pub-4089702550721662/5343036918";

    // Submit_Interstitial
    private static final String INTERSTITIAL_ID =
            "ca-app-pub-4089702550721662/5151465229";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MobileAds.initialize(this, status -> {});

        rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.VERTICAL);

        // WebView upar rahega, banner hamesha iske neeche
        webView = new WebView(this);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                boolean isTestPage = url != null && url.toLowerCase().contains(TEST_URL_KEYWORD.toLowerCase());
                String targetBannerId = isTestPage ? TEST_BANNER_ID : HOME_BANNER_ID;
                showBannerAtBottom(targetBannerId);
            }
        });

        rootLayout.addView(webView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0, 1
        ));

        setContentView(rootLayout);

        // Shuru mein Home Banner neeche load karein
        showBannerAtBottom(HOME_BANNER_ID);
        loadInterstitialAd();
        webView.loadUrl(WEBSITE_URL);
    }

    private void showBannerAtBottom(String bannerUnitId) {
        if (bannerUnitId.equals(currentBannerUnitId) && activeBannerView != null) {
            return;
        }

        if (activeBannerView != null) {
            rootLayout.removeView(activeBannerView);
            activeBannerView.destroy();
            activeBannerView = null;
        }

        currentBannerUnitId = bannerUnitId;

        int width = (int) (getResources()
                .getDisplayMetrics().widthPixels /
                getResources().getDisplayMetrics().density);
        AdSize adaptiveSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, width);

        activeBannerView = new AdView(this);
        activeBannerView.setAdUnitId(bannerUnitId);
        activeBannerView.setAdSize(adaptiveSize);

        rootLayout.addView(activeBannerView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        activeBannerView.loadAd(new AdRequest.Builder().build());
    }

    private void loadInterstitialAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(
                this,
                INTERSTITIAL_ID,
                adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd ad) {
                        interstitialAd = ad;
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        interstitialAd = null;
                    }
                }
        );
    }

    private void showInterstitialAndFinish() {
        if (interstitialAd != null) {
            interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    interstitialAd = null;
                    loadInterstitialAd();
                    finish();
                }

                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull com.google.android.gms.ads.AdError adError) {
                    interstitialAd = null;
                    finish();
                }
            });
            interstitialAd.show(this);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            showInterstitialAndFinish();
        }
    }

    @Override
    protected void onPause() {
        if (webView != null) webView.onPause();
        if (activeBannerView != null) activeBannerView.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webView != null) webView.onResume();
        if (activeBannerView != null) activeBannerView.resume();
        if (interstitialAd == null) {
            loadInterstitialAd();
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
