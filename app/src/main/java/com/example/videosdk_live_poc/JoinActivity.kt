package com.example.videosdk_live_poc

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
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
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

class JoinActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {

  //Replace with the token you generated from the VideoSDK Dashboard
  private var sampleToken  = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhcGlrZXkiOiI5YzZjMGVhZi01NTliLTRiYzQtYTQ5MC1kMDZlYWVkMjFjNjYiLCJwZXJtaXNzaW9ucyI6WyJhbGxvd19qb2luIl0sImlhdCI6MTY4NTUwMzM3NCwiZXhwIjoxNzAxMDU1Mzc0fQ.0vmY-Lc-_ddOCb-xvp5ubhE3G_oY5-2Lbw3vVXIuc8Q"

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_join)

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
    //check permission (Easy Permission)
    if(checkSelfPermission())
    {
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
    else
    {
      requestPermission()
      Toast.makeText(this,"All permission is not granted",Toast.LENGTH_SHORT).show()
    }



  }

  companion object{
    const val PERMISSION_REQ_CODE = 1
  }

  private fun checkSelfPermission() =
    EasyPermissions.hasPermissions(
      this,
        android.Manifest.permission.BLUETOOTH,
        android.Manifest.permission.BLUETOOTH_CONNECT,
        android.Manifest.permission.BLUETOOTH_ADMIN,
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.RECORD_AUDIO
    )


  private fun requestPermission(){
    EasyPermissions.requestPermissions(
      this,
      "This permission is required",
      PERMISSION_REQ_CODE,
      android.Manifest.permission.BLUETOOTH,
      android.Manifest.permission.BLUETOOTH_CONNECT,
      android.Manifest.permission.BLUETOOTH_ADMIN,
      android.Manifest.permission.CAMERA,
      android.Manifest.permission.RECORD_AUDIO
    )
  }

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,this)
  }


  override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
    Toast.makeText(this,"All permission Granted",Toast.LENGTH_SHORT).show()
  }

  override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
    if(EasyPermissions.somePermissionPermanentlyDenied(this,perms))
    {
      AppSettingsDialog.Builder(this).build().show()
    }
    else{
      requestPermission()
    }
  }

}