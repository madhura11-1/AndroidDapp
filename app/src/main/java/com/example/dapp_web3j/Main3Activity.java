package com.example.dapp_web3j;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.web3j.crypto.Bip32ECKeyPair;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.MnemonicUtils;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.DefaultGasProvider;

import java.io.IOException;
import java.lang.ref.WeakReference;

public class Main3Activity extends AppCompatActivity {

    private EditText registerName,registerAddress,inputAddress;
    private Button register,view,deploy,load;
    private TextView viewName,viewAddress;
    private Web3j web3j;
    private Credentials credentials;
    private int choice;
    private Register registerClass;
    private String password,walletName,walletDirectory;
    private Bip32ECKeyPair masterKeypair;
    private Bip32ECKeyPair derivedKeyPair;
    private int[] derivationPath = {44 | Bip32ECKeyPair.HARDENED_BIT, 60 | Bip32ECKeyPair.HARDENED_BIT, 0 | Bip32ECKeyPair.HARDENED_BIT, 0, 0};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        Intent intent = getIntent();
        connecttoEthereum();
        getIntentParameters(intent);
        setGlobalVariables();
        setOnClickListeners();

        if(choice == 1){

            try {

                walletName = intent.getExtras().getString("walletName");
                password = intent.getExtras().getString("Password");
                credentials = WalletUtils.loadCredentials(password, walletDirectory + "/" + walletName);
                Toast.makeText(this, credentials.getAddress(), Toast.LENGTH_SHORT).show();

            } catch (IOException e) {
                e.printStackTrace();
            } catch (CipherException e) {
                e.printStackTrace();
            }

        }
        else {

            try {
                String mnemonicS = getIntent().getExtras().getString("mnemonic");
                String pass = null;

                // Generate a BIP32 master keypair from the mnemonic phrase
                masterKeypair = Bip32ECKeyPair.generateKeyPair(MnemonicUtils.generateSeed(mnemonicS, pass));

                // Derived the key using the derivation path
                derivedKeyPair = Bip32ECKeyPair.deriveKeyPair(masterKeypair, derivationPath);

                // Load the wallet for the derived key
                credentials = Credentials.create(derivedKeyPair);
                //Toast.makeText(Main3Activity.this, credentials.getAddress(), Toast.LENGTH_SHORT).show();
                //EthGetBalance ethGetBalance = web3j.ethGetBalance(credentials.getAddress(), DefaultBlockParameterName.LATEST).sendAsync().get();

            } catch (Exception e) {
                Toast.makeText(Main3Activity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void setOnClickListeners() {

         deploy.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 AsyncClass asyncClass = new AsyncClass(Main3Activity.this);
                 asyncClass.execute();
             }
         });

         load.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {

                 AsyncClass2 asyncClass2 = new AsyncClass2(Main3Activity.this);
                 asyncClass2.execute();

             }
         });

         register.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 String name = registerName.getText().toString().trim();
                 String addressN = registerAddress.getText().toString().trim();
                 if(name.isEmpty() || addressN.isEmpty()){
                     Toast.makeText(Main3Activity.this, "Enter the required fields", Toast.LENGTH_SHORT).show();
                 }else{
                    // Toast.makeText(Main3Activity.this, registerClass.getContractAddress(), Toast.LENGTH_SHORT).show();
                     try {
                         TransactionReceipt receipt = registerClass.registerUser(name,addressN).sendAsync().get();
                         Toast.makeText(Main3Activity.this, receipt.getFrom() + "\n" + receipt.getTo(), Toast.LENGTH_LONG).show();
                     } catch (Exception e) {
                         Toast.makeText(Main3Activity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                     }
                 }
             }
         });

         view.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 String add = inputAddress.getText().toString().trim();
                 if(add.isEmpty()){
                     Toast.makeText(Main3Activity.this, "Please enter smtg", Toast.LENGTH_SHORT).show();
                 }else{
                     AsyncClass3 asyncClass3 = new AsyncClass3(Main3Activity.this);
                     asyncClass3.execute(add);
                 }
             }
         });

    }


    private void getIntentParameters(Intent intent) {

        choice = intent.getExtras().getInt("flag");

    }

    /*public void showToast(String msg){

        Handler handler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message m){
                Toast.makeText(Main3Activity.this, msg, Toast.LENGTH_SHORT).show();
            }
        };
    }*/

    private void setGlobalVariables() {

        walletDirectory = getFilesDir().getAbsolutePath();
        register = findViewById(R.id.register);
        registerAddress = findViewById(R.id.register_address);
        registerName = findViewById(R.id.register_name);
        inputAddress = findViewById(R.id.type_address);
        view = findViewById(R.id.button_address);
        viewAddress = findViewById(R.id.view_address);
        viewName = findViewById(R.id.view_name);
        deploy = findViewById(R.id.deploy);
        load = findViewById(R.id.load);

    }

    private void connecttoEthereum() {

        web3j = Web3j.build(new HttpService("https://rinkeby.infura.io/v3/bbc32ee079884ad9a6115dbc37904c10"));
        try {
            Web3ClientVersion clientVersion = web3j.web3ClientVersion().sendAsync().get();
            if (!clientVersion.hasError()) {
                Toast.makeText(this, "Connected", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, clientVersion.getError().getMessage(), Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    private static class AsyncClass extends AsyncTask<Void,Void,String>{

        private WeakReference<Main3Activity> activityWeakReference;

        AsyncClass(Main3Activity activity){
            activityWeakReference = new WeakReference<Main3Activity>(activity);
        }

        @Override
        protected String doInBackground(Void... voids) {
            int flag = 0;
            Register registerContract = null;
            Main3Activity activity = activityWeakReference.get();
            if(activity == null || activity.isFinishing()){
                return "Activity null";
            }
             try {
                     registerContract = Register.deploy(activity.web3j, activity.credentials, new DefaultGasProvider()).send();
                     flag =1;

                 } catch (Exception e) {
                    flag =0;
                 }
             if(flag == 1){
                 return registerContract.getContractAddress();
             }
             else{
                 return "Not done";
             }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            Main3Activity activity = activityWeakReference.get();
            if(activity == null || activity.isFinishing()){
                return;
            }

            activity.viewName.setText(s);
            Toast.makeText(activity, s, Toast.LENGTH_LONG).show();

        }
    }

     private static class AsyncClass2 extends AsyncTask<Void,Void,String> {

        private WeakReference<Main3Activity> weakReference;
        int flag =0;

         AsyncClass2(Main3Activity activity){
             weakReference = new WeakReference<Main3Activity>(activity);
         }

         @Override
         protected String doInBackground(Void... voids) {
             Main3Activity activity = weakReference.get();
             if(activity == null || activity.isFinishing()){
                 return "Activity null";
             }
             try {
                 activity.registerClass = Register.load("0xf801215bc7ba640f4f196fceed2cc231698d74b6",activity.web3j,activity.credentials,new DefaultGasProvider());

                 if(activity.registerClass.isValid()){
                     flag = 1;
                 }
                 else{
                     flag = 0;
                 }
             } catch (IOException e) {
                 Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
             }

             if(flag == 1){
                 return activity.registerClass.getContractAddress() + "  Happy;)";
             }
             else{
                 return "Async2 failed";
             }
         }

         @Override
         protected void onPostExecute(String s) {
             super.onPostExecute(s);

             Main3Activity activity = weakReference.get();
             if(activity == null || activity.isFinishing()){
                 return;
             }
             Toast.makeText(activity, s, Toast.LENGTH_LONG).show();
         }
     }

     private static class AsyncClass3 extends AsyncTask<String,Void,String>{
         private WeakReference<Main3Activity> weakReference;
         int flag =0;

         AsyncClass3(Main3Activity activity){
             weakReference = new WeakReference<Main3Activity>(activity);
         }

         @Override
         protected String doInBackground(String... strings) {
             Main3Activity activity = weakReference.get();
             if(activity == null || activity.isFinishing()){
                 return "Activity null";
             }
             String nameA;
             try {
                 TransactionReceipt receipt = activity.registerClass.viewDetials(strings[0]).sendAsync().get();
                 activity.registerClass.viewedEventFlowable(DefaultBlockParameterName.EARLIEST, DefaultBlockParameterName.LATEST)
                         .subscribe(event -> {
                             //nameA = event.name;
                             //addressA = event.address1;
                         }).toString();
             } catch (Exception e) {
                 Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
             }

             //return nameA + " " + addressA;
             return null;
         }



         //activity.viewName.setText(name[0]);
                // activity.viewAddress.setText(address[0]);
     }
}
