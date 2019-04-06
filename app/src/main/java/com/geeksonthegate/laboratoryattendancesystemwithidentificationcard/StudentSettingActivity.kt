package com.geeksonthegate.laboratoryattendancesystemwithidentificationcard

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.CheckBox
import android.widget.TextView
import android.view.View
import android.widget.*
import com.geeksonthegate.laboratoryattendancesystemwithidentificationcard.adapter.LabAdapter
import com.geeksonthegate.laboratoryattendancesystemwithidentificationcard.model.CoreTime
import com.geeksonthegate.laboratoryattendancesystemwithidentificationcard.model.Lab
import com.geeksonthegate.laboratoryattendancesystemwithidentificationcard.model.Student
import io.realm.Realm
import io.realm.RealmList
import kotlinx.android.synthetic.main.activity_student_setting.*
import java.lang.Exception
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
        val labId = intent.getStringExtra("lab_id")
        val nextIntent = Intent(this, MainActivity::class.java)
        val labIntent = Intent(this, LabSettingActivity::class.java)

        //タイトルの設定
        when (scanLabel) {
            getString(R.string.register) -> {
                setTitle(R.string.register_student)
            }
            getString(R.string.edit) -> {
                setTitle(R.string.edit_student)
            }
        }


        // 前画面から受け取ったIDmで検索する
        var student: Student? = realm.where(Student::class.java).equalTo("idm", Arrays.toString(idm)).findFirst()

        // コアタイム一覧を生成するメソッドに渡す一覧を生成・初期化

        var coreTimeList = mutableListOf<CoreTime>()
        for (i in 0..6) {
            coreTimeList.add(CoreTime(
                    GregorianCalendar(
                            2000, 0, 1, 10, 0).time,
                    GregorianCalendar(
                            2000, 0, 1, 17, 0).time,
                    true))
        }

        // 研究室一覧を受け取り、Spinner内のリストを生成
        // TODO: スピナー内の文字をデザインに合わせる
        val results = realm.where(Lab::class.java).findAll()
        val labList = mutableListOf<Lab>()
        labList.addAll(results.subList(0, results.size))
        labList.add(Lab(labName = "新規", coreTimeArray = listToRealmList(coreTimeList)))
        // スピナーの初期位置を所属研究室に合わせる（新規登録の場合は新規研究室）
        var defaultPosition: Int = labList.size - 1
        for (i in 0 until labList.size) {
            if (labList[i].labId == student?.lab?.labId) defaultPosition = i
        }
        // 前画面が研究室編集画面だった場合は初期位置を編集した研究室に合わせる
        if (labId != null) {
            for (i in 0 until labList.size) {
                if (labList[i].labId == labId) defaultPosition = i
            }
        }
        val adapter = LabAdapter(this, android.R.layout.simple_spinner_dropdown_item, labList)
        lab_spinner.adapter = adapter
        lab_spinner.setSelection(defaultPosition)
        // スピナー内の選択肢が選ばれたときの動作
        lab_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val spinner = parent as Spinner
                val item = spinner.selectedItem as Lab
                coreTimeList = item.coreTimeArray ?: coreTimeList
                setCoreTimeArea(coreTimeList, scanLabel, idm)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // 前画面から受け取ったラベルを基に処理分岐
        when (scanLabel) {
            getString(R.string.register) -> setCoreTimeArea(coreTimeList, scanLabel, idm)

            getString(R.string.edit) -> {
                setCoreTimeArea(student?.lab?.coreTimeArray ?: coreTimeList, scanLabel, idm)
                name_entry.setText(student?.name)
                studentid_entry.setText(student?.studentId)
            }
        }

        // 登録ボタンが押されたら画面情報を基にStudentデータを登録
        // 研究室名が「新規」の場合は登録を拒否し研究室入力を促す
        user_register_button.setOnClickListener {
            try {
                val selectedLab = lab_spinner.selectedItem as Lab
                if (selectedLab.labName != "新規") {
                    student = Student(Arrays.toString(idm), studentid_entry.text.toString(), name_entry.text.toString(), selectedLab)
                    realm.executeTransaction { it.insertOrUpdate(student) }
                    startActivity(nextIntent)
                } else {
                    labIntent.putExtra("scan_label", scanLabel)
                    labIntent.putExtra("idm", idm)
                    //研究室名が「新規」の場合はlab_Idがないので空文字
                    labIntent.putExtra("lab_id", "")
                    startActivity(labIntent)
                }
            } catch (e: Exception) {
                if (e.message == "Invalid studentId") {
                    Toast.makeText(this, "学籍番号は7文字で入力してください", Toast.LENGTH_SHORT).show()
                }
                if (e.message == "Invalid name") {
                    Toast.makeText(this, "名前は1文字以上7文字で以下で入力してください", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    // コアタイム一覧を受け取りレイアウト内に一覧を表示するメソッド
    private fun setCoreTimeArea(coreTimeList: List<CoreTime>, scanLabel: String, idm: ByteArray) {
        val sendLab = lab_spinner.selectedItem as Lab
        val nextIntent = Intent(this, LabSettingActivity::class.java)
        // TODO: idmは送る必要がないはずの情報 研究室情報画面から戻ってきたときに状態が保存されるようにする方法を探す
        nextIntent.putExtra("lab_id", sendLab.labId)
        nextIntent.putExtra("scan_label", scanLabel)
        nextIntent.putExtra("idm", idm)
        for (i in 0..6) {
            startCoreTimeLabelList[i].text = DateFormat.format("kk:mm", coreTimeList[i].startCoreTime)
            endCoreTimeLabelList[i].text = DateFormat.format("kk:mm", coreTimeList[i].endCoreTime)
            isCoreDayBoxList[i].isChecked = coreTimeList[i].isCoreDay ?: true
            startCoreTimeLabelList[i].setOnClickListener {
                nextIntent.putExtra("scan_label",scanLabel)
                startActivity(nextIntent)
            }
            endCoreTimeLabelList[i].setOnClickListener {
                nextIntent.putExtra("scan_label",scanLabel)
                startActivity(nextIntent)
            }
            isCoreDayBoxList[i].setOnClickListener {
                nextIntent.putExtra("scan_label",scanLabel)
                startActivity(nextIntent)
            }
        }
    }

    // ListからRealmListへ変換するメソッド
    // TODO: 不要なメソッド 研究室情報画面と同じように処理をすることでこのメソッドを削減する
    private fun <T> listToRealmList(list: List<T>): RealmList<T> {
        val realmList = RealmList<T>()
        for (item in list) {
            realmList.add(item)
        }
        return realmList
    }
}
