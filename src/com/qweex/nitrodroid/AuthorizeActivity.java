/*
Copyright (c) 2012 Qweex
Copyright (c) 2012 Jon Petraglia

This software is provided 'as-is', without any express or implied warranty. In no event will the authors be held liable for any damages arising from the use of this software.

Permission is granted to anyone to use this software for any purpose, including commercial applications, and to alter it and redistribute it freely, subject to the following restrictions:

    1. The origin of this software must not be misrepresented; you must not claim that you wrote the original software. If you use this software in a product, an acknowledgment in the product documentation would be appreciated but is not required.

    2. Altered source versions must be plainly marked as such, and must not be misrepresented as being the original software.

    3. This notice may not be removed or altered from any source distribution.
 */
package com.qweex.nitrodroid;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class AuthorizeActivity extends Activity {

	WebView wv;
	String authorize_url;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		Log.d("AuthorizeActivity", "Creating web browser thingamabob");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.auth);
		authorize_url = getIntent().getExtras().getString("authorize_url");
		System.out.println(authorize_url);
		
		wv = (WebView)findViewById(R.id.webView);
		wv.getSettings().setJavaScriptEnabled(true);
        wv.getSettings().setBuiltInZoomControls(true);
        wv.setInitialScale(100);
        wv.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
		wv.setWebViewClient(new WebViewClient() {
			
			@TargetApi(8)
			public void onReceivedSslError (WebView view, android.webkit.SslErrorHandler handler, android.net.http.SslError error) {
				handler.proceed() ;
				}
			
	        @Override
	        public void onReceivedError(WebView view, int errorCode,
	                String description, String failingUrl) {
	        	Log.d("AuthorizeActivity:onReceivedError", "FAIL: " + failingUrl);
	        }

	        @Override
	        public boolean shouldOverrideUrlLoading(WebView view, String url) {
	            view.loadUrl(url);
	            return true;
	        }
		});
		wv.loadUrl(authorize_url);
	}
	
	
	
	
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
    	if(wv.getUrl().contains("http://app.nitrotasks.com/success"))
    		setResult(RESULT_OK, returnIntent);
    	else
    		setResult(RESULT_CANCELED, returnIntent);
    	finish();
    }
}
