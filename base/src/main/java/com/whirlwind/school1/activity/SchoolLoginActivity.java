package com.whirlwind.school1.activity;

import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.URLSpan;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.whirlwind.school1.R;
import com.whirlwind.school1.base.BaseActivity;
import com.whirlwind.school1.helper.BackendHelper;
import com.whirlwind.school1.models.Group;
import com.whirlwind.school1.models.PendingSchoolLogin;

import java.util.ArrayList;
import java.util.List;

public class SchoolLoginActivity extends BaseActivity implements View.OnClickListener {

    private AutoCompleteTextView nameAutoCompleteTextView;
    private TextInputEditText passwordEditText;
    private TextInputLayout nameLayout, passwordLayout;
    private TextView signupTextView;

    private List<Group> schools = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_school_login);

        // TODO: Failure messages

        nameAutoCompleteTextView = findViewById(R.id.activity_school_login_autocomplete_text_view_name);
        passwordEditText = findViewById(R.id.activity_school_login_edit_text_password);
        nameLayout = findViewById(R.id.activity_school_login_text_input_layout_name);
        passwordLayout = findViewById(R.id.activity_school_login_text_input_layout_password);
        signupTextView = findViewById(R.id.activity_school_login_signup);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_account_school);
        }


        FirebaseDatabase.getInstance().getReference()
                .child("schools")
                .addValueEventListener(new BackendHelper.ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        schools.clear();
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            Group school = child.getValue(Group.class);
                            if (school != null) {
                                school.setKey(child.getKey());
                                schools.add(school);
                            }
                        }

                        nameAutoCompleteTextView.setAdapter(new ArrayAdapter<>(SchoolLoginActivity.this, android.R.layout.simple_list_item_1, schools));
                    }
                });

        FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("school")
                .addValueEventListener(new BackendHelper.ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue(String.class) != null) {
                            setResult(1);
                            finish();
                        }
                    }
                });

        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    onClick(v);
                    return true;
                }
                return false;
            }
        });

        findViewById(R.id.activity_school_login_button_login).setOnClickListener(this);

        String signupText = "Sign up your School!";
        SpannableString spannableString = new SpannableString(signupText);
        spannableString.setSpan(new URLSpan(""), 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        signupTextView.setText(spannableString, TextView.BufferType.SPANNABLE);
        signupTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        nameLayout.setErrorEnabled(false);
        passwordLayout.setErrorEnabled(false);
        final String name = nameAutoCompleteTextView.getText().toString(),
                password = passwordEditText.getText().toString();
        if ("".equals(name))
            nameLayout.setError(getString(R.string.error_field_required));
        else if ("".equals(password))
            passwordLayout.setError(getString(R.string.error_field_required));
        else {
            String uid = null;
            for (Group school : schools)
                if (school.name.equals(name)) {
                    uid = school.getKey();
                    break;
                }

            FirebaseDatabase.getInstance().getReference()
                    .child("users")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child("pendingSchoolLogin")
                    .setValue(new PendingSchoolLogin(uid, name, password));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            onBackPressed();
        else
            return super.onOptionsItemSelected(item);

        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(0);
    }
}