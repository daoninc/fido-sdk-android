package com.daon.fido.sdk.sample.basic.ui.chooser.account;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.daon.fido.sdk.sample.basic.R;

public class AccountListAdapter extends ArrayAdapter<String> {

    public AccountListAdapter(Context context, String[] dataSource) {
        super(context, 0, dataSource);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        // Inflate the view if it doesn't already exist
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.account_list_item, parent, false);
        }
        String account = getItem(position);
        TextView user = convertView.findViewById(R.id.user_account_id);
        user.setText(account);
        return convertView;
    }
}
