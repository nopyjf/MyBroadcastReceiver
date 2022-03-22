package com.example.mybroadcastreceiver

import android.Manifest
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.mybroadcastreceiver.ui.theme.MyBroadcastReceiverTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainActivity : ComponentActivity() {

    private val _otpState: MutableStateFlow<String> = MutableStateFlow("Test")
    private val otpState: StateFlow<String> = _otpState

    private val broadcast = MyBroadcastReceiver()

    private val requestPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            receiveBroadcast()
            displayScreen()
        } else {
            Log.d("NOPY", "Permission Denied")
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED -> {
                receiveBroadcast()
                displayScreen()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.RECEIVE_SMS) -> {
                Log.d("NOPY", "You shoud allow permission to received SMS")
            }
            else -> {
                requestPermission.launch(Manifest.permission.RECEIVE_SMS)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadcast)
    }

    private fun receiveBroadcast() {
        broadcast.callDisplayOtp { displayOtp(it) }
        val filter = IntentFilter(SMS_RECEIVED)
        registerReceiver(broadcast, filter, Manifest.permission.RECEIVE_SMS, null)
    }

    private fun displayOtp(otp: String) {
        _otpState.value = otp
    }

    private fun displayScreen() {
        setContent {
            MyBroadcastReceiverTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    // OTP Screen
                    OtpScreen()
                }
            }
        }
    }

    @Composable
    fun OtpScreen() {
        Scaffold(
            topBar = { MyTopAppBar() },
            content = { MyContent() }
        )
    }

    @Preview(showBackground = true)
    @Composable
    fun DefaultPreview() {
        MyBroadcastReceiverTheme {
            OtpScreen()
        }
    }

    @Composable
    fun MyTopAppBar() {
        TopAppBar(
            title = { Text(getString(R.string.app_name)) }
        )
    }

    @Composable
    fun MyContent() {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.padding(36.dp))
            OtpHeader()
            Spacer(modifier = Modifier.padding(16.dp))
            OtpTextField()
        }
    }

    @Composable
    fun OtpHeader() {
        Text(
            text = "OTP",
            fontSize = 24.sp
        )
    }

    @Composable
    fun OtpTextField() {
        OutlinedTextField(
            value = otpState.collectAsState().value,
            onValueChange = { /* do nothing */ },
            readOnly = true
        )
    }

    companion object {
        private const val SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED"
    }
}