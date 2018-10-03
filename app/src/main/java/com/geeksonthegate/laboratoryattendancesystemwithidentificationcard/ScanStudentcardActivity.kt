package com.geeksonthegate.laboratoryattendancesystemwithidentificationcard

import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.geeksonthegate.laboratoryattendancesystemwithidentificationcard.model.Student
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_scan_studentcard.*
import java.util.*


@Suppress("DEPRECATED_IDENTITY_EQUALS")
class ScanStudentcardActivity : AppCompatActivity() {

    private lateinit var realm: Realm

    /**
     * NFCアダプタのインスタンスを格納するプロパティ
     */
    private lateinit var nfcAdapter: NfcAdapter

    /**
     * 前画面で押されたボタンのIDを格納するプロパティ
     */
    // TODO: ここでidを宣言するか再考の余地あり
    var id: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_studentcard)
        realm = Realm.getDefaultInstance()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        id = intent.getIntExtra("scan_label", 0)

        // 前画面で押されたボタンに応じてラベルの内容を変更
        when (id) {
            R.id.enter -> {
                scan_card_label.setText(R.string.enter_label)
                setTitle(R.string.enter)
                background.setImageResource(R.drawable.bg_enter)
            }
            R.id.exit -> {
                scan_card_label.setText(R.string.exit_label)
                setTitle(R.string.exit)
                background.setImageResource(R.drawable.bg_exit)
            }
            R.id.register -> {
                scan_card_label.setText(R.string.register_label)
                setTitle(R.string.register)
            }
            R.id.edit -> {
                scan_card_label.setText(R.string.edit_label)
                setTitle(R.string.edit)
            }
        }

        // NFCアダプタのインスタンスを生成
        nfcAdapter = android.nfc.NfcAdapter.getDefaultAdapter(this)
    }

    override fun onResume() {
        super.onResume()

        // NFCがかざされたとき、このActivityに読み込まれるようにする
        val intent = Intent(this, ScanStudentcardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent: PendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, 0)
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null)
    }

    override fun onPause() {
        super.onPause()

        // このアプリが前面にない時はNFCがかざされても反応しないようにする
        nfcAdapter.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // 初期値として遷移先をメイン画面に設定
        var nextIntent = Intent(this, MainActivity::class.java)

        // NFCのEXTRA_IDを読み込み、前画面で押されたボタンと共に表示する
        val idm: ByteArray = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID) ?: run {
            Toast.makeText(this, "Failed to read NFC", Toast.LENGTH_SHORT).show()
            return
        }

        nextIntent.putExtra("scan_label", title)
        nextIntent.putExtra("idm", idm)

        when (id) {
            R.id.enter -> {
                when {
                    // 登録済みの学生の場合には確認画面に遷移する
                    isRegisteredCard(idm) -> {
                        nextIntent = Intent(this, RoomConfirmationActivity::class.java)
                        nextIntent.putExtra("scan_label", title)
                        nextIntent.putExtra("idm", idm)
                        startActivity(nextIntent)
                    }
                    // 未登録の学生の場合にはモーダルを表示し、メインに遷移
                    !isRegisteredCard(idm) -> {
                        unknownResistedCardModal(nextIntent)
                    }
                }
            }

            R.id.exit -> {
                when {
                    // 登録済みの学生の場合には確認画面に遷移する
                    isRegisteredCard(idm) -> {
                        nextIntent = Intent(this, RoomConfirmationActivity::class.java)
                        nextIntent.putExtra("scan_label", title)
                        nextIntent.putExtra("idm", idm)
                        startActivity(nextIntent)
                    }
                    // 未登録の学生の場合にはモーダルを表示し、メインに遷移
                    !isRegisteredCard(idm) -> {
                        unknownResistedCardModal(nextIntent)
                    }
                }
            }

            R.id.register -> {
                when {
                    // 登録済みの学生の場合には、モーダルを表示してメインに遷移
                    isRegisteredCard(idm) -> {
                        existCardModal(nextIntent)
                    }
                    // 未登録の学生の場合には登録画面に遷移
                    !isRegisteredCard(idm) -> {
                        nextIntent = Intent(this, StudentSettingActivity::class.java)
                        nextIntent.putExtra("scan_label", title)
                        nextIntent.putExtra("idm", idm)
                        startActivity(nextIntent)
                    }
                }
            }

            R.id.edit -> {
                when {
                    // 登録済みの学生の場合には編集画面に遷移する
                    isRegisteredCard(idm) -> {
                        nextIntent = Intent(this, StudentSettingActivity::class.java)
                        nextIntent.putExtra("scan_label", title)
                        nextIntent.putExtra("idm", idm)
                        startActivity(nextIntent)
                    }
                    // 未登録の学生の場合にはメインに遷移し, モーダルを表示
                    !isRegisteredCard(idm) -> {
                        unknownResistedCardModal(nextIntent)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    /*登録済みの学生証かどうかを判断する
    * 登録済みなら true _ 未登録ならfalseを返す*/
    private fun isRegisteredCard(idm: ByteArray): Boolean {
        return when {
            realm.where(Student::class.java).equalTo("idm", Arrays.toString(idm)).findFirst() == null -> false
            else -> true
        }
    }

    /*
    スキャンされたカードが未登録の場合に表示するモーダル
    入室・退室・登録に未登録カードがスキャンされた場合に表示
    */
    private fun unknownResistedCardModal(nextIntent: Intent) {
        setContentView(R.layout.activity_scan_studentcard)
        AlertDialog.Builder(this).apply {
            setCancelable(false)
            setTitle("この学生証は未登録です")
            setMessage("トップから登録ボタンをタップして\n学生情報を登録してください")
            setPositiveButton("OK", { _, _ ->
                // OKがタップされたらMain画面に遷移
                startActivity(nextIntent)
            })
            show()
        }
    }

    /*
    スキャンされたカードが登録済みの場合に表示するモーダル
    登録にすでに登録ずみカードがスキャンされたときに表示
    */
    private fun existCardModal(nextIntent: Intent) {

        setContentView(R.layout.activity_scan_studentcard)
        AlertDialog.Builder(this).apply {
            setCancelable(false)
            setTitle("この学生証は登録済みです")
            setPositiveButton("OK", { _, _ ->
                // OKがタップされたらMain画面に遷移
                startActivity(nextIntent)
            })
            show()
        }
    }
}
