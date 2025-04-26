package com.zf.camera.trick;

import static com.zf.camera.trick.filter.sample.IShapeKt.SHAPE_TYPE_NONE;
import static com.zf.camera.trick.filter.sample.IShapeKt.SHAPE_TYPE_TRIANGLE_VAO;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.zf.camera.trick.base.BaseActivity;
import com.zf.camera.trick.base.IAction;
import com.zf.camera.trick.base.IGroup;
import com.zf.camera.trick.databinding.ActivityOpenGlTestBinding;
import com.zf.camera.trick.filter.sample.IShapeKt;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class GLTestActivity extends BaseActivity {

    public static void start(Activity activity) {
        activity.startActivity(new Intent(activity, GLTestActivity.class));
    }

    private ActivityOpenGlTestBinding binding;

    protected ActivityOpenGlTestBinding getViewBinding() {
        return ActivityOpenGlTestBinding.inflate(getLayoutInflater());
    }

    private final Map<Integer, IAction> actions = new HashMap<>();
    private final Map<Integer, IGroup> groups = new HashMap<>();
    private int mCurrentType = SHAPE_TYPE_NONE;
    //默认选中的选项
    private static final int DEFAULT_TYPE = SHAPE_TYPE_TRIANGLE_VAO;

    @Override
    protected boolean isDarkFont() {
        return false;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = getViewBinding();
        setContentView(binding.getRoot());

        init();
        //默认显示第一项
        updateMenu(DEFAULT_TYPE);
    }

    private void init() {
        IShapeKt.getShapeAction(this, actions, groups);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (null != menu.findItem(mCurrentType)) {
            menu.findItem(mCurrentType).setChecked(true);
        }
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        IShapeKt.onCreateOptionsMenu(menu, actions, groups);
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (null != item.getSubMenu()) {//父菜单，不处理
            return super.onOptionsItemSelected(item);
        }


        if (actions.containsKey(item.getItemId())) {
            updateMenu(item.getItemId());
            return true;
        } else {
            Toast.makeText(this, "暂不支持该选项", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateMenu(int menuId) {
        mCurrentType = menuId;
        Objects.requireNonNull(getActionBar()).setTitle(Objects.requireNonNull(actions.get(menuId)).getName());
        binding.glSurfaceView.updateShape(Objects.requireNonNull(actions.get(menuId)).getAction());
    }
}
