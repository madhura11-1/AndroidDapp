package com.example.dapp_web3j;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;

import org.web3j.crypto.Bip32ECKeyPair;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.MnemonicUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;

import java.math.BigDecimal;

public class Main2Activity extends AppCompatActivity {

    private EditText mnemonic,amount,to;
    private TextView address,balance;
    private Button import_wallet,transfer,getbalance;
    private  String mnemonicS,pass;
    private Web3j web3j;
    private Bip32ECKeyPair masterKeypair;
    private Bip32ECKeyPair  derivedKeyPair;
    private int[] derivationPath = {44 | Bip32ECKeyPair.HARDENED_BIT, 60 | Bip32ECKeyPair.HARDENED_BIT, 0 | Bip32ECKeyPair.HARDENED_BIT, 0,0};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        setGlobalvariables();
        connectToEthereum();
        setOnClicklistner();

    }

    private void setOnClicklistner() {

        import_wallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mnemonicS = mnemonic.getText().toString().trim();
                if(mnemonicS.isEmpty()){
                    Toast.makeText(Main2Activity.this, "Please enter the mnemonic", Toast.LENGTH_SHORT).show();
                }
                else{
                    try {
                        pass = null;

                        // Generate a BIP32 master keypair from the mnemonic phrase
                        masterKeypair = Bip32ECKeyPair.generateKeyPair(MnemonicUtils.generateSeed(mnemonicS, pass));

                        // Derived the key using the derivation path
                        derivedKeyPair = Bip32ECKeyPair.deriveKeyPair(masterKeypair, derivationPath);

                        // Load the wallet for the derived key
                        Credentials credentials = Credentials.create(derivedKeyPair);
                        Toast.makeText(Main2Activity.this, credentials.getAddress(), Toast.LENGTH_SHORT).show();
                        //EthGetBalance ethGetBalance = web3j.ethGetBalance(credentials.getAddress(), DefaultBlockParameterName.LATEST).sendAsync().get();

                    } catch (Exception e) {
                        Toast.makeText(Main2Activity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        getbalance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Credentials credentials = Credentials.create(derivedKeyPair);
                    EthGetBalance ethGetBalance = web3j.ethGetBalance(credentials.getAddress(), DefaultBlockParameterName.LATEST).sendAsync().get();
                    address.setText(credentials.getAddress());
                    balance.setText((Convert.fromWei(ethGetBalance.getBalance().toString(), Convert.Unit.ETHER)).toString());

                }catch (Exception e){
                    Toast.makeText(Main2Activity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        });

        transfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Credentials credentials = Credentials.create(derivedKeyPair);
                    Double amt = Double.parseDouble(amount.getText().toString().trim());
                    String to1 = to.getText().toString();
                    TransactionReceipt transactionReceipt = Transfer.sendFunds(web3j, credentials,to1, BigDecimal.valueOf(amt), Convert.Unit.ETHER).sendAsync().get();
                }catch (Exception e){
                    Toast.makeText(Main2Activity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void connectToEthereum() {
        CustomDialog customDialog = new CustomDialog();
        customDialog.showDialog();
        web3j = Web3j.build(new HttpService("https://rinkeby.infura.io/v3/bbc32ee079884ad9a6115dbc37904c10"));
        try {
            Web3ClientVersion clientVersion = web3j.web3ClientVersion().sendAsync().get();
            if (!clientVersion.hasError()) {
                customDialog.closeDialog();
            } else {
                Toast.makeText(this, clientVersion.getError().getMessage(), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();;
        }

    }

    public class CustomDialog {

        Dialog dialog;
        LottieAnimationView lottieAnimationView;
        TextView loading;
        public void showDialog() {
            dialog = new Dialog(Main2Activity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.dialog);
//            lottieAnimationView = findViewById(R.id.animationView);

            dialog.show();
        }

        public void closeDialog(){
      /*      loading = findViewById(R.id.loading);
            loading.setText("Connected :)");*/
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    dialog.dismiss();
                }
            },3000);
        }

    }

    private void setGlobalvariables() {
        mnemonic = findViewById(R.id.mnemonic);
        import_wallet = findViewById(R.id.import_button);
        amount = findViewById(R.id.amount);
        transfer = findViewById(R.id.send_ether);
        address = findViewById(R.id.address);
        balance = findViewById(R.id.balance);
        to = findViewById(R.id.to);
        getbalance = findViewById(R.id.import_address);
    }
}
