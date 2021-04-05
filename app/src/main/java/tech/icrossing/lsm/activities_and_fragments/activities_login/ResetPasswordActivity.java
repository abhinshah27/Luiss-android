/*
 * Copyright (c) 2018. Highlanders LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package tech.icrossing.lsm.activities_and_fragments.activities_login;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONException;

import tech.icrossing.lsm.R;
import tech.icrossing.lsm.base.HLActivity;
import tech.icrossing.lsm.base.OnApplicationContextNeeded;
import tech.icrossing.lsm.utility.AnalyticsUtils;
import tech.icrossing.lsm.utility.Constants;
import tech.icrossing.lsm.utility.FieldValidityWatcher;
import tech.icrossing.lsm.utility.LogUtils;
import tech.icrossing.lsm.utility.Utils;
import tech.icrossing.lsm.websocket_connection.HLRequestTracker;
import tech.icrossing.lsm.websocket_connection.HLServerCalls;
import tech.icrossing.lsm.websocket_connection.OnMissingConnectionListener;
import tech.icrossing.lsm.websocket_connection.OnServerMessageReceivedListenerWithErrorDescription;
import tech.icrossing.lsm.websocket_connection.ServerMessageReceiver;

/**
 * A screen for password reset.
 *
 * @author mbaldrighi on 4/20/2017
 */
public class ResetPasswordActivity extends HLActivity implements View.OnClickListener,
		OnServerMessageReceivedListenerWithErrorDescription, OnMissingConnectionListener {

	public static final String LOG_TAG = ResetPasswordActivity.class.getCanonicalName();

	// UI references.
	private EditText mEmailView;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reset_password);
		setRootContent(R.id.root_content);
		setProgressIndicator(R.id.generic_progress_indicator);

		mEmailView = findViewById(R.id.reset_pwd_email);
		mEmailView.addTextChangedListener(new FieldValidityWatcher(R.id.reset_pwd_email, mEmailView));

		findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
				overridePendingTransition(R.anim.no_animation, R.anim.slide_out_down);
			}
		});

		findViewById(R.id.main_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				attemptPasswordRecovery();
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();

		configureResponseReceiver();
	}

	@Override
	protected void onResume() {
		super.onResume();

		AnalyticsUtils.trackScreen(this, AnalyticsUtils.LOGIN_RESET_PWD);
	}

	@Override
	protected void onPause() {
		super.onPause();
		Utils.closeKeyboard(mEmailView);
	}

	@Override
	public void onClick(View v) {}

	@Override
	protected void configureResponseReceiver() {
		if (serverMessageReceiver == null)
			serverMessageReceiver = new ServerMessageReceiver();
		serverMessageReceiver.setListener(this);
	}


	@Override
	protected void manageIntent() {}


	private void attemptPasswordRecovery() {
		String email = mEmailView.getText().toString();

		if (!Utils.isStringValid(email)) {
			showAlert(getString(R.string.error_recovery_email_empty));
			return;
		}
		else if (!Utils.isEmailValid(email)) {
			showAlert(R.string.error_invalid_email);
			return;
		}

		try {
			Object[] results = HLServerCalls.recoverPassword(email);
			HLRequestTracker.getInstance((OnApplicationContextNeeded) getApplication())
					.handleCallResult(this, this, results);
		}
		catch (JSONException e) {
			LogUtils.e(LOG_TAG, e.getMessage());
			e.printStackTrace();
			showGenericError();
		}
	}

	@Override
	public void handleSuccessResponse(int operationId, JSONArray responseObject) {
		closeProgress();

		switch (operationId) {
			case Constants.SERVER_OP_PWD_RECOVERY:
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						Utils.fireOpenEmailIntent(ResetPasswordActivity.this);
					}
				}, 1000);
				break;
		}
	}

	@Override
	public void handleErrorResponse(int operationId, int errorCode) {}

	@Override
	public void handleErrorResponse(int operationId, int errorCode, String description) {
		closeProgress();

		String message = getString(R.string.error_unknown);
		if (errorCode == Constants.SERVER_ERROR_GENERIC) {
			showAlert(message);
		}
		else {
			switch (operationId) {
				case Constants.SERVER_OP_PWD_RECOVERY:
					switch (errorCode) {
						case Constants.SERVER_ERROR_RECOVERY_NO_EMAIL:
							message = description;
					}
					showAlert(message);
					break;
			}
		}
	}

	@Override
	public void onMissingConnection(int operationId) {
		closeProgress();
	}

}