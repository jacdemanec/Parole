package com.jacdemanec.parole;


import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.jacdemanec.parole.adapters.HashtagAdapter;
import com.jacdemanec.parole.adapters.MainPageAdapter;
import com.jacdemanec.parole.model.Hashtag;
import com.jacdemanec.parole.viewmodel.HashtagViewModel;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.google.GoogleEmojiProvider;

import java.util.Arrays;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity implements AddHashtagDialogFragment.AddHasthagListener{

    private static final String TAG = "MainActivity";

    public static final String ANONYMOUS = "anonymous";
    private static final int RC_SIGN_IN = 1;

    private HashtagViewModel mViewModel;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EmojiManager.install(new GoogleEmojiProvider());

        mViewModel = ViewModelProviders.of(this).get(HashtagViewModel.class);

        ViewPager viewPager = findViewById(R.id.pager);
        FragmentPagerAdapter fragmentPagerAdapter = new MainPageAdapter(getSupportFragmentManager(), this);
        TabLayout tabLayout = findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);

        viewPager.setAdapter(fragmentPagerAdapter);
        mFirebaseAuth = FirebaseAuth.getInstance();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    onSingedInInitialize(user.getDisplayName());
                } else {
                    onSingedOutCleanup();
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setAvailableProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.EmailBuilder().build(),
                                            new AuthUI.IdpConfig.GoogleBuilder().build()))
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RC_SIGN_IN:
                if (resultCode == RESULT_OK) {
                    Toast.makeText(this, "Signed in!", Toast.LENGTH_SHORT).show();
                } else if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, "Sign in cancelled", Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        /*
        //I order to enable this feature, implement OnQueryTextListener and override its methods
        SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView search = (SearchView) menu.findItem(R.id.search).getActionView();
        search.setSearchableInfo(manager.getSearchableInfo(getComponentName()));
        search.setOnQueryTextListener(this);
        */
        return true;
    }

    private void onSingedInInitialize(String username) {
        mViewModel.setmUsername(username);
    }

    private void onSingedOutCleanup() {
        mViewModel.setmUsername(ANONYMOUS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                AuthUI.getInstance().signOut(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    @Override
    public void onDialogPositivieClick(String hashtag, String text) {
        HashMap<String, Boolean> emptyLikesMap = new HashMap<>();
        HashMap<String, Boolean> emptyFavoritesMap = new HashMap<>();
        Hashtag hashtagInstance = new Hashtag(hashtag, text, "@" + mViewModel.getmUsername(), emptyLikesMap, 0, emptyFavoritesMap, 0, 0);
        mViewModel.getmHashtagDbReference().child(hashtag).setValue(hashtagInstance);

    }
}
