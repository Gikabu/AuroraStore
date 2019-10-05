

package com.aurora.store.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ViewSwitcher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;

import com.aurora.store.AnonymousLoginService;
import com.aurora.store.AnonymousRefreshService;
import com.aurora.store.ErrorType;
import com.aurora.store.R;
import com.aurora.store.activity.AccountsActivity;
import com.aurora.store.api.PlayStoreApiAuthenticator;
import com.aurora.store.api.SearchIterator2;
import com.aurora.store.events.Event;
import com.aurora.store.events.Events;
import com.aurora.store.events.RxBus;
import com.aurora.store.exception.CredentialsEmptyException;
import com.aurora.store.exception.InvalidApiException;
import com.aurora.store.exception.MalformedRequestException;
import com.aurora.store.exception.TooManyRequestsException;
import com.aurora.store.iterator.CustomAppListIterator;
import com.aurora.store.utility.Accountant;
import com.aurora.store.utility.ContextUtil;
import com.aurora.store.utility.Log;
import com.aurora.store.view.ErrorView;
import com.dragons.aurora.playstoreapiv2.AuthException;
import com.dragons.aurora.playstoreapiv2.IteratorGooglePlayException;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;

import butterknife.BindView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static com.aurora.store.utility.Util.noNetwork;

public abstract class BaseFragment extends Fragment {

    protected CustomAppListIterator iterator;
    protected CompositeDisposable disposable = new CompositeDisposable();

