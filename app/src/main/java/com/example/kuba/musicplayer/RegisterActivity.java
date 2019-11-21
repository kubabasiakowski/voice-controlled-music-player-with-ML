package com.example.kuba.musicplayer;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity extends AppCompatActivity {

    EditText editTextName, editTextSurname, editTextEmail, editTextPassword, editTextPassword2;
    Button registerButton;
    DatabaseHelper myDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //inicializacja elementow
        editTextName = (EditText) findViewById(R.id.editTextName);
        editTextSurname = (EditText) findViewById(R.id.editTextSurname);
        editTextEmail = (EditText) findViewById(R.id.editTextMail);
        editTextPassword = (EditText) findViewById(R.id.editTextPasswd);
        editTextPassword2 = (EditText) findViewById(R.id.editTextPasswd2);
        registerButton = (Button) findViewById(R.id.btnRegisterNew);
        myDb = new DatabaseHelper(this);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validate()) {
                        myDb.addUser(editTextName.getText().toString(), editTextSurname.getText().toString(), editTextEmail.getText().toString(), editTextPassword.getText().toString());
                        Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
                        RegisterActivity.this.startActivity(loginIntent);
                }
            }
        });
    }

    private Boolean validate(){
        Boolean result = false;

        String name = editTextName.getText().toString();
        String surname = editTextSurname.getText().toString();
        String email = editTextEmail.getText().toString();
        String password = editTextPassword.getText().toString();
        String password2 = editTextPassword2 .getText().toString();

        if(name.isEmpty() || surname.isEmpty() || email.isEmpty() || password.isEmpty() || password2.isEmpty())
        {
            Toast.makeText(this, "Uzupełnij wszystkie pola.", Toast.LENGTH_SHORT).show();
        }
        else if (!password.equals(password2))
        {
            Toast.makeText(this, "Podane hasła nie są takie same.", Toast.LENGTH_SHORT).show();
        }
        else if (myDb.checkIfExists(email))
        {
            Toast.makeText(this, "Podany email już istnieje.", Toast.LENGTH_SHORT).show();
        }
        else
        {
            result = true;
        }
        return result;
    }
}
