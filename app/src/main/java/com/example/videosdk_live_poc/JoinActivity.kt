package com.example.videosdk_live_poc

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import org.json.JSONException
import org.json.JSONObject

class JoinActivity : AppCompatActivity() {

  //Replace with the token you generated from the VideoSDK Dashboard
  private var sampleToken  = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhcGlrZXkiOiI5YzZjMGVhZi01NTliLTRiYzQtYTQ5MC1kMDZlYWVkMjFjNjYiLCJwZXJtaXNzaW9ucyI6WyJhbGxvd19qb2luIl0sImlhdCI6MTY4NTUwMzM3NCwiZXhwIjoxNzAxMDU1Mzc0fQ.0vmY-Lc-_ddOCb-xvp5ubhE3G_oY5-2Lbw3vVXIuc8Q"

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_join)

    checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID)
    checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID)

    val btnCreate = findViewById<Button>(R.id.btnCreateMeeting)
    val btnJoin = findViewById<Button>(R.id.btnJoinMeeting)
    val etMeetingId = findViewById<EditText>(R.id.etMeetingId)

    btnCreate.setOnClickListener { v: View? ->
      createMeeting(sampleToken)
    }

    btnJoin.setOnClickListener { v: View? ->
      val intent = Intent(this@JoinActivity, MeetingActivity::class.java)
      intent.putExtra("token", sampleToken)
      intent.putExtra("meetingId", etMeetingId.text.toString())
      startActivity(intent)
    }

    val saveToken : Button = findViewById(R.id.btnSaveToken)
    val tokenEdt : EditText = findViewById(R.id.gwTokenEdt)

    saveToken.setOnClickListener {
      Toast.makeText(this,"Token Updated .....", Toast.LENGTH_SHORT).show()
      sampleToken = tokenEdt.text.toString()
    }

  }



  private fun createMeeting(token: String) {
    // we will make an API call to VideoSDK Server to get a roomId
    AndroidNetworking.post("https://api.videosdk.live/v2/rooms")
      .addHeaders("Authorization", token)
      .build()
      .getAsJSONObject(object : JSONObjectRequestListener {
        override fun onResponse(response: JSONObject) {
          try {
            // response will contain `roomId`
            val meetingId = response.getString("roomId")
            Log.e("response",response.toString())
            // starting the MeetingActivity with received roomId and our sampleToken
            val intent = Intent(this@JoinActivity, MeetingActivity::class.java)
            intent.putExtra("token", sampleToken)
            intent.putExtra("meetingId", meetingId)
            startActivity(intent)
          } catch (e: JSONException) {
            e.printStackTrace()
          }
        }

        override fun onError(anError: ANError) {
          anError.printStackTrace()
          Toast.makeText(
            this@JoinActivity, anError.message,
            Toast.LENGTH_SHORT
          ).show()
        }
      })

  }

  companion object {
    private const val PERMISSION_REQ_ID = 22
    private val REQUESTED_PERMISSIONS = arrayOf(
      android.Manifest.permission.RECORD_AUDIO,
      android.Manifest.permission.CAMERA
    )
  }

  private fun checkSelfPermission(permission: String, requestCode: Int): Boolean {
    if (ContextCompat.checkSelfPermission(this, permission) !=
      PackageManager.PERMISSION_GRANTED)
    {
      ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, requestCode)
      return false
    }
    return true
  }


}