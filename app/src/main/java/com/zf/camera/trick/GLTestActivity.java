package com.zf.camera.trick;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.zf.camera.trick.base.BaseActivity;

public class GLTestActivity extends BaseActivity {

    public static void start(Activity activity) {
        activity.startActivity(new Intent(activity, GLTestActivity.class));
    }


    @Override
    protected boolean isDarkFont() {
        return false;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_gl_test);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "反转");
        menu.add(0, 1, 1, "测试");
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        while (0 == item.getItemId()) {
            Toast.makeText(this, "xxx", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
}
