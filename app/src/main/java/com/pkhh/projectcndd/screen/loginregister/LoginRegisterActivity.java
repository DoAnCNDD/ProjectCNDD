package com.pkhh.projectcndd.screen.loginregister;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.pkhh.projectcndd.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import static java.util.Objects.requireNonNull;

public final class LoginRegisterActivity extends AppCompatActivity implements LoginFragment.Listener, RegisterFragment.Listener {
  public static final String TAG = LoginRegisterActivity.class.getSimpleName();
  public static final String REGISTER_FRAGMENT = "REGISTER_FRAGMENT";
  public static final String LOGIN_FRAGMENT = "LOGIN_FRAGMENT";
  private boolean isSuccessfully = false;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login_register);
    requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

    if (savedInstanceState == null) {
      getSupportFragmentManager()
          .beginTransaction()
          .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
              R.anim.slide_in_left, R.anim.slide_out_right)
          .add(R.id.login_register_container, new LoginFragment(), LOGIN_FRAGMENT)
          .commit();
    }
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
        getSupportFragmentManager().popBackStack();
      } else {
        finish();
      }
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onRegisterClick() {
    getSupportFragmentManager()
        .beginTransaction()
        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
            R.anim.slide_in_left, R.anim.slide_out_right)
        .replace(R.id.login_register_container, new RegisterFragment(), REGISTER_FRAGMENT)
        .addToBackStack(null)
        .commit();
  }

  @Override
  public void onLoginSuccessfully() {
    isSuccessfully = true;
    Toast.makeText(this, getString(R.string.login_successfully), Toast.LENGTH_SHORT).show();
    finish();
  }

  @Override
  public void onLoginClick() {
    getSupportFragmentManager().popBackStack();
  }

  @Override
  public void onRegisterSuccessfully() {
    isSuccessfully = true;
    Toast.makeText(this, getString(R.string.register_successfully), Toast.LENGTH_SHORT).show();
    finish();
  }

  @Override
  public void finish() {

    if (isSuccessfully) {
      setResult(RESULT_OK);
    } else {
      setResult(RESULT_CANCELED);
    }
    super.finish();
  }
}
