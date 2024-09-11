// Copyright (C) 2022 Daon.
//
// Permission to use, copy, modify, and/or distribute this software for any purpose with or without
// fee is hereby granted.
//
// THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS
// SOFTWARE INCLUDING ALL IMPLIED
// WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
// SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL
// DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
// ACTION OF CONTRACT, NEGLIGENCE OR OTHER
// TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.

package com.daon.fido.sdk.sample.basic.ui.authenticators;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;

import com.daon.fido.sdk.sample.basic.R;
import com.daon.fido.sdk.sample.basic.databinding.ItemAuthenticatorInfoBinding;
import com.daon.fido.sdk.sample.basic.model.AuthenticatorInfo;

import java.text.DateFormat;

/**
 * A layout item used in the creation of the list of authenticators
 */
@SuppressLint("ViewHolder")
public class AuthenticatorsAdapter extends ArrayAdapter<AuthenticatorInfo> {
    public AuthenticatorsAdapter(Context context, AuthenticatorInfo[] authenticatorInfos) {
        super(context, 0, authenticatorInfos);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        // Get the data item for this position
        AuthenticatorInfo authenticatorInfo = getItem(position);
        ItemAuthenticatorInfoBinding binding = ItemAuthenticatorInfoBinding.inflate(LayoutInflater.from(getContext()), parent, false);

        // Populate the data into the template view using the data object
        if (authenticatorInfo != null) {
            binding.getRoot().setTag(authenticatorInfo);
            binding.authName.setText(authenticatorInfo.getName());
            binding.authCreated.setText(DateFormat.getDateTimeInstance().format(authenticatorInfo.getCreated()));
            if (authenticatorInfo.getLastUsed() == null) {
                binding.authLastUsed.setText(R.string.never_used);
            } else {
                binding.authLastUsed.setText(DateFormat.getDateTimeInstance().format(authenticatorInfo.getLastUsed()));
            }
            binding.authStatus.setText(authenticatorInfo.getStatus());

            // Create the icon to be displayed
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inMutable = true;
            if (authenticatorInfo.getIcon() != null) {
                byte[] imgBytes = Base64.decode(authenticatorInfo.getIcon(), Base64.DEFAULT);
                Bitmap bmp = BitmapFactory.decodeByteArray(imgBytes, 0, imgBytes.length, options);
                binding.authIcon.setImageBitmap(bmp);
            }
        }


        // Return the completed view to render on screen
        return binding.getRoot();
    }
}
