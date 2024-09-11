package com.daon.fido.sdk.sample.basic.util;

import android.util.Log;

import java.util.Random;

public class Util {

    /** @noinspection SpellCheckingInspection*/
    public static String generateEmail() {
        String saltChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 10) {
            int index = (int) (rnd.nextFloat() * saltChars.length());
            salt.append(saltChars.charAt(index));
        }
        String saltStr = salt + "@daon.com";
        Log.d("DAON", "Generated email :" + saltStr);
        return saltStr;
    }
}
