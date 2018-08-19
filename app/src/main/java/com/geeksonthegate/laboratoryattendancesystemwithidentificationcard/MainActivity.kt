package com.geeksonthegate.laboratoryattendancesystemwithidentificationcard

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        enter.setOnClickListener{ scanStudentcartButtonTapped(it) }
        exit.setOnClickListener{ scanStudentcartButtonTapped(it)}

    }

    // カード読み取り(入退室)画面に遷移
    private fun scanStudentcartButtonTapped(view: View?) {
        val intent = Intent(this, ScanStudentcardActivity::class.java)
        intent.putExtra("scan_label", view?.id)
        startActivity(intent)
    }

}
