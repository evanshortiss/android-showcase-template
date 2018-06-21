package com.aerogear.androidshowcase;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.aerogear.androidshowcase.domain.models.Note;
import com.aerogear.androidshowcase.features.authentication.AuthenticationDetailsFragment;
import com.aerogear.androidshowcase.features.authentication.AuthenticationFragment;
import com.aerogear.androidshowcase.features.authentication.providers.KeycloakAuthenticateProviderImpl;
import com.aerogear.androidshowcase.features.authentication.providers.OpenIDAuthenticationProvider;
import com.aerogear.androidshowcase.features.storage.NotesDetailFragment;
import com.aerogear.androidshowcase.features.storage.NotesListFragment;
import com.aerogear.androidshowcase.mvp.components.HttpHelper;
import com.aerogear.androidshowcase.navigation.Navigator;

import org.aerogear.mobile.auth.AuthService;
import org.aerogear.mobile.auth.user.UserPrincipal;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasFragmentInjector;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, AuthenticationFragment.AuthenticationListener, NotesListFragment.NoteListListener, NotesDetailFragment.SaveNoteListener, AuthenticationDetailsFragment.AuthenticationDetailsListener, HasFragmentInjector {


    @Inject
    DispatchingAndroidInjector<Fragment> fragmentInjector;

    @Inject
    OpenIDAuthenticationProvider authProvider;

    @Inject
    @Nullable
    AuthService authService;

    @Inject
    Navigator navigator;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;

    @BindView(R.id.nav_view)
    NavigationView navigationView;

    @Inject
    Context context;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     *
     * @param savedInstanceState - the saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        // initialise the httphelper
        HttpHelper.init();

        // load the main menu fragment
        navigator.navigateToHomeView(this);
    }

    /**
     * Handling to close the sidebar on back button press
     */
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (navigator.canGoBack(this)) {
            navigator.goBack(this);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Handling for Sidebar Navigation
     *
     * @param item - the menu item that was selected from the menu
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case R.id.nav_home:
                navigator.navigateToHomeView(this);
                break;
            case R.id.nav_identity_management:
                navigator.navigateToLandingIdentityManagement(this);
                break;
            case R.id.nav_identity_management_documentation:
                navigator.navigateToIdentityManagementDocumentation(this);
                break;
            case R.id.nav_identity_management_authentication:
                navigator.navigateToAuthenticationView(this);
            case R.id.nav_identity_management_sso:
                navigator.navigateToUnderConstructorView(this);
                break;
            case R.id.nav_security:
                navigator.navigateToLandingSecurity(this);
                break;
            case R.id.nav_security_documentation:
                navigator.navigateToSecurityDocumentation(this);
                break;
            case R.id.nav_security_device_trust:
                navigator.navigateToDeviceView(this);
                break;
            case R.id.nav_security_storage:
                navigator.navigateToStorageView(this);
                break;
            case R.id.nav_security_cert_pinning:
                navigator.navigateToNetworkView(this);
                break;
            case R.id.nav_push:
                navigator.navigateToLandingPush(this);
                break;
            case R.id.nav_push_documentation:
                navigator.navigateToPushDocumentation(this);
                break;
            case R.id.nav_push_messages:
                navigator.navigateToPushView(this);
                break;
            case R.id.nav_metrics:
                navigator.navigateToLandingMetrics(this);
                break;
            case R.id.nav_metrics_documentation:
                navigator.navigateToMetricsDocumentation(this);
                break;
            case R.id.nav_metrics_device_profile_info:
                navigator.navigateToUnderConstructorView(this);
                break;
            case R.id.nav_metrics_trust_check_info:
                navigator.navigateToUnderConstructorView(this);
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public AndroidInjector<Fragment> fragmentInjector() {
        return fragmentInjector;
    }

    // tag::onActivityResult[]
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == KeycloakAuthenticateProviderImpl.LOGIN_RESULT_CODE) {
            //Put a note here that the null check is not how we expect people to use the SDK and link the good usage to the auth screen
            if (authService != null)
                authService.handleAuthResult(data);
        }
    }
    // end::onActivityResult[]

    @Override
    public void onAuthSuccess(final UserPrincipal user) {
        navigator.navigateToAuthenticateDetailsView(this, user);
    }

    @Override
    public void onAuthError(final Exception error) {

    }

    @Override
    public void onLogoutSuccess(final UserPrincipal user) {
        navigator.navigateToAuthenticationView(this);
    }

    @Override
    public void onLogoutError(final Exception error) {

    }

    @Override
    public void onNoteClicked(Note note) {
        Log.i("SecureAndroidApp", "Note selected: " + note.getContent());
        navigator.navigateToSingleNoteView(this, note);
    }

    @Override
    public void onCreateNote() {
        navigator.navigateToSingleNoteView(this, null);
    }


    @Override
    public void onNoteSaved(Note note) {
        navigator.navigateToStorageView(this);
    }
}


