package com.pkhh.projectcndd.screen.loginregister;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.pkhh.projectcndd.R;
import com.pkhh.projectcndd.utils.Constants;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static android.app.Activity.RESULT_OK;
import static com.pkhh.projectcndd.utils.FirebaseUtil.getMessageFromFirebaseAuthExceptionErrorCode;
import static java.util.Objects.requireNonNull;

public class RegisterFragment extends Fragment {
  private static final int CHOOSE_AVATAR_IMAGE_REQUEST_CODE = 1;

  @BindView(R.id.image_avatar) ImageView mImageAvatar;
  @BindView(R.id.edit_full_name) TextInputLayout mEditName;
  @BindView(R.id.edit_email) TextInputLayout mEditEmail;
  @BindView(R.id.edit_password) TextInputLayout mEditPassword;
  @BindView(R.id.button_register) Button mButtonRegister;
  private ProgressDialog mProgressDialog;

  private FirebaseAuth mFirebaseAuth;
  private FirebaseFirestore mFirebaseFirestore;
  private FirebaseStorage mFirebaseStorage;

  private Listener mListener;
  @Nullable private Uri mSelectedImageUri;
  private Unbinder unbinder;

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    mListener = (Listener) context;
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_register, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    unbinder = ButterKnife.bind(this, view);

    initView();

    mFirebaseAuth = FirebaseAuth.getInstance();
    mFirebaseFirestore = FirebaseFirestore.getInstance();
    mFirebaseStorage = FirebaseStorage.getInstance();
  }

  private void initView() {
    requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setTitle(getString(R.string.register));

    requireNonNull(mEditName.getEditText()).addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        final String name = s.toString();
        if (name.length() < 3) {
          mEditName.setError(getString(R.string.min_length_name_is_3));
        } else {
          mEditName.setError(null);
        }
      }

      @Override
      public void afterTextChanged(Editable s) { }
    });
    requireNonNull(mEditEmail.getEditText()).addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        final String email = s.toString();
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
          mEditEmail.setError(getString(R.string.invalid_email_address));
        } else {
          mEditEmail.setError(null);
        }
      }

      @Override
      public void afterTextChanged(Editable s) { }
    });
    requireNonNull(mEditPassword.getEditText()).addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        final String password = s.toString();
        if (password.length() < 6) {
          mEditPassword.setError(getString(R.string.min_length_password_is_6));
        } else {
          mEditPassword.setError(null);
        }
      }

      @Override
      public void afterTextChanged(Editable s) { }
    });

    requireNonNull(mEditName.getEditText()).setText("");
    requireNonNull(mEditEmail.getEditText()).setText("");
    requireNonNull(mEditPassword.getEditText()).setText("");

    Picasso.get()
        .load(R.drawable.avatar_default_icon)
        .fit()
        .centerCrop()
        .into(mImageAvatar);
  }


  @OnClick({
      R.id.button_register,
      R.id.button_back_to_login,
      R.id.image_avatar
  })
  public void onClick(@NonNull View v) {
    switch (v.getId()) {
      case R.id.button_register:
        onRegister();
        break;
      case R.id.button_back_to_login:
        mListener.onLoginClick();
        break;
      case R.id.image_avatar:
        final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, getString(R.string.choose_avatar)), CHOOSE_AVATAR_IMAGE_REQUEST_CODE);
        break;
    }
  }

  private void onRegister() {
    boolean isValid = true;

    final String email = requireNonNull(mEditEmail.getEditText()).getText().toString();
    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
      isValid = false;
    }

    final String password = requireNonNull(mEditPassword.getEditText()).getText().toString();
    if (password.length() < 6) {
      isValid = false;
    }

    final String name = requireNonNull(mEditName.getEditText()).getText().toString();
    if (name.length() < 3) {
      isValid = false;
    }

    if (!isValid) {
      return;
    }

    mProgressDialog = new ProgressDialog(requireContext());
    mProgressDialog.setTitle(getString(R.string.processing));
    mProgressDialog.setMessage(getString(R.string.please_wait));
    mProgressDialog.show();
    mProgressDialog.setCancelable(false);
    mButtonRegister.setEnabled(false);

    mFirebaseAuth.createUserWithEmailAndPassword(email, password)
        .addOnSuccessListener(requireActivity(), authResult -> {
          final String uid = authResult.getUser().getUid();

          final Map<String, Object> user = new HashMap<>();
          user.put("email", email);
          user.put("full_name", name);
          user.put("is_active", true);
          user.put("phone", "");
          user.put("address", "");
          user.put("avatar", "");
          user.put("created_at", FieldValue.serverTimestamp());

          if (mSelectedImageUri == null) {
            insertUserToFirestore(uid, user);
          } else {
            uploadAvatarToStorage(uid, user, mSelectedImageUri);
          }
        })
        .addOnFailureListener(requireActivity(), this::onError);
  }

  private void uploadAvatarToStorage(String uid, Map<String, Object> user, Uri uri) {
    final StorageReference reference = mFirebaseStorage.getReference("avatar_images/" + uid);
    reference.putFile(uri)
        .continueWithTask(task -> {
          if (!task.isSuccessful()) {
            throw requireNonNull(task.getException());
          }
          return reference.getDownloadUrl();
        })
        .addOnCompleteListener(requireActivity(), task -> {
          if (task.isSuccessful()) {
            user.put("avatar", requireNonNull(task.getResult()).toString());
            insertUserToFirestore(uid, user);
          } else {
            onError(task.getException());
          }
        });
  }

  private void onError(@Nullable Exception e) {
    mProgressDialog.dismiss();
    mButtonRegister.setEnabled(true);

    String message = e instanceof FirebaseAuthException
        ? getMessageFromFirebaseAuthExceptionErrorCode(((FirebaseAuthException) e).getErrorCode())
        : e != null ? e.getMessage() : getString(R.string.undefined_error);
    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
  }

  private void insertUserToFirestore(String uid, Map<String, Object> user) {
    mFirebaseFirestore.document(Constants.USERS_NAME_COLLECION + "/" + uid)
        .set(user)
        .addOnSuccessListener(requireActivity(), documentReference -> {
          mProgressDialog.dismiss();
          mButtonRegister.setEnabled(true);
          mListener.onRegisterSuccessfully();
        })
        .addOnFailureListener(requireActivity(), this::onError);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == CHOOSE_AVATAR_IMAGE_REQUEST_CODE
        && resultCode == RESULT_OK && data != null) {
      mSelectedImageUri = data.getData();
      if (mSelectedImageUri != null) {
        Picasso.get()
            .load(mSelectedImageUri)
            .fit()
            .centerCrop()
            .into(mImageAvatar);
      }
    }
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    unbinder.unbind();
    if (mProgressDialog != null && mProgressDialog.isShowing()) {
      mProgressDialog.dismiss();
    }
  }

  interface Listener {
    void onLoginClick();

    void onRegisterSuccessfully();
  }
}
