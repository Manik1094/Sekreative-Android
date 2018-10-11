package com.sekreative.sekreative.ui.auth;


import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sekreative.sekreative.R;
import com.sekreative.sekreative.ui.MainActivity;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;


public class AuthFragment extends androidx.fragment.app.Fragment {


    public AuthFragment() {
        // Required empty public constructor
    }
    public static AuthFragment instantiate() {
        return new AuthFragment();
    }
    public static final String TAG = "AuthFragment";
    private Animation fromBottom , fromTop;
    String verificationCode;
    private String mVerificationId;
    PhoneAuthProvider.ForceResendingToken mResendToken;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    FirebaseAuth mFirebaseauth;
    String phonenumber;
    boolean isConnected;
    String phn;
    long phoneLong;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();

    @BindView(R.id.main_progress)
    ProgressBar progressBar;

    @BindView(R.id.verify_button)
    FloatingActionButton verify_button;

    @BindView(R.id.sekreative_label)
    TextView seKreative_label;

    @BindView(R.id.tnc_textview)
    TextView tnc_textview;

    @BindView(R.id.beCreative_label)
    TextView beCreative_label;

    @BindView(R.id.textView15)
    TextView textView15;

    @BindView(R.id.textInputEditText)
    EditText phone_edit_text;

    @BindView(R.id.send_otp_btn_signup)
    FloatingActionButton fab;

    @BindView(R.id.checkBox2)
    CheckBox checkBox;

    @BindView(R.id.otp_editText)
    EditText otp_editText;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_auth, container, false);
        progressBar = view.findViewById(R.id.main_progress);
        phone_edit_text = view.findViewById(R.id.textInputEditText);
        otp_editText = view.findViewById(R.id.otp_editText);
        seKreative_label = view.findViewById(R.id.sekreative_label);
        beCreative_label = view.findViewById(R.id.beCreative_label);
        checkBox = view.findViewById(R.id.checkBox2);
        textView15 = view.findViewById(R.id.textView15);
        fab = view.findViewById(R.id.send_otp_btn_signup) ;
        tnc_textview = view.findViewById(R.id.tnc_textview);
        progressBar.setVisibility(View.INVISIBLE);
        otp_editText.setVisibility(View.INVISIBLE);
        verify_button.setVisibility(View.INVISIBLE);

        mFirebaseauth = FirebaseAuth.getInstance();

        fromBottom = AnimationUtils.loadAnimation(getActivity() , R.anim.from_bottom);
        fromTop = AnimationUtils.loadAnimation(getActivity() , R.anim.from_top);
        phone_edit_text.setAnimation(fromBottom);
        seKreative_label.setAnimation(fromTop);
        beCreative_label.setAnimation(fromTop);
        checkBox.setAnimation(fromBottom);
        textView15.setAnimation(fromBottom);
        fab.setAnimation(fromTop);
        tnc_textview.setAnimation(fromBottom);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checkBox.isChecked()) {

                    isConnected = checkInternetConnection();


                    if (isConnected) {
                        //we are connected to a network


                       phonenumber = phone_edit_text.getText().toString();
                       phonenumber+= "+91";

                        authenticateUser(phonenumber);



                    } else {
                        //connected = false;
                        Toast.makeText(getActivity(), "No Internet Connection", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "You need to be 13 years or older to use SeKreative!", Toast.LENGTH_SHORT).show();
                }

            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

                Log.e(TAG, "Entered method onVerificationCompleted");


            }

            @Override
            public void onVerificationFailed(FirebaseException e) {

                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(getActivity(), "Error occurred! Please try again", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Entered method onVerificationFailed", e);


            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.

                progressBar.setVisibility(View.INVISIBLE);

                Log.e(TAG, "Entered method onCodeSent");

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                Log.e(TAG, "Verification Id is " + mVerificationId);
                mResendToken = token;

                verifyUser(mVerificationId , mResendToken);



            }


        };



        return view;
    }

    private boolean checkInternetConnection(){

        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo!=null && networkInfo.isConnected()){

            return true;
        }
        else
            return false;

    }

    private void authenticateUser(String phonenumber){

        if (phonenumber.length() == 10) {

            progressBar.setVisibility(View.VISIBLE);
            PhoneAuthProvider.getInstance().verifyPhoneNumber(phonenumber, 60, TimeUnit.SECONDS, getActivity(), mCallbacks);

        } else {
            phone_edit_text.setError("Invalid Phone Number");
        }


    }
    private void verifyUser(String mVerificationId , PhoneAuthProvider.ForceResendingToken mResendToken){

        phone_edit_text.setVisibility(View.GONE);
        checkBox.setVisibility(View.GONE);
        textView15.setVisibility(View.GONE);
        fab.setVisibility(View.GONE);
        tnc_textview.setVisibility(View.GONE);
        otp_editText.setVisibility(View.VISIBLE);
        verify_button.setVisibility(View.VISIBLE);
        otp_editText.setAnimation(fromTop);
        verify_button.setAnimation(fromTop);

        verify_button.setOnClickListener(view -> {


            verificationCode = otp_editText.getText().toString();
            if(!(TextUtils.isEmpty(verificationCode))){

                progressBar.setVisibility(View.VISIBLE);

                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);

                 isConnected = checkInternetConnection();

                if (isConnected) {

                    //Connected to a network
                    signInWithPhoneAuthCredential(credential);

                } else {

                    Toast.makeText(getActivity(), "No Internet Connection", Toast.LENGTH_SHORT).show();
                }
            } else {
                otp_editText.setError("Please enter OTP");
            }

        });


    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {

        mFirebaseauth.signInWithCredential(credential)

                .addOnCompleteListener(getActivity(), task -> {

                    if (task.isSuccessful()) {

                        progressBar.setVisibility(View.INVISIBLE);

                        final FirebaseUser user = task.getResult().getUser();

                        phn = user.getPhoneNumber();
                        final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        Log.e("OtpActivtiySignUp", "User Id Sent to intent extra: " + userId);

                        phoneLong = Long.parseLong(phn);

                        final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("phoneNumbers");

                        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {

                            @Override

                            public void onDataChange(DataSnapshot snapshot) {

                                if (snapshot.hasChild(phn)) {

                                    Toast.makeText(getActivity(), "Sign In Successful!", Toast.LENGTH_SHORT).show();

                                    Intent intent = new Intent(getActivity(),MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    intent.putExtra("phn",phn);
                                    startActivity(intent);
                                   getActivity().finish();

                                }else{


                                   Log.e(TAG , "New User , Send to DataCOllection Fragment");

                                }

                            }

                            @Override

                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                Log.e(TAG, "Database Error Message: " + databaseError.toString());

                            }

                        });


                    } else {

                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {

                            // The verification code entered was invalid

                            Toast.makeText(getActivity(), "Invalid Code entered", Toast.LENGTH_SHORT).show();

                            progressBar.setVisibility(View.INVISIBLE);

                        }
                    }
                });
    }



}