    @BindView(R.id.coordinator)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.view_switcher)
    ViewSwitcher viewSwitcher;
    @BindView(R.id.content_view)
    ViewGroup layoutContent;
    @BindView(R.id.err_view)
    ViewGroup layoutError;

    private SearchIterator2 searchIterator;
    private CompositeDisposable disposableBus = new CompositeDisposable();
    private Context context;

    CustomAppListIterator getIterator(String query) {
        try {
            searchIterator = new SearchIterator2(PlayStoreApiAuthenticator.getApi(context), query);
            iterator = new CustomAppListIterator(searchIterator);
            return iterator;
        } catch (Exception e) {
            processException(e);
            return null;
        }
    }

    protected abstract View.OnClickListener errRetry();

    protected abstract void fetchData();

    /*UI handling methods*/

    protected void notifyLoggedIn() {
        ContextUtil.runOnUiThread(() -> {
            fetchData();
            notifyStatus(coordinatorLayout, context.getResources().getString(R.string.action_logging_in_success));
        });
    }

    protected void notifyNetworkFailure() {
        setErrorView(ErrorType.NO_NETWORK);
        notifyStatus(coordinatorLayout, context.getString(R.string.error_no_network));
        switchViews(true);
    }

    protected void notifyPermanentFailure() {
        setErrorView(ErrorType.UNKNOWN);
        switchViews(true);
    }

    protected void notifyLoggedOut() {
        setErrorView(ErrorType.LOGOUT_ERR);
        switchViews(true);
        notifyStatus(coordinatorLayout, context.getString(R.string.error_logged_out));
    }

    protected void notifyTokenExpired() {
        notifyStatus(coordinatorLayout, context.getString(R.string.action_token_expired));
    }

    @Override
    public void onStart() {
        super.onStart();
        disposableBus.add(RxBus.get().toObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(event -> {
                    if (event instanceof Event) {
                        Events eventEnum = ((Event) event).getEvent();
                        switch (eventEnum) {
                            case LOGGED_IN:
                                notifyLoggedIn();
                                break;
                            case LOGGED_OUT:
                                notifyLoggedOut();
                                break;
                            case TOKEN_REFRESHED:
                                notifyLoggedIn();
                                break;
                            case TOKEN_EXPIRED:
                                notifyTokenExpired();
                                break;
                            case NET_DISCONNECTED:
                                notifyNetworkFailure();
                                break;
                            case PERMANENT_FAIL:
                                notifyPermanentFailure();
                                break;
                        }
                    }
                }));
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        searchIterator = null;
        iterator = null;
        disposable.clear();
    }

    @Override
    public void onStop() {
        super.onStop();
        disposableBus.clear();
    }

    private void notifyStatus(@NonNull CoordinatorLayout coordinatorLayout, String message) {
        Snackbar snackbar = Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    /*ErrorView UI handling methods*/

    protected void setErrorView(ErrorType errorType) {
        layoutError.removeAllViews();
        layoutError.addView(new ErrorView(context, errorType, getAction(errorType)));
    }

    protected void switchViews(boolean showError) {
        if (viewSwitcher.getCurrentView() == layoutContent && showError)
            viewSwitcher.showNext();
        else if (viewSwitcher.getCurrentView() == layoutError && !showError)
            viewSwitcher.showPrevious();
    }

    private View.OnClickListener errLogin() {
        return v -> {
            ((Button) v).setText(getString(R.string.action_logging_in));
            ((Button) v).setEnabled(false);
            if (Accountant.isLoggedIn(context)) {
                RxBus.publish(new Event(Events.LOGGED_IN));
                return;
            }
            if (Accountant.isGoogle(context))
                context.startActivity(new Intent(context, AccountsActivity.class));
            else
                logInWithDummy();
        };
    }

    protected View.OnClickListener errClose() {
        return v -> {

        };
    }

    private View.OnClickListener getAction(ErrorType errorType) {
        switch (errorType) {
            case LOGOUT_ERR:
                return errLogin();
            case APP_NOT_FOUND:
                return errClose();
            default:
                return errRetry();
        }
    }

    /*Exception handling methods*/

    protected void processException(Throwable e) {
        disposable.clear();
        if (e instanceof AuthException) {
            processAuthException((AuthException) e);
        } else if (e instanceof IteratorGooglePlayException) {
            processException(e.getCause());
        } else if (e instanceof TooManyRequestsException) {
            processAuthException(new AuthException("Too many request", 429));
        } else if (e instanceof MalformedRequestException) {
            processAuthException(new AuthException("Malformed Request", 401));
        } else if (e instanceof IOException) {
            processIOException((IOException) e);
        } else {
            Log.e("Unknown exception " + e.getClass().getName() + " " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void processIOException(IOException e) {
        String message;
        if (context != null) {
            if (noNetwork(e)) {
                message = context.getString(R.string.error_no_network);
                Log.i(message);
                RxBus.publish(new Event(Events.NET_DISCONNECTED));
            } else {
                message = TextUtils.isEmpty(e.getMessage())
                        ? context.getString(R.string.error_network_other)
                        : e.getMessage();
                Log.i(message);
            }
        } else Log.i("No Network Connection");
    }

    private void processAuthException(AuthException e) {
        if (e instanceof CredentialsEmptyException || e instanceof InvalidApiException) {
            Log.i("Logged out");
            Accountant.completeCheckout(context);
            RxBus.publish(new Event(Events.LOGGED_OUT));
        } else if (e.getCode() == 401 && Accountant.isDummy(context)) {
            Log.i("Token is stale");
            refreshToken();
        } else if (e.getCode() == 429 && Accountant.isDummy(context)) {
            Log.i("Too many requests from current session, requesting new login session");
            Accountant.completeCheckout(context);
            logInWithDummy();
        } else {
            ContextUtil.toast(context, R.string.error_incorrect_password);
            PlayStoreApiAuthenticator.logout(context);
            Accountant.completeCheckout(context);
        }
    }

    /*
     * Anonymous accounts handling methods
     *
     */

    private void logInWithDummy() {
        Intent intent = new Intent(context, AnonymousLoginService.class);
        if (!AnonymousLoginService.isServiceRunning())
            context.startService(intent);
    }

    private void refreshToken() {
        Intent intent = new Intent(context, AnonymousRefreshService.class);
        if (!AnonymousRefreshService.isServiceRunning())
            context.startService(intent);
    }
}
