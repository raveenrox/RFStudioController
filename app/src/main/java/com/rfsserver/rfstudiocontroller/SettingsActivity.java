package com.rfsserver.rfstudiocontroller;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class SettingsActivity extends Activity {

    private HelperClass helperClass;

    private EditText etName;
    private EditText etUsername;
    private EditText etPassword;
    private EditText etURL;
    private EditText etPort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        helperClass = HelperClass.getInstance();

        etName = (EditText) findViewById(R.id.etName);
        etUsername = (EditText) findViewById(R.id.etUsername);
        etPassword = (EditText) findViewById(R.id.etPassword);
        etURL = (EditText) findViewById(R.id.etURL);
        etPort = (EditText) findViewById(R.id.etPort);

        loadUserDetails();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

       /* if (id == R.id.action_settings) {
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    public void buttonClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.btnSave:
                saveUserDetails();
                break;
            case R.id.btnRestore:
                loadUserDetails();
                break;
            case R.id.btnClear:
                etName.setText("");
                etUsername.setText("");
                etPassword.setText("");
                etURL.setText("");
                etPort.setText("");
                break;
        }
    }

    private void saveUserDetails() {
        if(!etName.getText().toString().isEmpty() && !etUsername.getText().toString().isEmpty() &&
                !etPassword.getText().toString().isEmpty() && !etURL.getText().toString().isEmpty())
        {
            SharedPreferences.Editor editor = helperClass.preferences.edit();
            editor.putString("name", etName.getText().toString());
            editor.putString("username", etUsername.getText().toString());
            editor.putString("password", etPassword.getText().toString());
            editor.putString("url", etURL.getText().toString());
            editor.putInt("port", Integer.parseInt(etPort.getText().toString()));
            if(!helperClass.preferences.getBoolean("initialized", false)) {
                editor.putBoolean("initialized", true);
                editor.apply();
                Intent intent = new Intent(this, SplashScreenActivity.class);
                startActivity(intent);
                finish();
            }
            editor.apply();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();

        }else
        {
            Toast.makeText(this, "Fill the form correctly", Toast.LENGTH_LONG).show();
        }
    }

    private void loadUserDetails() {
        etName.setText(helperClass.preferences.getString("name", ""));
        etUsername.setText(helperClass.preferences.getString("username", ""));
        etPassword.setText(helperClass.preferences.getString("password", ""));
        etURL.setText(helperClass.preferences.getString("url", ""));
        etPort.setText(Integer.toString(helperClass.preferences.getInt("port", 0)));
    }
}
