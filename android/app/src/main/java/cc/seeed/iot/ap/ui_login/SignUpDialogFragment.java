package cc.seeed.iot.ap.ui_login;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import cc.seeed.iot.ap.datastruct.User;
import cc.seeed.iot.ap.webapi.IotApi;
import cc.seeed.iot.ap.webapi.IotService;
import cc.seeed.iot.ap.MyApplication;
import cc.seeed.iot.ap.R;
import cc.seeed.iot.ap.ui_main.MainScreenActivity;
import cc.seeed.iot.ap.webapi.model.UserResponse;
import retrofit.Callback;
import retrofit.RetrofitError;

/**
 * Created by tenwong on 15/6/30.
 */
public class SignUpDialogFragment extends DialogFragment {
    Context context;
    User user;

    AutoCompleteTextView mEmailView;
    EditText mPasswordView;
    EditText mPasswordVerifyView;
    private View mProgressView;
    private View mLoginRegisterView;
    private AlertDialog alertDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        user = ((MyApplication) getActivity().getApplication()).getUser();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_sign_up, null);

        mEmailView = (AutoCompleteTextView) view.findViewById(R.id.email);
        mPasswordView = (EditText) view.findViewById(R.id.password);
        mPasswordVerifyView = (EditText) view.findViewById(R.id.verify);
        mProgressView = view.findViewById(R.id.login_progress);
        mLoginRegisterView = view.findViewById(R.id.email_register_form);

        builder.setView(view);
        builder.setTitle("Sign Up");
        builder.setPositiveButton("Sign up", null);
        builder.setNegativeButton("Cancel", null);

        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        alertDialog = (AlertDialog) getDialog();
        if (alertDialog != null) {
            Button positiveButton = (Button) alertDialog.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Boolean wantToCloseDialog = false;
                    //Do stuff, possibly set wantToCloseDialog to true then...
                    attemptRegister();
                    if (wantToCloseDialog)
                        alertDialog.dismiss();
                    //else dialog stays open. Make sure you have an obvious way to close the dialog especially if you set cancellable to false.
                }
            });
        }
    }

    private void attemptRegister() {
        mEmailView.setError(null);
        mPasswordView.setError(null);

        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String passwordVerify = mPasswordVerifyView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (!(password.equals(passwordVerify))) {
            mPasswordVerifyView.setError("not same password");
            focusView = mPasswordVerifyView;
            cancel = true;
        }

        if (TextUtils.isEmpty(password) || !isPasswordValid(password)) {
            mPasswordView.setError("invalid Password");
            focusView = mPasswordView;
            cancel = true;
        }


        if (TextUtils.isEmpty(email)) {
            mEmailView.setError("Require email");
            focusView = mEmailView;
            cancel = true;

        } else if (!isEmailValid(email)) {
            mEmailView.setError("Invalid email");
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            final String fianlEmail = email;
            IotApi api = new IotApi();
            IotService iot = api.getService();
            iot.userCreate(email, password, new Callback<UserResponse>() {
                @Override
                public void success(UserResponse userResponse, retrofit.client.Response response) {
                    String status = userResponse.status;
                    if (status.equals("200")) {
//                        Toast.makeText(context, userResponse.msg, Toast.LENGTH_LONG).show();
                        alertDialog.dismiss();
                        user.email = fianlEmail;
                        user.user_key = userResponse.token;
                        user.user_id = userResponse.user_id;
                        ((MyApplication) getActivity().getApplication()).setUser(user);
                        ((MyApplication) getActivity().getApplication()).setLoginState(true);
                        Intent intent = new Intent(context, MainScreenActivity.class);
                        context.startActivity(intent);
                    } else {
                        showProgress(false);
                        mEmailView.setError(userResponse.msg);
                        mEmailView.requestFocus();
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    Toast.makeText(context, "连接服务器失败", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 6;
    }


    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginRegisterView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginRegisterView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginRegisterView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginRegisterView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}
