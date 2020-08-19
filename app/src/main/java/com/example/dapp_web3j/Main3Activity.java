package com.example.dapp_web3j;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Credentials;
import android.os.Bundle;

import org.web3j.protocol.Web3j;

public class Main3Activity extends AppCompatActivity {

    private Web3j web3j;
    private Credentials credentials;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        int choice = getIntent().getExtras().getInt("flag");
        web3j = (Web3j) getIntent().getSerializableExtra("Web3j");

        if(choice == 1){

            credentials = (Credentials) getIntent().getSerializableExtra("Credentials");

        }
        else {

            credentials = (Credentials) getIntent().getSerializableExtra("Credentials");

        }

    }
}
