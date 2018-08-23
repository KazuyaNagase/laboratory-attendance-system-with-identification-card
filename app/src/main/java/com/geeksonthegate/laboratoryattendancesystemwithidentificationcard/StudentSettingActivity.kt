package com.geeksonthegate.laboratoryattendancesystemwithidentificationcard

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle

import android.text.format.DateFormat
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import com.geeksonthegate.laboratoryattendancesystemwithidentificationcard.model.CoreTime
import com.geeksonthegate.laboratoryattendancesystemwithidentificationcard.model.Lab
import com.geeksonthegate.laboratoryattendancesystemwithidentificationcard.model.Student
import io.realm.Realm
import io.realm.RealmList
import kotlinx.android.synthetic.main.activity_student_setting.*
import java.util.*

class StudentSettingActivity : AppCompatActivity() {

    private lateinit var realm: Realm
    private lateinit var startCoreTimeLabelList: List<TextView>
    private lateinit var endCoreTimeLabelList: List<TextView>
    private lateinit var isCoreDayBoxList: List<CheckBox>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_setting)

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

        realm = Realm.getDefaultInstance()
        val scanLabel = intent.getStringExtra("scan_label")
        val idm = intent.getByteArrayExtra("idm")
        val intent = Intent(this, MainActivity::class.java)

        // 前画面から受け取ったIDmで検索する
        var student: Student? = realm.where(Student::class.java).equalTo("idm", Arrays.toString(idm)).findFirst()

        // コアタイム一覧を生成するメソッドに渡す一覧を生成・初期化
        val coreTimeList = mutableListOf<CoreTime>()
        for (i in 0..6) {
            coreTimeList.add(CoreTime(
                    GregorianCalendar(
                            2000, 0, 1, 10, 0).time,
                    GregorianCalendar(
                            2000, 0, 1, 17, 0).time,
                    true))
        }

        // 前画面から受け取ったラベルを基に処理分岐
        when (scanLabel) {
            getString(R.string.register) -> {
                when (student) {
                    null -> {
                        setCoreTimeArea(coreTimeList)
                    }
                    else -> {
                        Toast.makeText(this,
                                "ここで登録済みモーダルを表示", Toast.LENGTH_SHORT).show()
                        startActivity(intent)
                    }
                }
            }
            getString(R.string.edit) -> {
                when (student) {
                    null -> {
                        Toast.makeText(this,
                                "ここで未登録モーダルを表示", Toast.LENGTH_SHORT).show()
                        startActivity(intent)
                    }
                    else -> {
                        setCoreTimeArea(student.lab?.coretimeArray ?: coreTimeList)
                        name_entry.setText(student.name)
                        studentid_entry.setText(student.studentId)
                        // TODO: spinnerはまだ全く触ってない
                    }
                }
            }
        }
        user_register_button.setOnClickListener {
            val realmCoretimeList = RealmList<CoreTime>()
            for (item in coreTimeList) {
                realmCoretimeList.add(item)
            }
            student = Student(Arrays.toString(idm), studentid_entry.text.toString(), name_entry.text.toString(), Lab(labName = "福田研究室", coretimeArray = realmCoretimeList))
            realm.executeTransaction { it.insertOrUpdate(student) }
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    // コアタイム一覧を受け取りレイアウト内に一覧を表示するメソッド
    private fun setCoreTimeArea(coreTimeList: List<CoreTime>) {
        for (i in 0..6) {
            startCoreTimeLabelList[i].text = DateFormat.format("kk:mm", coreTimeList[i].startCoreTime)
            endCoreTimeLabelList[i].text = DateFormat.format("kk:mm", coreTimeList[i].endCoreTime)
            isCoreDayBoxList[i].isChecked = coreTimeList[i].isCoreDay ?: true
        }
    }
}
