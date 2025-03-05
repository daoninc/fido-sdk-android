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

package com.daon.fido.sdk.sample.basic;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.daon.fido.sdk.sample.basic.databinding.ActivityMainBinding;
import com.daon.fido.sdk.sample.basic.ui.intro.IntroActivity;

public class MainActivity extends EdgeToEdgeActivity {
    private ActivityMainBinding viewBinding;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        startIntroActivity();
    }

    private void startIntroActivity() {
        Intent newIntent = new Intent(MainActivity.this, IntroActivity.class);
        startActivity(newIntent);
        finish();
    }
}
