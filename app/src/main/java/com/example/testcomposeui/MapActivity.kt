package com.example.testcomposeui

import android.content.ContentValues.TAG
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.testcomposeui.data.CampData
import com.example.testcomposeui.data.Const
import com.example.testcomposeui.ui.theme.TestComposeUITheme

class MapActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val cmapData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra<CampData>(Const.EXTRA_CAMP_DATA, CampData::class.java)
        } else {
            intent.getParcelableExtra<CampData>(Const.EXTRA_CAMP_DATA)

        }
        cmapData?.let {
            Log.d(TAG, it.toString())
        }


        setContent {
            TestComposeUITheme {
            }
        }
    }
}
