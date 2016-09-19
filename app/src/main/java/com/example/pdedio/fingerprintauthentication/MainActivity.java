package com.example.pdedio.fingerprintauthentication;

import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class MainActivity extends AppCompatActivity {

    private static final String KEY_NAME = "MyKey";

    private FingerprintManager fingerprintManager;

    private Cipher cipher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        try {
            setContentView(R.layout.activity_main);

            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

            int purpose = KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT;
            String padding = KeyProperties.ENCRYPTION_PADDING_PKCS7;

            keyGenerator.init(new KeyGenParameterSpec.Builder(KEY_NAME, purpose)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(padding)
                    .build());

            keyGenerator.generateKey();

            this.cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                    + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7);

            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            SecretKey secretKey = (SecretKey) keyStore.getKey(KEY_NAME, null);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            this.fingerprintManager = (FingerprintManager) this.getSystemService(Context.FINGERPRINT_SERVICE);
            this.authenticate(new Callback());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void authenticate(FingerprintManager.AuthenticationCallback callback) {
        fingerprintManager.authenticate(new FingerprintManager.CryptoObject(this.cipher), null, 0, callback, null);
    }

    private class Callback extends FingerprintManager.AuthenticationCallback {

        @Override
        public void onAuthenticationFailed() {
            super.onAuthenticationFailed();
            Toast.makeText(MainActivity.this, "Cannot verify your fingerprint, please try again",Toast.LENGTH_SHORT).show();
            authenticate(this);
        }

        @Override
        public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
            Toast.makeText(MainActivity.this, "Your access have been granted",Toast.LENGTH_LONG).show();
        }
    }
}
