package com.daon.fido.sdk.sample.basic.ui.chooser.account;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.daon.fido.sdk.sample.basic.R;

public class ChooseAccountDialogFragment extends DialogFragment {

    private final OnAccountSelectListener accountSelectListener;

    public interface OnAccountSelectListener {
        void onAccountSelected(int selectedAccount);
    }

    public ChooseAccountDialogFragment(OnAccountSelectListener accountSelectListener) {
        this.accountSelectListener = accountSelectListener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.choose_account, container, false);

        ListView listView = rootView.findViewById(R.id.list_view_user_accounts);
        Button cancel = rootView.findViewById(R.id.choose_user_account_cancel);

        cancel.setOnClickListener(v -> {
            // We need to inform Fido SDK that user did not select an account.
            accountSelectListener.onAccountSelected(-1);
            dismiss();
        });

        if (getDialog() != null) {
            getDialog().setCanceledOnTouchOutside(false);
        }

        setCancelable(false);

        if (getArguments() != null && !getArguments().isEmpty()) {
            String[] accounts = (String[]) getArguments().getSerializable("accounts");
            AccountListAdapter adapter = new AccountListAdapter(getContext(), accounts);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener((parent, view, position, id) -> {
                accountSelectListener.onAccountSelected(position);
                dismiss();
            });
        }
        return rootView;
    }
}