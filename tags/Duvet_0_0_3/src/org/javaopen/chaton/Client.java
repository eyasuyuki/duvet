package org.javaopen.chaton;

import java.io.IOException;
import java.util.HashMap;

import net.it4myself.util.RestfulClient;

import org.apache.http.client.ClientProtocolException;
import org.javaopen.lisp.Env;
import org.javaopen.lisp.List;
import org.javaopen.lisp.Nil;
import org.javaopen.lisp.Reader;
import org.javaopen.lisp.Sexp;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

//public class Client implements Runnable {
public class Client {
	private static final String TAG = "Client";
	
	public static final String LOGIN_PATH    = "apilogin";
	public static final String WHO_KEY       = "who";
	
	public static final String ROOM_NAME_KEY = "room-name";
	public static final String POST_URI_KEY  = "post-uri";
	public static final String COMET_URI_KEY = "comet-uri";
	public static final String ICON_URI_KEY  = "icon-uri";
	public static final String CID_KEY       = "cid";
	public static final String POS_KEY       = "pos";
	public static final String NC_KEY        = "nc";
	
	private static final String T_KEY        = "t";
	private static final String P_KEY        = "p";
	private static final String C_KEY        = "c";

	private Context context;
	private String roomUri;
	private String roomName;
	private String postUri;
	private String cometUri;
	private String iconUri;
	private String cid;
	private String pos;
	private String nc;
	
	private boolean isNonStop = true;
	
	private int count = 0;
	
	public boolean isNonStop() {
		return isNonStop;
	}

	public void setNonStop(boolean isNonStop) {
		this.isNonStop = isNonStop;
	}

	private OnStateChangedListener listener = null;
	
	private HashMap<String, String> param = new HashMap<String, String>();
	
	private static final String CONTENT_KEY = "content";
	
	public Client(Context context, String roomUri, String listText) {
        Env env = new Env();
        Reader reader = new Reader(env);
		List conn = null;
		try {
			conn = (List)reader.readFromString(listText);
			init(roomUri, conn);
		} catch (IOException e) {
			Log.d(TAG, "Client: e=" + e + ", message=" + e.getMessage());
		}
	}
	
	public void init(String roomUri, List list) {
		this.roomUri = roomUri;
		
		HashMap<String, Sexp> map = new HashMap<String, Sexp>();
		List car = null;
		List lis = null;
		Sexp caar = null;
		List cdar = null;
		Sexp cdr = null;
		do {
			car = (List)list.car();
			cdr = list.cdr();
			if (car.size() > 1) {
				lis = car;
				do {
					caar = lis.car();
					cdar = (List)lis.cdr();
					lis = cdar;
				} while (cdar != Nil.NIL);
			}
			map.put(car.car().serialize(), caar);
			list = (List)cdr;
		} while (cdr != Nil.NIL);
		
		setRoomName(((org.javaopen.lisp.String)map.get(ROOM_NAME_KEY)).valueOf());
		setPostUri(((org.javaopen.lisp.String)map.get(POST_URI_KEY)).valueOf());
		setCometUri(((org.javaopen.lisp.String)map.get(COMET_URI_KEY)).valueOf());
		setIconUri(((org.javaopen.lisp.String)map.get(ICON_URI_KEY)).valueOf());
		setCid(String.valueOf(((org.javaopen.lisp.Integer)map.get(CID_KEY)).valueOf()));
		setPos(String.valueOf(((org.javaopen.lisp.Integer)map.get(POS_KEY)).valueOf()));
	}

//	public void run() {
//		Looper.prepare();
//		longPoll();
//	}
	
	public String fetchContent(String pos) throws ClientProtocolException, IOException, JSONException {
		param.put(T_KEY, String.valueOf(System.currentTimeMillis()));
		param.put(P_KEY, pos);
		param.put(C_KEY, getCid());
		String json = RestfulClient.Get(cometUri, param);
		JSONObject result = new JSONObject(json);
		setCid(result.getString(CID_KEY));
		setPos(result.getString(POS_KEY));
		setNc(result.getString(NC_KEY));
		final String html = result.getString(CONTENT_KEY);
		if (listener != null) {
			listener.stateChanged(this, html);
		}
		return html;
	}
	
//	public void longPoll() {
//		while (isNonStop()) {
//			try {
//				fetchContent(getPos());
//				this.count++;
//				Log.d(TAG, "longPoll: count=" + count);
////			} catch (InterruptedException e) {
////				showError(context, e);
//			} catch (ClientProtocolException e) {
//				showError(context, e);
//			} catch (IOException e) {
//				showError(context, e);
//			} catch (JSONException e) {
//				showError(context, e);
//			}
//		}
//	}
	
	public void showError(Context context, Exception e) {
		Log.d(TAG, "longPoll: e=" + e + ", message=" + e.getMessage() + ", context=" + context);
		if (context != null) {
			Toast.makeText(context, "Connection Error. Please Re-connection. " + e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

	interface OnStateChangedListener {
		public void stateChanged(Client client, String content);
	}
	
	public void setOnStateChangedListener(OnStateChangedListener l) {
		listener = l;
	}

	public String getRoomUri() {
		return roomUri;
	}
	public void setRoomUri(String roomUri) {
		this.roomUri = roomUri;
	}
	public String getRoomName() {
		return roomName;
	}
	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}
	public String getPostUri() {
		return postUri;
	}
	public void setPostUri(String postUri) {
		this.postUri = postUri;
	}
	public String getCometUri() {
		return cometUri;
	}
	public void setCometUri(String cometUri) {
		this.cometUri = cometUri;
	}
	public String getIconUri() {
		return iconUri;
	}
	public void setIconUri(String iconUri) {
		this.iconUri = iconUri;
	}
	public String getCid() {
		return cid;
	}
	public void setCid(String cid) {
		this.cid = cid;
	}
	public String getPos() {
		return pos;
	}
	public void setPos(String pos) {
		this.pos = pos;
	}

	public String getNc() {
		return nc;
	}

	public void setNc(String nc) {
		this.nc = nc;
	}

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}
}
