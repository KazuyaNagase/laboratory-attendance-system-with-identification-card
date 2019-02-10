package com.geeksonthegate.laboratoryattendancesystemwithidentificationcard

import android.annotation.SuppressLint
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.KeyEvent
import android.view.MotionEvent
import android.widget.TextView
import com.geeksonthegate.laboratoryattendancesystemwithidentificationcard.model.Student
import io.realm.Realm
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_scan_studentcard.*
import java.util.*
import java.lang.Runnable
import java.text.SimpleDateFormat


class RoomConfirmationActivity : AppCompatActivity() {
    private lateinit var realm: Realm
    private val handler = Handler()
    @SuppressLint("ResourceType")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room_confirmation)
        realm = Realm.getDefaultInstance()

        val id = intent.getStringExtra("scan_label")
        when (id) {
            getString(R.string.enter) -> {
                setTitle(R.string.enter)
                background.setImageResource(R.drawable.bg_enter_confirmation)
            }
            getString(R.string.exit) -> {
                setTitle(R.string.exit)
                background.setImageResource(R.drawable.bg_exit_confirmation)
            }
        }
        val idm: ByteArray = intent.getByteArrayExtra("idm")
        val student = realm.where<Student>().contains("idm", Arrays.toString(idm)).findFirst()

        val currentTime = Calendar.getInstance()
        var day = currentTime.get(Calendar.DAY_OF_WEEK)
        when (day) {
            1 -> {
                day += 5
            }
            2, 3, 4, 5, 6, 7 -> {
                day -= 2
            }
        }
        val startCoreTime: Calendar = Calendar.getInstance()
        startCoreTime.time = student?.lab?.coreTimeArray?.get(day)?.startCoreTime ?: startCoreTime.time
        val endCoreTime: Calendar = Calendar.getInstance()
        endCoreTime.time = student?.lab?.coreTimeArray?.get(day)?.endCoreTime
        val lab_view: TextView = findViewById(R.id.lab)
        val name_view: TextView = findViewById(R.id.name)
        val studentId_view: TextView = findViewById(R.id.studentId)
        val currentTime_view: TextView = findViewById(R.id.currentTime)
        val startcoreTime_view: TextView = findViewById(R.id.coretime_start)
        val endcoreTime_view: TextView = findViewById(R.id.coretime_end)
        lab_view.text = student?.lab?.labName.toString()
        name_view.text = student?.name.toString()
        studentId_view.text = student?.studentId.toString()
        //時間のフォーマット指定
        val sdf = SimpleDateFormat("HH:mm")
        startcoreTime_view.text = sdf.format(startCoreTime.getTime())
        endcoreTime_view.text = sdf.format(endCoreTime.getTime())
        currentTime_view.text = sdf.format(currentTime.getTime())
        //登録確認画面から3秒後にMainActivityに遷移
        handler.postDelayed(Runnable {
            moveToMainActivity()
        }, 3000)
    }

    //画面がタッチされるとMainActivityに遷移
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            moveToMainActivity()
        }
        return true
    }

    //端末のBackボタンを押すとMainActivityに遷移
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveToMainActivity()
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    private fun moveToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
    }
}
