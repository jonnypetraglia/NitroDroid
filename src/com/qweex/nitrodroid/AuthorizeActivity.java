/*
Copyright (c) 2012-2013 Qweex
Copyright (c) 2012-2013 Jon Petraglia

This software is provided 'as-is', without any express or implied warranty. In no event will the authors be held liable for any damages arising from the use of this software.

Permission is granted to anyone to use this software for any purpose, including commercial applications, and to alter it and redistribute it freely, subject to the following restrictions:

    1. The origin of this software must not be misrepresented; you must not claim that you wrote the original software. If you use this software in a product, an acknowledgment in the product documentation would be appreciated but is not required.

    2. Altered source versions must be plainly marked as such, and must not be misrepresented as being the original software.

    3. This notice may not be removed or altered from any source distribution.
 */
package com.qweex.nitrodroid;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ProgressBar;
import com.qweex.utils.QweexUtils;
import org.json.JSONObject;

public class AuthorizeActivity extends Activity {

	WebView wv;
	String authorize_url;
    ProgressBar Pbar;
    String serv;
    final static String REQUEST_URL = "http://app.nitrotasks.com/request_url",
            AUTH_URL = "http://app.nitrotasks.com/auth";
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
        String TAG= QweexUtils.TAG();
		Log.d(TAG, "Creating web browser thingamabob");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.auth);
		serv = getIntent().getExtras().getString("service");
        setTitle("NitroDroid - " + serv);
        loadPageAsync.execute();
	}


    AsyncTask<Void, Void, Void> loadPageAsync = new AsyncTask<Void, Void, Void>()
    {
        @Override
        protected Void doInBackground(Void... params)
        {
            String TAG= QweexUtils.TAG();
            Log.d(TAG, "Launching asyncronously");

            try {

                String arg1[] = {"service", serv};
                String arg2[] = {"app", "android"};

                JSONObject result = new JSONObject(QuickPrefsActivity.postData(REQUEST_URL, arg1, arg2));
                Log.d(TAG, "getAuth " + result.toString());
                authorize_url = result.getString("authorize_url");
                QuickPrefsActivity.oauth_token = result.getString("oauth_token");
                if("dropbox".equals(serv))
                    QuickPrefsActivity.oauth_token_secret = result.getString("oauth_token_secret");
                else
                    QuickPrefsActivity.oauth_token_secret = result.getString("oauth_secret");

            }catch(Exception e)
            {
                Log.e(TAG, "An error occurred in getting the auth: " + e.getClass());
                e.printStackTrace();
                doBackThings();
            }

            Pbar = (ProgressBar) findViewById(R.id.progressBar);

            wv = (WebView)findViewById(R.id.webView); //*/

            wv.getSettings().setJavaScriptEnabled(true);
            wv.getSettings().setBuiltInZoomControls(true);
            wv.setInitialScale(100);
            wv.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
            wv.setWebChromeClient(new WebChromeClient() {
                public void onProgressChanged(WebView view, int progress)
                {
                    if(progress < 100 && Pbar.getVisibility() == ProgressBar.GONE){
                        Pbar.setVisibility(ProgressBar.VISIBLE);
                    }
                    Pbar.setProgress(progress);
                    if(progress == 100) {
                        Pbar.setVisibility(ProgressBar.GONE);
                    }
                }
            });
            wv.setWebViewClient(new WebViewClient() {

                /*@TargetApi(8)
                public void onReceivedSslError (WebView view, android.webkit.SslErrorHandler handler, android.net.http.SslError error) {
                    handler.proceed() ;
                }*/

                @Override
                public void onReceivedError(WebView view, int errorCode,
                                            String description, String failingUrl) {
                    String TAG= QweexUtils.TAG();
                    Log.d(TAG, "FAIL: " + failingUrl);
                }

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);
                    return true;
                }
            });
            wv.loadUrl(authorize_url);
            return null;
        }
    };


	
    @TargetApi(5)
	public void onBackPressed()
    {
    	doBackThings();
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (Integer.parseInt(android.os.Build.VERSION.SDK) < 5
                && keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            doBackThings();
        }

        return super.onKeyDown(keyCode, event);
    }
    
    void doBackThings()
    {
    	Intent returnIntent = new Intent();
    	if(wv!=null && wv.getUrl().contains("http://app.nitrotasks.com/success"))
    		setResult(RESULT_OK, returnIntent);
    	else
    		setResult(RESULT_CANCELED, returnIntent);
    	finish();
    }
}
