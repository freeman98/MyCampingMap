package com.freeman.mycampingmap.utils

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.freeman.mycampingmap.MyApplication
import com.freeman.mycampingmap.data.CampingDataUtil

class IntentUtil {

    val TAG: String = this::class.java.simpleName

    companion object {
        fun openWebPage(uri: Uri) {
            // 웹 페이지를 열기 위한 Intent 생성
            MyLog.d("openWebPage() $uri")
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = uri
                flags = Intent.FLAG_ACTIVITY_NEW_TASK // 플래그 추가
            }
            try {
                MyApplication.context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(MyApplication.context, "No app found to open the website", Toast.LENGTH_SHORT).show()
            }
        }

        fun callPhone(phoneNumber: String) {
            // 전화를 걸기 위한 Intent 생성
            MyLog.d("callPhone() $phoneNumber")
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phoneNumber")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK // 플래그 추가
            }
            if (intent.resolveActivity(MyApplication.context.packageManager) != null) {
                MyApplication.context.startActivity(intent)
            } else {
                Toast.makeText(MyApplication.context, "No app found to place the call", Toast.LENGTH_SHORT).show()
            }
        }
    }
}