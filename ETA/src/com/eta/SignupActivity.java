package com.eta;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import com.eta.transport.RegistrationRequest;
import com.eta.transport.TransportService;
import com.eta.transport.TransportServiceFactory;
import com.eta.transport.User;
import com.eta.util.ApplicationSharedPreferences;
import com.eta.util.Utility;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class SignupActivity extends Activity {
	private final String TAG = SignupActivity.class.getSimpleName();
	private EditText etName;
	private EditText etEmail;
	private EditText etPhone;
	private EditText etPassword;
	private EditText etConfirmPassword;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signup);

		etName = (EditText)findViewById(R.id.et_signup_name);
		etEmail =  (EditText)findViewById(R.id.et_signup_email);
		etPhone = (EditText)findViewById(R.id.et_signup_phone);
		etPassword = (EditText)findViewById(R.id.et_signup_password);
		etConfirmPassword = (EditText)findViewById(R.id.et_signup_confirm_password);
		
		String phoneNumber = Utility.getDevicePhoneNumber(this);
		if (phoneNumber != null && !phoneNumber.isEmpty()) {
			etPhone.setText(Utility.purgePhoneNumber(phoneNumber));
		}
	}

	public void onClick(View view) {
		switch(view.getId()) {
		case R.id.bt_signup:
			signUpUser();
			break;
			
		case R.id.bt_signup_cancel:
			//finish activity.
			finish();
			break;
			
		default:
			Log.e(TAG, "There is no such button");
			break;
		}
	}
	//onClick method for Signup button
	public void signUpUser(){

		String name = etName.getText().toString();
		String email = etEmail.getText().toString();
		String phone = etPhone.getText().toString();
		String password = etPassword.getText().toString();
		String confirmPassword = etConfirmPassword.getText().toString();
		String msg = "Please proivde values for following field(s):\n";
		boolean isError = false;
		if(name.isEmpty()){
			msg += "name\n";
			isError = true;
		}

		if(email.isEmpty()) {
			msg += "email\n";
			isError = true;
		}

		if(phone.isEmpty()) {
			msg += "phone\n";
			isError = true;
		}
		if (password.isEmpty()) {
			msg += "password\n";
			isError = true;
		}
		if (confirmPassword.isEmpty()) {
			msg += "confirm password\n";
			isError = true;
		}
		
		if (isError) {
			Utility.showErrorMessageWithOKButton(this, "Signup", msg);
			return;
		}

		//Check if password doesn't match.
		if(!password.equals(confirmPassword)) {
			Utility.showErrorMessageWithOKButton(this, 
											     "Password", 
											     "Passwords are not matching");
			//Reset password fields
			etPassword.setText("");
			etConfirmPassword.setText("");
			//Bring the focus.
			etPassword.setFocusableInTouchMode(true);
			etPassword.requestFocus();
			return;
		}
		
		String gcmRegistrationId = ApplicationSharedPreferences.getGCMClientRegistrationId(this);
		//if GCM registration id is empty then don't proceed.
		//There is something terribly wrong.
		if (gcmRegistrationId.isEmpty()) {
			Utility.showErrorMessageWithOKButton(this, 
					                             "GCM Registration missing",
					                             "GCM Registration ID not found, Something gone terribly wrong, closing application.");
			finish();
		}
		
		//Get the transport service to make post request to server.
		TransportService service = TransportServiceFactory.getTransportService();
				
		User user = new User (name,
							  email,
							  phone,
							  password,
							  gcmRegistrationId);
		
		service.signUp(new RegistrationRequest(user),
					   TransportService.HEADER_CONTENT_TYPE_JSON,
				       TransportService.HEADER_ACCEPT_JSON,
				       new Callback<Void>() {

			@Override
			public void failure(RetrofitError error) {
				
				Response response = error.getResponse();
				Utility.showErrorMessageWithOKButton(getApplicationContext(), 
						                             "Signup error", 
						                             "Server returned error during Singup");
				Log.i(TAG, response.getReason());
				Log.e(TAG, error.getStackTrace().toString());
			}

			@Override
			public void success(Void voidreturn, Response response) {
				Log.i(TAG, " STATUS : " + String.valueOf(response.getStatus()));
				Context context = getApplicationContext();
				Toast.makeText(context,
						       "Successfully registered", 
						       Toast.LENGTH_SHORT).show();
				
				//Time to launch ContactListActivity
				Intent intent = new Intent(context, ContactListActivity.class);
				startActivity(intent);
			}

		});
	}
	
	
}