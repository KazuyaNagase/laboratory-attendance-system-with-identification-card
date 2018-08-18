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

        enter.setOnClickListener{ accessButtonTapped(it) }
        exit.setOnClickListener{ accessButtonTapped(it)}

    }

    // カード読み取り(入退室)画面に遷移
    private fun accessButtonTapped(view: View?) {
        val intent = Intent(this, AccessActivity::class.java)
        intent.putExtra("access_label", view?.id)
        startActivity(intent)
    }

}
