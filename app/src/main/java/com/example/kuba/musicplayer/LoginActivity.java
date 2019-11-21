package com.example.kuba.musicplayer;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    EditText eMail, password;
    Button btnLogin, btnRegister;
    DatabaseHelper dbHelper;
    SharedPreferencesConfig preferencesConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new DatabaseHelper(this);

        //inicializacja elementow aktywnosci
        eMail = (EditText) findViewById(R.id.editTextEMail);
        password = (EditText) findViewById(R.id.editTextPassword);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnRegister = (Button) findViewById(R.id.btnRegister);
        preferencesConfig = new SharedPreferencesConfig(getApplicationContext());

        if(preferencesConfig.readLoginStatus()){
            Intent loginIntent = new Intent(LoginActivity.this, MainActivity.class);
            LoginActivity.this.startActivity(loginIntent);
            finish();
        }

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                LoginActivity.this.startActivity(registerIntent);
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String email = eMail.getText().toString();
                String passwd = password.getText().toString();

                if(dbHelper.login(email,passwd)!=0){
                    Intent loginIntent = new Intent(LoginActivity.this, MainActivity.class);
                    LoginActivity.this.startActivity(loginIntent);
                    preferencesConfig.writeLoginStatus(true);
                    preferencesConfig.writeUserId(dbHelper.login(email,passwd));
                }
                else{
                    Toast.makeText(LoginActivity.this, "Niepoprawne dane logowania.",Toast.LENGTH_SHORT).show();
                    eMail.setText("");
                    password.setText("");
                }
            }
        });
    }
}
