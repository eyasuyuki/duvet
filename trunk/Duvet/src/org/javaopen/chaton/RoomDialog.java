package org.javaopen.chaton;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class RoomDialog extends Dialog {
	private static final String TAG = "RoomDialog";
	private EditText uriText;
	private EditText nicknameText;
	private Button goButton;
	private Button cancelButton;
	private Context context;

	public RoomDialog(Context chaton) {
		super(chaton);
		this.context = chaton;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.uri_diarog);

		findViews();
		setListeners();
		setSettings();
		
	}
	
	private void findViews() {
		uriText = (EditText)findViewById(R.id.uri_text);
		nicknameText = (EditText)findViewById(R.id.nickname_text);
		goButton = (Button)findViewById(R.id.go_button);
		cancelButton = (Button)findViewById(R.id.cancel_button);
	}
	
	private void setListeners() {
		View.OnClickListener l = new View.OnClickListener() {
            public void onClick(View v) {
            	checkResult();
            }
        };
		uriText.setOnClickListener(l);
		nicknameText.setOnClickListener(l);
		goButton.setOnClickListener(l);
		cancelButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Log.d(TAG, "cancel clicked.");
				dismiss();
			}
		});
	}
	
	private void checkResult() {
		String uri = uriText.getText().toString();
		String nickname = nicknameText.getText().toString();
		SharedPreferences.Editor edit =
			PreferenceManager.getDefaultSharedPreferences(context).edit();
		edit.putString(Settings.OPT_URI, uri);
		edit.putString(Settings.OPT_NICKNAME, nickname);
		edit.commit();
    	dismiss();
	}
	
	private void setSettings() {
		String uri = Settings.getUri(context);
		String nickname = Settings.getNickname(context);
		uriText.setText(uri.toCharArray(), 0, uri.length());
		nicknameText.setText(nickname.toCharArray(), 0, nickname.length());
	}
	
}
