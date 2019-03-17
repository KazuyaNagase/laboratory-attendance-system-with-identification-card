package com.geeksonthegate.laboratoryattendancesystemwithidentificationcard

import android.app.TimePickerDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import com.geeksonthegate.laboratoryattendancesystemwithidentificationcard.dialog.TimePick
import com.geeksonthegate.laboratoryattendancesystemwithidentificationcard.model.CoreTime
import com.geeksonthegate.laboratoryattendancesystemwithidentificationcard.model.Lab
import io.realm.Realm
import io.realm.RealmList
import kotlinx.android.synthetic.main.activity_lab_setting.*
import java.lang.Exception
import java.util.*

class LabSettingActivity : AppCompatActivity(), TimePickerDialog.OnTimeSetListener {
    private lateinit var realm: Realm
    private lateinit var startCoreTimeLabelList: List<TextView>
    private lateinit var endCoreTimeLabelList: List<TextView>
    private lateinit var isCoreDayBoxList: List<CheckBox>
    private lateinit var tappedView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lab_setting)

        // 前画面のタイトルを適用
        val scanLabel = intent.getStringExtra("scan_label")
        title = scanLabel
        val idm = intent.getByteArrayExtra("idm")

        // 画面下部のコアタイム一覧の各パーツを取得
        startCoreTimeLabelList = listOf<TextView>(monday_coretime_start,
                tuesday_coretime_start, wednesday_coretime_start,
                thursday_coretime_start, friday_coretime_start,
                saturday_coretime_start, sunday_coretime_start)
        endCoreTimeLabelList = listOf<TextView>(monday_coretime_end,
                tuesday_coretime_end, wednesday_coretime_end,
                thursday_coretime_end, friday_coretime_end,
                saturday_coretime_end, sunday_coretime_end)
        isCoreDayBoxList = listOf<CheckBox>(monday_check_box,
                tuesday_check_box, wednesday_check_box,
                thursday_check_box, friday_check_box,
                saturday_check_box, sunday_check_box)

        // コアタイムリストに表示するデータを初期化
        var coreTimeList = RealmList<CoreTime>()
        for (i in 0..6) {
            coreTimeList.add(CoreTime(
                    GregorianCalendar(
                            2000, 0, 1, 10, 0).time,
                    GregorianCalendar(
                            2000, 0, 1, 17, 0).time,
                    true))
        }

        // 前画面から研究室IDを受け取り、IDを基に研究室DBから情報を取得 取得できない場合は新規研究室を作成
        val labId: String = intent.getStringExtra("lab_id")
        realm = Realm.getDefaultInstance()
        val lab = realm.where(Lab::class.java).equalTo("labId", labId).findFirst()
                ?: Lab(labName = "新規", coreTimeArray = coreTimeList)
        coreTimeList = lab.coreTimeArray ?: coreTimeList

        // 取得もしくは生成した研究室情報から画面描画・リスナにクリック・表示内容変更イベントを登録
        // TODO: 時刻設定のValidationが編集中にも適用されてしまう 編集が終わってから検証するようにする
        lab_name.setText(lab.labName)
        coretime_start.text = DateFormat.format("kk:mm", coreTimeList[0]?.startCoreTime)
        coretime_start.setOnClickListener {
            tappedView = it
            val timePick = TimePick()
            timePick.show(supportFragmentManager, "startTimePicker")
        }
        coretime_start.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                for (item in startCoreTimeLabelList)
                    item.text = s.toString()
            }
        })
        coretime_end.text = DateFormat.format("kk:mm", coreTimeList[0]?.endCoreTime)
        coretime_end.setOnClickListener {
            tappedView = it
            val timePick = TimePick()
            timePick.show(supportFragmentManager, "endTimePicker")
        }
        coretime_end.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                for (item in endCoreTimeLabelList)
                    item.text = s.toString()
            }
        })

        // コアタイムリストを表示する
        setCoreTimeArea(coreTimeList)

        // 登録ボタンのクリックイベントをリスナに登録
        // 研究室名が「新規」もしくは空の場合は登録を拒否
        lab_register_button.setOnClickListener {
            try {
                if (lab_name.text.toString() != "新規" && lab_name.text.toString() != "") {
                    val nextIntent = Intent(this, StudentSettingActivity::class.java)
                    realm.executeTransaction {
                        lab.labName = lab_name.text.toString()
                        for (i in 0..6) {
                            var hourAndMinute = startCoreTimeLabelList[i].text.split(":")
                            val cal = Calendar.getInstance()
                            cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hourAndMinute[0]))
                            cal.set(Calendar.MINUTE, Integer.parseInt(hourAndMinute[1]))
                            lab.coreTimeArray!![i]!!.startCoreTime = cal.time
                            hourAndMinute = endCoreTimeLabelList[i].text.split(":")
                            cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hourAndMinute[0]))
                            cal.set(Calendar.MINUTE, Integer.parseInt(hourAndMinute[1]))
                            lab.coreTimeArray!![i]!!.endCoreTime = cal.time
                            lab.coreTimeArray!![i]!!.isCoreDay = isCoreDayBoxList[i].isChecked
                        }
                        it.insertOrUpdate(lab)
                    }
                    nextIntent.putExtra("scan_label", scanLabel)
                    nextIntent.putExtra("idm", idm)
                    nextIntent.putExtra("lab_id", lab.labId)
                    startActivity(nextIntent)
                } else {
                    Toast.makeText(this, "研究室名を入力してください", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        val tappedTextView = tappedView as TextView
        tappedTextView.text = "${String.format("%02d", hourOfDay)}:${String.format("%02d", minute)}"
    }

    // コアタイムリストを基に画面描画し、変更内容をコアタイムリストに適用するメソッド
    private fun setCoreTimeArea(coreTimeList: RealmList<CoreTime>) {
        for (i in 0..6) {
            startCoreTimeLabelList[i].text = DateFormat.format("kk:mm", coreTimeList[i]?.startCoreTime)
            startCoreTimeLabelList[i].setOnClickListener {
                tappedView = it
                val timePick = TimePick()
                timePick.show(supportFragmentManager, "startTimePicker")
            }
            endCoreTimeLabelList[i].text = DateFormat.format("kk:mm", coreTimeList[i]?.endCoreTime)
            endCoreTimeLabelList[i].setOnClickListener {
                tappedView = it
                val timePick = TimePick()
                timePick.show(supportFragmentManager, "endTimePicker")
            }
            isCoreDayBoxList[i].isChecked = coreTimeList[i]?.isCoreDay ?: true
        }
    }
}
