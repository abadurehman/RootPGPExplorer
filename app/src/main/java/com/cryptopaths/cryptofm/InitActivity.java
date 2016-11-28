package com.cryptopaths.cryptofm;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.cryptopaths.cryptofm.encryption.DatabaseHandler;
import com.cryptopaths.cryptofm.encryption.KeyManagement;

import org.spongycastle.bcpg.ArmoredOutputStream;
import org.spongycastle.openpgp.PGPKeyRingGenerator;
import org.spongycastle.openpgp.PGPPublicKeyRing;
import org.spongycastle.openpgp.PGPSecretKeyRing;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.security.Security;


public class InitActivity extends AppCompatActivity {
    private int mFragmentNumber         =0;
    private static final String TAG     ="InitActivity";

    private ProgressBar         mProgressBar;
    private DatabaseHandler     mDatabaseHandler;
    private ProgressDialog      mLoading;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);
        //first of all check shared preferences
        SharedPreferences preferences   =getPreferences(Context.MODE_PRIVATE);
        boolean isNotFirstRun           =preferences.getBoolean("key",false);
        if(isNotFirstRun){
            //change activity to unlock db activity
            Intent intent=new Intent(this,UnlockDbActivity.class);
            startActivity(intent);

        }
        mProgressBar=(ProgressBar)findViewById(R.id.setup_progressbar);
        mProgressBar.setMax(100);

    }
    @ActionHandler
    public void onNextButtonClick(View v){
        switch (mFragmentNumber){
            case 0:
                EditText passwordEditText=
                        (EditText)findViewById(R.id.input_password_first_fragment);
                CharSequence sequence=passwordEditText.getText();
                if(isValidPassword(sequence)){
                    Log.d("fragment","replacing fragmnet "+mFragmentNumber);
                    //create encrypted database and set password of user choice
                    mDatabaseHandler=new DatabaseHandler(this,sequence.toString(),false);
                    //change fragment to next fragment
                    SecondFragment secondFragment=new SecondFragment();
                    getSupportFragmentManager().beginTransaction().
                            setCustomAnimations(R.anim.enter_from_right,R.anim.exit_to_left,
                                    R.anim.enter_from_left, R.anim.exit_to_right).
                            replace(R.id.first_fragment,secondFragment).
                            commit();
                    //set fragment number to 1
                    mFragmentNumber=1;
                    //set progress
                    mProgressBar.setProgress(33);

                }else{
                    passwordEditText.setError("password length should be greater than 3");
                }
                Log.d("fragment","replacing fragmnet "+mFragmentNumber);
                break;
            case 1:
                passwordEditText=(EditText)findViewById(R.id.input_password_second_fragment);
                EditText emailEditText=(EditText)findViewById(R.id.input_email_second_fragment);
                if(!isValidEmail(emailEditText.getText())){
                    emailEditText.setError("please enter valid email address");
                    return;
                }
                if(!isValidPassword(passwordEditText.getText())){
                    passwordEditText.setError("password length should be greater than 3");
                    return;
                }
                //generate keys
                new KeyGenerationTask().execute(
                        emailEditText.getText().toString(),
                        passwordEditText.getText().toString()
                );

                break;
            case 2:
                //choose dir and start encrypting it
                //TODO
                //change the button text to lets go
                ((AppCompatButton) v).setText("Let's Go");
                mFragmentNumber=3;
                mProgressBar.setProgress(100);
                break;
            case 3:
                //start the encrypting activity
                //TODO
                break;




        }
    }
    private boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }
    private boolean isValidPassword(CharSequence password){
        return password.length() > 2;
    }

    //async task for the generation of keys
    private class KeyGenerationTask extends AsyncTask<String,Void,byte[]> {

        @Override
        protected byte[] doInBackground(String... strings) {
            String email                =strings[0];
            char[] password             =strings[1].toCharArray();
            KeyManagement keyManagement =new KeyManagement();
            try {
                Log.d(TAG,"start generating keys");
                PGPKeyRingGenerator keyRingGenerator    =keyManagement.generateKey(email,password);
                PGPPublicKeyRing publicKeys             =keyRingGenerator.generatePublicKeyRing();
                PGPSecretKeyRing secretKeys             =keyRingGenerator.generateSecretKeyRing();

                //output keys in ascii armored format
                File file=new File(getFilesDir(),"pub.asc");
                ArmoredOutputStream pubOut=new ArmoredOutputStream(new FileOutputStream(file));
                publicKeys.encode(pubOut);
                pubOut.close();
                ByteArrayOutputStream outputStream  =new ByteArrayOutputStream();
                ArmoredOutputStream secOut          =new ArmoredOutputStream(outputStream);
                secretKeys.encode(secOut);
                secOut.close();
                byte[] test=outputStream.toByteArray();
                //call the db methods to store
                mDatabaseHandler.insertSecKey(email,test);
                //put in shared preferences
                SharedPreferences preferences=getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor=preferences.edit();
                editor.putBoolean("key",true);
                editor.putString("mail",email);
                editor.apply();
                editor.commit();
                Log.d(TAG,"secret key written to file");
                return  test;

            } catch (Exception e) {
                Log.d(TAG,"Error generating keys");
                e.printStackTrace();
                return null;

            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //show a progress dialog
            mLoading=new ProgressDialog(InitActivity.this);
            mLoading.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mLoading.setIndeterminate(true);
            mLoading.setTitle("Generating keys");
            mLoading.setMessage("Please wait while generating keys");
            mLoading.setCancelable(false);
            mLoading.show();

        }

        @Override
        protected void onPostExecute(byte[] s) {
            super.onPostExecute(s);
            mLoading.hide();
            //change fragment to third fragment
            ThirdFragment thirdFragment=new ThirdFragment();
            getSupportFragmentManager().beginTransaction().
                    setCustomAnimations(R.anim.enter_from_right,R.anim.exit_to_left,
                            R.anim.enter_from_left, R.anim.exit_to_right).
                    replace(R.id.first_fragment,thirdFragment).
                    commit();
            mFragmentNumber=2;
            mProgressBar.setProgress(66);
        }
    }
    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }
}