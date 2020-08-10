package com.example.dapp_web3j;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.airbnb.lottie.LottieAnimationView;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;

import java.io.File;
import java.math.BigDecimal;
import java.security.Provider;
import java.security.Security;

public class MainActivity extends AppCompatActivity {

    private Button create_wallet, get_address, send_ether;
    private EditText password, password1, wallet_name1, amount, to;
    private TextView wallet_name, import_wallet, address, balance;
    private ImageButton connect;
    private String walletDirectory, walletName;
    private Web3j web3j;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupBouncyCastle();
        setGlobalvariabls();
        setOnclicklistners();
    }

    private void setOnclicklistners() {

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectToEthereum();
            }
        });

        create_wallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    String password_string = password.getText().toString().trim();
                    if (password_string.isEmpty()) {
                        Toast.makeText(MainActivity.this, "Please enter a password", Toast.LENGTH_SHORT).show();
                    } else {
                        walletName = WalletUtils.generateNewWalletFile(password_string, new File(walletDirectory));
                        wallet_name.setText(walletName);

                        System.out.println("wallet location: " + walletDirectory + "/" + walletName);
                    }
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        });

        get_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String walletPassword = password1.getText().toString().trim();
                    String enterName = wallet_name1.getText().toString().trim();
                    if (walletPassword.isEmpty() || enterName.isEmpty()) {
                        Toast.makeText(MainActivity.this, "Please fill all the details", Toast.LENGTH_SHORT).show();
                    } else {
                        Credentials credentials = WalletUtils.loadCredentials(walletPassword, walletDirectory + "/" + enterName);
                        System.out.println("Your address is " + credentials.getAddress());

                        EthGetBalance ethGetBalance = web3j.ethGetBalance(credentials.getAddress(), DefaultBlockParameterName.LATEST)
                                .sendAsync().get();
                        System.out.println((Convert.fromWei(ethGetBalance.getBalance().toString(), Convert.Unit.ETHER)).toString());

                        address.setText(credentials.getAddress());
                        balance.setText((Convert.fromWei(ethGetBalance.getBalance().toString(), Convert.Unit.ETHER)).toString());
                    }
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        send_ether.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String walletPassword = password1.getText().toString().trim();
                    String enterName = wallet_name1.getText().toString().trim();
                    String amount1 = amount.getText().toString().trim();
                    Double amt = Double.parseDouble(amount1);
                    String to1 = to.getText().toString().trim();
                    if (walletPassword.isEmpty() || enterName.isEmpty() || amount1.isEmpty() || to1.isEmpty()) {
                        Toast.makeText(MainActivity.this, "Please fill all the details i.e. wallet password and wallet name too", Toast.LENGTH_SHORT).show();
                    } else {
                        Credentials credentials = WalletUtils.loadCredentials(walletPassword, walletDirectory + "/" + enterName);
                        EthGetBalance ethGetBalance = web3j.ethGetBalance(credentials.getAddress(), DefaultBlockParameterName.LATEST)
                                .sendAsync().get();
                        System.out.println("Status" + credentials.getAddress() + " " + (Convert.fromWei(ethGetBalance.getBalance().toString(), Convert.Unit.ETHER)).toString());
                        TransactionReceipt transactionReceipt = Transfer.sendFunds(web3j, credentials, to1, BigDecimal.valueOf(amt), Convert.Unit.ETHER).sendAsync().get();
                        //TransactionReceipt receipt = Transfer.sendFunds(web3,credentials,"0xD931DFE25660081fc25729c6114D96Cc131cA9d1",BigDecimal.valueOf(1),Convert.Unit.ETHER).send();
                        Toast.makeText(MainActivity.this, "Transaction complete: " + transactionReceipt.getTransactionHash(), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        import_wallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Main2Activity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

    }

    private void connectToEthereum() {
        web3j = Web3j.build(new HttpService("https://rinkeby.infura.io/v3/bbc32ee079884ad9a6115dbc37904c10"));
        try {
            Web3ClientVersion clientVersion = web3j.web3ClientVersion().sendAsync().get();
            if (!clientVersion.hasError()) {
                Resources res = getApplicationContext().getResources();
                connect.setBackgroundColor(Color.RED);
                Toast.makeText(this, "Connected", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, clientVersion.getError().getMessage(), Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

    public class CustomDialog {

        Dialog dialog;
        LottieAnimationView lottieAnimationView;
        TextView loading;

        public void showDialog() {
            dialog = new Dialog(MainActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.dialog);
            //lottieAnimationView = findViewById(R.id.animationView);

            dialog.show();
        }

        public void closeDialog() {
            /*dialog.setContentView(R.layout.dialog);
            loading = findViewById(R.id.loading);
            loading.setText("Connected :)");*/
            dialog.dismiss();
            /*Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                     dialog.dismiss();
                }
            },3000);*/
        }

    }

    private void setupBouncyCastle() {                                                           //Android uses the Bouncy Castle Java libraries by default to
        final Provider provider = Security.getProvider(BouncyCastleProvider.PROVIDER_NAME);      //implement some of its cryptographic functionality
        if (provider == null) {                                                                  //But Android included a shortened version of Bouncycastle, and there is no full support for ECDSA.
            // Web3j will set up the provider lazily when it's first used.                       //KeyPairGenerator/ECDSA is not supported, which is the required one to generate ethereum keys.
            return;                                                                              // So we are providing it manually here
        }
        if (provider.getClass().equals(BouncyCastleProvider.class)) {
            // BC with same package name, shouldn't happen in real life.
            return;
        }
        // Android registers its own BC provider. As it might be outdated and might not include
        // all needed ciphers, we substitute it with a known BC bundled in the app.
        // Android's BC has its package rewritten to "com.android.org.bouncycastle" and because
        // of that it's possible to have another BC implementation loaded in VM.
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        Security.insertProviderAt(new BouncyCastleProvider(), 1);
    }

    private void setGlobalvariabls() {
        create_wallet = findViewById(R.id.create_wallet);
        get_address = findViewById(R.id.get_Address);
        send_ether = findViewById(R.id.send_ether);
        password = findViewById(R.id.password);
        password1 = findViewById(R.id.password1);
        wallet_name = findViewById(R.id.wallet_name);
        wallet_name1 = findViewById(R.id.wallet_name1);
        amount = findViewById(R.id.amount);
        to = findViewById(R.id.to);
        address = findViewById(R.id.address);
        balance = findViewById(R.id.balance);
        connect = findViewById(R.id.connect);
        import_wallet = findViewById(R.id.import_account);
        walletDirectory = getFilesDir().getAbsolutePath();
        import_wallet.setPaintFlags(import_wallet.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
    }
}
