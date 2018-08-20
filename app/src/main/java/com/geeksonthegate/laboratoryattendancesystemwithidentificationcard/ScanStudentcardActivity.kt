package com.geeksonthegate.laboratoryattendancesystemwithidentificationcard

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import java.util.*

class ScanStudentcardActivity : AppCompatActivity() {

    /**
     * NFCアダプタのインスタンスを格納するプロパティ
     */
    private lateinit var mNfcAdapter: NfcAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_studentcard)

        val id = intent.getIntExtra("scan_label",0)
        when(id) {
            R.id.enter -> findViewById<TextView>(R.id.scanCardLabel).setText(R.string.enter_label)
            R.id.exit ->  findViewById<TextView>(R.id.scanCardLabel).setText(R.string.exit_label)
        }

        // NFCアダプタのインスタンスを生成
        mNfcAdapter = android.nfc.NfcAdapter.getDefaultAdapter(this)
    }

    override fun onResume() {
        super.onResume()
        // NFCがかざされたとき、このActivityに読み込まれるようにする
        val intent = Intent(this, ScanStudentcardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent: PendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, 0)
        mNfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null)
    }

    override fun onPause() {
        super.onPause()
        // このアプリが前面にない時はNFCがかざされても反応しないようにする
        mNfcAdapter.disableForegroundDispatch(this)
    }

    @SuppressLint("ShowToast")
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        // NFCのEXTRA_IDを読み込み表示する
        val uid: ByteArray = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID) ?: run {
            Toast.makeText(this, "Failed to read NFC", Toast.LENGTH_SHORT)
            return
        }
        Toast.makeText(this, Arrays.toString(uid), Toast.LENGTH_SHORT).show()
    }
}
