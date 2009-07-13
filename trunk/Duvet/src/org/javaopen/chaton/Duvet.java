package org.javaopen.chaton;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;

import net.it4myself.util.RestfulClient;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Duvet extends Activity {
	private static final String TAG = "Duvet";
	
	public static final int PROGRESS_DIALOG = 0;
	
	private static final String HTML_TOP =
		"<html><head>" +
	    "<meta http-equiv='Content-Type' content='text/html; charset=utf-8' />\n" +
		"<title>Chaton</title>\n" +
		"<link href='file:///android_asset/chaton.css' rel='Stylesheet' type='text/css' />\n" +
		"<script src='file:///android_asset/chaton.js'></script>\n" +
		"<script src='file:///android_asset/prototype.js'></script>\n" +
		"<script language='JavaScript'>\n" +
		"	function updateMessage(arg) {\n" +
		"		var message = window.duvet.getChatMessage();\n" +
		"		var nc = window.duvet.getNc();\n" +
		"		new Insertion.Bottom('view-pane', message);\n" +
		"		document.getElementById('status-line').innerHTML = 'Connected (' + nc + ' users chatting)';\n" +
		"		window.scrollTo(0,document.body.scrollHeight); // bottom\n" +
		"	}\n" +
		"	function scrollToBottom() {\n" +
		"		window.scrollTo(0, document.body.scrollHeight);\n" +
		"	}\n" +
		"</script>\n" +
		"</head>\n" +
		"<body onload='scrollToBottom'><div id='view-pane'>\n";

	private static final String HTML_MIDDLE =
		"</div>\n" +
		"<div id='status-pane'><p id='status-line'>Connecting (";

	private static final String HTML_BOTTOM =
		" users chatting)</p>\n" +
		"</div>\n" +
		"<div id='bottom' />" +
		"</body>\n" +
		"<script language='JavaScript'>\n" +
		"window.addEventListener('DOMCharacterDataModified', scrollToBottom);\n" +
		"window.addEventListener('DOMSubtreeModified', scrollToBottom);\n" +
		"window.addEventListener('DOMNodeInserted', scrollToBottom);\n" +
		"window.addEventListener('DOMAttrModified', scrollToBottom);\n" +
		"</script>\n" +
		"</html>\n";

	private ProgressDialog dialog;
	
	private HandlerThread clientThread;
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
        webView.addJavascriptInterface(new AndroidBridge(), "duvet");
        webView.setWebViewClient(new WebViewClient() {
        	private static final String TAG = "WebViewClient";
        	@Override
			public void onLoadResource(WebView view, String url) {
        		Log.d(TAG, "loadResouce");
        		view.pageDown(true);
			}
			@Override
        	public void onPageStarted(WebView view, String url, Bitmap favicon) {
        		Log.d(TAG, "pageStarted");
                dialog =
                	ProgressDialog.show(Duvet.this, "", "Loading. Please wait...", true, true);
                dialog.show();
        	}
        	@Override
			public void onPageFinished(WebView view, String url) {
        		Log.d(TAG, "pageFinished");
            	if (dialog != null && dialog.isShowing()) dialog.dismiss(); 
        		view.pageDown(true);
        	}
        });
        
        String uri = Settings.getUri(this);
        String nickname = Settings.getNickname(this);

        client = (Client)this.getLastNonConfigurationInstance();
        if (client == null) {
        	if (dialog != null && dialog.isShowing()) dialog.dismiss(); 
            Dialog r = new RoomDialog(this);
            r.setTitle("Setup Room uri and Nickname");
            r.show();
        } else {
        	client.setContext(this);
        }
//        dialog =
//        	ProgressDialog.show(Duvet.this, "", "Loading. Please wait...", true, true);

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
        //webView.loadUrl("file:///android_asset/client.html");
		String uri = Settings.getUri(this);
        Uri.Builder builder =
        	Uri.parse(uri).buildUpon().appendPath(Client.LOGIN_PATH);
        Uri logpath = builder.build();
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Client.WHO_KEY, Settings.getNickname(this));
        final Context me = this;
        if (dialog != null) dialog.show();
        try {
			String sexp = RestfulClient.Post(logpath.toString(), map);
			Log.d(TAG, "sexp=" + sexp);
			client = new Client(this, uri, sexp);
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
						}
					});
				}
			});
			String content = client.fetchContent("0");
			String htmlFile = this.getString(R.string.html_file);
			writeHtml(htmlFile, content, client.getNc());
			String htmlUri = this.getString(R.string.html_uri);;
			webView.loadUrl(htmlUri);
//			new Thread(client).start();
			clientThread = createThread();
		} catch (JSONException e) {
			Toast.makeText(this, "Connection Error. Please Re-connection. " + e.getMessage(), Toast.LENGTH_LONG).show();
			Log.d(TAG, "connect: RuntimeException=" + e + ", message=" + e.getMessage());
		} catch (RuntimeException e) {
			Toast.makeText(this, "Connection Error. Please Re-connection. " + e.getMessage(), Toast.LENGTH_LONG).show();
			Log.d(TAG, "connect: RuntimeException=" + e + ", message=" + e.getMessage());
		} catch (ClientProtocolException e) {
			Toast.makeText(this, "Connection Error. Please Re-connection. " + e.getMessage(), Toast.LENGTH_LONG).show();
			Log.d(TAG, "connect: ClientProtocolException" + e + ", message=" + e.getMessage());
		} catch (IOException e) {
			Toast.makeText(this, "Connection Error. Please Re-connection. " + e.getMessage(), Toast.LENGTH_LONG).show();
			Log.d(TAG, "connect: IOException=" + e + ", message=" + e.getMessage());
		}
    }
	
	private HandlerThread createThread() {
		HandlerThread thread = new HandlerThread("duvet");
		thread.start();
		Handler handler = new Handler(thread.getLooper());
		handler.post(new Runnable (){
				public void run() {
					client.longPoll();
				}
			});
		return thread;
	}
	
//	@Override
//	protected void onDestroy() {
//		super.onDestroy();
//		Handler handler = new Handler(clientThread.getLooper());
//		handler.post(new Runnable() {
//			public void run() {
//				client.setNonStop(false);
//			}
//		});
//		clientThread.getLooper().quit();
//		try {
//			clientThread.join();
//		} catch (InterruptedException e) {
//			Toast.makeText(this, "Thread Error:" + e.getMessage(), Toast.LENGTH_LONG).show();
//			e.printStackTrace();
//		}
//	}

	private void writeHtml(String htmlFile, String content, String nc) throws FileNotFoundException, IOException {
		FileOutputStream stream = this.openFileOutput(htmlFile, Activity.MODE_PRIVATE);
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(stream));
		out.write(HTML_TOP);
		out.write(content);
		out.write(HTML_MIDDLE);
		out.write(nc);
		out.write(HTML_BOTTOM);
		out.flush();
		out.close();
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
			Toast.makeText(this, "Post Error:" + e.getMessage(), Toast.LENGTH_LONG).show();
			Log.d(TAG, e + e.getMessage());
			return false;
		} catch (ClientProtocolException e) {
			Toast.makeText(this, "Post Error:" + e.getMessage(), Toast.LENGTH_LONG).show();
			Log.d(TAG, e + e.getMessage());
			return false;
		} catch (IOException e) {
			Toast.makeText(this, "Post Error:" + e.getMessage(), Toast.LENGTH_LONG).show();
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
			// TODO thread join
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

