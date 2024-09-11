package com.daon.fido.sdk.sample.basic.ui.chooser.authenticator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.daon.fido.client.sdk.core.FidoConstants;
import com.daon.fido.client.sdk.model.Authenticator;
import com.daon.fido.sdk.sample.basic.R;

public class AuthenticatorListAdapter extends ArrayAdapter<Authenticator[]> {

    private final Context context;

    public AuthenticatorListAdapter(Context context, Authenticator[][] dataSource) {
        super(context, 0, dataSource);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        Authenticator[] authenticatorSet = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.authenticator_list_item, parent, false);
        }

        ImageView image = convertView.findViewById(R.id.auth_icon);
        TextView name = convertView.findViewById(R.id.auth_name);

        StringBuilder builder = new StringBuilder();
        int authCount = 0;
        int iconIndex = 0;

        if (authenticatorSet != null) {
            for (int index = 0; index < authenticatorSet.length; index++) {
                Authenticator element = authenticatorSet[index];
                if (element.getUserVerification() == FidoConstants.USER_VERIFY_NONE) {
                    if (authenticatorSet.length == 1) {
                        builder.append(element.getTitle());
                        authCount++;
                    } else if (index == 0) {
                        iconIndex = 1;
                    }
                } else {
                    if (authCount > 0) {
                        builder.append(" &\n");
                    }
                    builder.append(element.getTitle());
                    authCount++;
                }
            }

            name.setText(builder.toString());

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inMutable = true;
            String icon = authenticatorSet[iconIndex].getIcon();
            int commandIndex = icon.indexOf(',');
            String imageBase64 = icon.substring(commandIndex + 1);
            byte[] imageBytes = Base64.decode(imageBase64, Base64.DEFAULT);
            Bitmap bmp = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, options);
            image.setImageBitmap(bmp);
        }

        return convertView;
    }
}

