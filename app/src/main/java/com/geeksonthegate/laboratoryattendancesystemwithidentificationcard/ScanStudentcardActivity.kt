package com.geeksonthegate.laboratoryattendancesystemwithidentificationcard

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import io.realm.Realm
import com.geeksonthegate.laboratoryattendancesystemwithidentificationcard.model.Student
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_scan_studentcard.*
import java.util.*

class ScanStudentcardActivity : AppCompatActivity() {

    private lateinit var realm: Realm

    /**
     * NFCアダプタのインスタンスを格納するプロパティ
     */
    private lateinit var nfcAdapter: NfcAdapter

    /**
     * 前画面で押されたボタンのIDを格納するプロパティ
     */
    // TODO: 宣言位置の再考
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
        // TODO: 初期値の再考
        var nextIntent = Intent(this, MainActivity::class.java)

        // NFCのEXTRA_IDを読み込み、前画面で押されたボタンと共に表示する
        val idm: ByteArray = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID) ?: run {
            Toast.makeText(this, "Failed to read NFC", Toast.LENGTH_SHORT).show()
            return
        }

        // タイトルによって次に飛ぶActivityを選択する
        when (id) {
            R.id.enter -> {
                nextIntent = Intent(this, RoomConfirmationActivity::class.java)
            }
            R.id.exit -> {
                nextIntent = Intent(this, RoomConfirmationActivity::class.java)
            }
            R.id.register -> {
                nextIntent = Intent(this, StudentSettingActivity::class.java)
            }
            R.id.edit -> {
                nextIntent = Intent(this, StudentSettingActivity::class.java)
            }
        }
        nextIntent.putExtra("scan_label", title)
        nextIntent.putExtra("idm", idm)
        startActivity(nextIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }
}
