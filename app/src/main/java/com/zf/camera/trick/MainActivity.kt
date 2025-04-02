package com.zf.camera.trick

import android.os.Bundle
import androidx.appcompat.widget.AppCompatTextView
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.zf.camera.trick.base.BaseActivity
import com.zf.camera.trick.ui.theme.MainActivityTheme

class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<AppCompatTextView>(R.id.camera).setOnClickListener {
            CameraActivity.startActivity(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MainActivityTheme {
        Greeting("Android")
    }
}