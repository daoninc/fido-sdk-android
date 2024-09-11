package com.daon.fido.sdk.sample.basic.ui.chooser.authenticator;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.daon.fido.client.sdk.model.Authenticator;
import com.daon.fido.sdk.sample.basic.R;

public class ChooseAuthenticatorDialogFragment extends DialogFragment {

    private static final String TAG = ChooseAuthenticatorDialogFragment.class.getSimpleName();
    private final OnAuthSelectListener authSelectListener;
    private final Authenticator[][] authenticators;

    public interface OnAuthSelectListener {
        void onAuthSelected(int selectedAuth);
    }

    public ChooseAuthenticatorDialogFragment(OnAuthSelectListener authSelectListener, Authenticator[][] authenticators) {
        this.authSelectListener = authSelectListener;
        this.authenticators = authenticators;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        View rootView = inflater.inflate(R.layout.choose_authenticator, container, false);
        ListView listView = rootView.findViewById(R.id.list_view_authenticatorsets);

        Button cancel = rootView.findViewById(R.id.choose_authenticator_cancel);
        cancel.setOnClickListener(v -> {
            authSelectListener.onAuthSelected(-1);
            dismiss();
        });

        if (getDialog() != null) {
            getDialog().setCanceledOnTouchOutside(false);
        }

        AuthenticatorListAdapter adapter = new AuthenticatorListAdapter(getContext(), authenticators);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            authSelectListener.onAuthSelected(position);
            dismiss();
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getDialog() != null) {
            getDialog().setOnKeyListener((dialog, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    authSelectListener.onAuthSelected(-1);
                    dismiss();
                    return true;
                }
                return false;
            });
        }
    }
}
