package org.javaopen.chaton;

import java.io.IOException;
import java.util.HashMap;

import net.it4myself.util.RestfulClient;

import org.apache.http.client.ClientProtocolException;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Duvet extends Activity {
	private static final String TAG = "Chaton";
	
	private Handler handler;
	private Client client;
	private WebView webView;
	private EditText sayText;
	private Button sayButton;
	
	private String message;
	private String numChats;
	
	public String getNumChats() {
		return numChats;
	}

	public void setNumChats(String numChats) {
		this.numChats = numChats;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        handler = new Handler(Looper.myLooper());

        webView = (WebView)findViewById(R.id.web_view);
        sayText = (EditText)findViewById(R.id.say_text);
        sayButton = (Button)findViewById(R.id.say_button);
        
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new AndroidBridge(), "chaton");
        webView.loadUrl("file:///android_asset/client.html");

        
        String uri = Settings.getUri(this);
        String nickname = Settings.getNickname(this);

        client = (Client)this.getLastNonConfigurationInstance();
        if (client == null) {
            Dialog r = new RoomDialog(this);
            r.setTitle("Setup Room uri and Nickname");
            r.show();
        }
        
        connect();
        
        sayText.setOnKeyListener(new View.OnKeyListener() {
        	public boolean onKey(View view, int keyCode, KeyEvent event) {
        		if (keyCode == KeyEvent.KEYCODE_ENTER) {
        			return say();
        		}
        		return false;
        	}
        });
        
        sayButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view) {
        		say();
        	}
        });
	}
	
	private void connect() {
        // connect
		String uri = Settings.getUri(this);
        Uri.Builder builder =
        	Uri.parse(uri).buildUpon().appendPath(Client.LOGIN_PATH);
        Uri logpath = builder.build();
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Client.WHO_KEY, Settings.getNickname(this));
        final Context me = this;
        try {
			String sexp = RestfulClient.Post(logpath.toString(), map);
			Log.d(TAG, "sexp=" + sexp);
			client = new Client(uri, sexp);
			final WebView view = webView;
			client.setOnStateChangedListener(new Client.OnStateChangedListener() {
				public void stateChanged(Client client, String content) {
					final String text = content;
					Log.d(TAG, "stateChange: text=" + text);
					setMessage(text);
					setNumChats(client.getNc());
					handler.post(new Runnable() {
						public void run() {
							view.loadUrl("javascript:updateMessage()");
							view.pageDown(true); // bottom
						}
					});
				}
			});
			client.setPos("0"); // test
			new Thread(client).start();
		} catch (RuntimeException e) {
			Toast.makeText(this, "Connection Error. Please Re-connection. " + e.getMessage(), Toast.LENGTH_LONG);
			Log.d(TAG, "connect: RuntimeException=" + e + ", message=" + e.getMessage());
		} catch (ClientProtocolException e) {
			Toast.makeText(this, "Connection Error. Please Re-connection. " + e.getMessage(), Toast.LENGTH_LONG);
			Log.d(TAG, "connect: ClientProtocolException" + e + ", message=" + e.getMessage());
		} catch (IOException e) {
			Toast.makeText(this, "Connection Error. Please Re-connection. " + e.getMessage(), Toast.LENGTH_LONG);
			Log.d(TAG, "connect: IOException=" + e + ", message=" + e.getMessage());
		}
    }
	
	private boolean say() {
		String text = sayText.getText().toString();
		if (text == null || text.length() == 0) return false;
		String post = client.getPostUri();
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("text", text);
		map.put("nick", Settings.getNickname(this));
		String result = null;
		try {
			result = RestfulClient.Post(post, map);
			sayText.getText().clear();
			return true;
		} catch (RuntimeException e) {
			Toast.makeText(this, "Post Error:" + e.getMessage(), Toast.LENGTH_LONG);
			Log.d(TAG, e + e.getMessage());
			return false;
		} catch (ClientProtocolException e) {
			Toast.makeText(this, "Post Error:" + e.getMessage(), Toast.LENGTH_LONG);
			Log.d(TAG, e + e.getMessage());
			return false;
		} catch (IOException e) {
			Toast.makeText(this, "Post Error:" + e.getMessage(), Toast.LENGTH_LONG);
			Log.d(TAG, e + e.getMessage());
			return false;
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		return client;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.settings:
			startActivity(new Intent(this, Settings.class));
			return true;
		case R.id.connect:
			connect();
			return true;
		}
		return false;
	}

	private class AndroidBridge {
	    public String getChatMessage() {
	    	return getMessage();
	    }
	    public String getNc() {
	    	return getNumChats();
	    }
    }
	
}
