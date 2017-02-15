/*
 * Copyright (c) 2017. Osama Bin Omar
 *    This file is part of Crypto File Manager also known as Crypto FM
 *
 *     Crypto File Manager is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Crypto File Manager is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Crypto File Manager.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.osama.cryptofm.startup;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.osama.cryptofm.R;
import com.osama.cryptofm.encryption.DatabaseHandler;
import com.osama.cryptofm.filemanager.ui.FilemanagerTabs;
import com.osama.cryptofm.startup.fragments.InitActivityFirstFragment;
import com.osama.cryptofm.startup.fragments.InitActivityThirdFragment;
import com.osama.cryptofm.utils.ActionHandler;

import net.sqlcipher.database.SQLiteDatabase;

import java.security.Security;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;


public class InitActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks , InitActivityThirdFragment.FragmentCreated{
    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }
    private static final String TAG          = "InitActivity";
    private static final int RC_PERMISSION   = 101;


    private String  mUserSecretDatabase;
    private String  mUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: Creating activity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);
        SQLiteDatabase.loadLibs(this);
        //add first fragment
        getSupportFragmentManager().beginTransaction().
                setCustomAnimations(R.anim.enter_from_right,R.anim.exit_to_left,
                        R.anim.enter_from_left, R.anim.exit_to_right).
                replace(R.id.fragment_frame_layout, new InitActivityFirstFragment()).
                commit();


    }

    @ActionHandler(layoutResource = R.id.next_button)
    public void onNextButtonClick(View v){
        EditText passwordEditText       = (EditText)findViewById(R.id.password);
        EditText passwordConfirm1       = (EditText)findViewById(R.id.password_confirm);
        EditText usernameEdit           = (EditText)findViewById(R.id.username_edittext);

        String errorMessageLength       = "password length should be greater than 3";
        String errorMessageMatch        = "password does not match";
        CharSequence sequence           = passwordEditText.getText();
        CharSequence sequenceConfirm    = passwordConfirm1.getText();

        if(usernameEdit.getText().toString().length()<1){
            usernameEdit.setError("Please give me your name");
            return;
        }
        mUserName=usernameEdit.getText().toString();
        if(isValidPassword(sequence)){
            if(sequence.toString().equals(sequenceConfirm.toString())) {
                Log.d("password","one password and two: "+sequence +" : "+sequenceConfirm);
                mUserSecretDatabase=sequence.toString();
                    Boolean permission=checkPermissions();
                    if(permission){
                        //replace fragment to second fragment
                        Log.d(TAG, "onNextButtonClick: aor mera b bnta hai");
                       // replaceFragment(FRAGMENT_TWO_NUMBER);
                        new DatabaseSetupTask().execute();

                    } else{
                        // get read and write storage permission
                        Log.d(TAG, "onNextButtonClick: mera execute hona bnta hai");
                        //do nothing
                    }

                }else{
                    passwordConfirm1.setError(errorMessageMatch);
                }
            }else{
                passwordEditText.setError(errorMessageLength);
            }

    }

    private void commitInitActivity() {
        //put in shared preferences
        SharedPreferences preferences=getSharedPreferences("done",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=preferences.edit();
        editor.putBoolean("done",true);
        editor.putString("username",mUserName);
        editor.apply();
        editor.commit();
    }

    private boolean isValidPassword(CharSequence password){
        return password.length() > 2;
    }

    /*
    Permission area
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        Log.d(TAG, "onPermissionsGranted: permission granted");
        if (requestCode==RC_PERMISSION){
            new DatabaseSetupTask().execute();

        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Log.d(TAG, "onPermissionsDenied: permission denied");

    }
    @AfterPermissionGranted(RC_PERMISSION )
    private boolean checkPermissions(){
        Log.d(TAG, "checkPermissions: im called dsds");
        String[] perms  = {Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE
                };
        if(EasyPermissions.hasPermissions(this,perms)){
               return true;
        }else{
            EasyPermissions.requestPermissions(this,getString(R.string.permission_string),
                    RC_PERMISSION,perms);
        }
        return false;
    }
    private boolean permissionGranted=false;
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode){
            case RC_PERMISSION:{
                if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Log.d(TAG,"permissions granted");
                    permissionGranted=true;
                }
            }
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: Resuming activity");
        super.onResume();
        if(permissionGranted){
            new DatabaseSetupTask().execute();
        }


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onThirdFragmentCreated() {
        Log.d("fragment","yes the fragment created");
    }


    private ProgressDialog dialog;
    private class DatabaseSetupTask extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... voids) {
          new DatabaseHandler(
                    InitActivity.this,
                    mUserSecretDatabase,
                    false
            );
            return null;
        }

        @Override
        protected void onPreExecute() {
            permissionGranted   =false;
            dialog              =new ProgressDialog(InitActivity.this);
            dialog.setIndeterminate(true);
            dialog.setMessage("Settings up application");
            dialog.setTitle("Application setup");
            dialog.setCancelable(false);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
           dialog.dismiss();
            commitInitActivity();
            //start intermediateActivity
            Intent intent = new Intent(InitActivity.this,FilemanagerTabs.class);
            intent.putExtra("dbpass",mUserSecretDatabase);
            intent.putExtra("username",mUserName);
            //this flag will remove all the previous activities from backstack
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivityForResult(intent,1);
            finish();
        }
    }



}
