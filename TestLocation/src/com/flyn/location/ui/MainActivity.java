package com.flyn.location.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.flyn.location.R;

public class MainActivity extends Activity
{
        @Override
        protected void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
        }
        
        public void onClick1(View view)
        {
            Intent intent=AbstractLocationMapActivity.createIntent(this, null,true);
            startActivity(intent);
        }
}
