package com.example.videosdk_live_poc

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AndroidException
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import live.videosdk.rtc.android.Meeting
import live.videosdk.rtc.android.Participant
import live.videosdk.rtc.android.VideoSDK
import live.videosdk.rtc.android.listeners.MeetingEventListener
import org.json.JSONException
import org.json.JSONObject
import java.lang.Exception

class MeetingActivity : AppCompatActivity() {

  // declare the variables we will be using to handle the meeting
  private var meeting: Meeting? = null
  private var micEnabled = true
  private var webcamEnabled = true
  private var token : String? = null
  var meetingId : String? = null

  @SuppressLint("MissingInflatedId")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_meeting)
    token = intent.getStringExtra("token")
    val meetingId = intent.getStringExtra("meetingId")
    val customId = intent.getStringExtra("customId")
    val participantName = "John Doe"

    // 1. Configuration VideoSDK with Token
    VideoSDK.config(token)

    getMeetingID(customId)

    // 2. Initialize VideoSDK Meeting

    try {
      meeting = VideoSDK.initMeeting(
        this@MeetingActivity, meetingId, participantName,
        micEnabled, webcamEnabled, null, null, false, null
      )
    } catch (e: Exception) {
      Toast.makeText(this, "No such meeting available", Toast.LENGTH_LONG).show()
    }


    // 3. Add event listener for listening upcoming events
    meeting!!.addEventListener(meetingEventListener)
    //4. Join VideoSDK Meeting
    meeting!!.join()

    (findViewById<View>(R.id.tvMeetingId) as TextView).text = customId
    val endButton: Button = findViewById(R.id.endRoom)



    endButton.setOnClickListener {
      meeting!!.end()
      AndroidNetworking.post("https://api.videosdk.live/v2/rooms/deactivate")
        .addHeaders("Authorization", token)
        .addBodyParameter("roomId", meetingId)//we will pass the token in the Headers
        .build()
        .getAsJSONObject(object : JSONObjectRequestListener {
          override fun onResponse(response: JSONObject) {
            try {
              if (response.getBoolean("disabled")) {
                Toast.makeText(
                  this@MeetingActivity, "Room Deleted",
                  Toast.LENGTH_SHORT
                ).show()
              } else {
                Toast.makeText(
                  this@MeetingActivity, "Room not Deleted",
                  Toast.LENGTH_SHORT
                ).show()
              }
            } catch (e: JSONException) {
              e.printStackTrace()
            }
          }

          override fun onError(anError: ANError) {
            anError.printStackTrace()
            Toast.makeText(
              this@MeetingActivity, anError.message,
              Toast.LENGTH_SHORT
            ).show()
          }
        })
    }




    val rvParticipants = findViewById<RecyclerView>(R.id.rvParticipants)
    rvParticipants.layoutManager = GridLayoutManager(this, 2)
    rvParticipants.adapter = ParticipantAdapter(meeting!!)

    setActionListeners()

  }

  private fun getMeetingID(customId: String?) {
    AndroidNetworking.get("https://api.videosdk.live/v2/rooms/${customId}")
      .addHeaders("Authorization", token)
      .build()
      .getAsJSONObject(object : JSONObjectRequestListener {
        override fun onResponse(response: JSONObject) {
          meetingId = response.getString("roomId")
          Log.e("pID",meetingId.toString())
        }

        override fun onError(anError: ANError) {
          anError.printStackTrace()
          Toast.makeText(
            this@MeetingActivity, anError.message,
            Toast.LENGTH_SHORT
          ).show()
        }
      })
  }

  private fun validateMeetingId(meetingId: String?) {

    AndroidNetworking.get("https://api.videosdk.live/v2/rooms/${meetingId}")
      .addHeaders("Authorization", token)
      .build()
      .getAsJSONObject(object : JSONObjectRequestListener {
        override fun onResponse(response: JSONObject) {
          if(response.getBoolean("disabled"))
          {
            Toast.makeText(
              this@MeetingActivity, "Meeting is Already End !",
              Toast.LENGTH_SHORT
            ).show()
            this@MeetingActivity.finish()
          }

        }

        override fun onError(anError: ANError) {
          anError.printStackTrace()
          Toast.makeText(
            this@MeetingActivity, anError.message,
            Toast.LENGTH_SHORT
          ).show()
        }
      })

  }

  // creating the MeetingEventListener
  private val meetingEventListener: MeetingEventListener = object : MeetingEventListener() {
    override fun onMeetingJoined() {
      Log.d("#meeting", "onMeetingJoined()")
      if (meeting!!.participants.size > 1) {
        this@MeetingActivity.finish()
        Toast.makeText(
          this@MeetingActivity, "Not more than 2 Participants",
          Toast.LENGTH_SHORT
        ).show()
        meeting!!.leave()
      }

    }

    override fun onMeetingLeft() {
      Log.d("#meeting", "onMeetingLeft()")
      meeting = null
      if (!isDestroyed) finish()
    }

    override fun onParticipantJoined(participant: Participant) {
      Toast.makeText(
        this@MeetingActivity, participant.displayName + " joined",
        Toast.LENGTH_SHORT
      ).show()
    }

    override fun onParticipantLeft(participant: Participant) {
      Toast.makeText(
        this@MeetingActivity, participant.displayName + " left",
        Toast.LENGTH_SHORT
      ).show()
    }
  }

  private fun setActionListeners() {
    // toggle mic
    findViewById<View>(R.id.btnMic).setOnClickListener { view: View? ->
      if (micEnabled) {
        // this will mute the local participant's mic
        meeting!!.muteMic()
        Toast.makeText(this@MeetingActivity, "Mic Muted", Toast.LENGTH_SHORT).show()
      } else {
        // this will unmute the local participant's mic
        meeting!!.unmuteMic()
        Toast.makeText(this@MeetingActivity, "Mic Enabled", Toast.LENGTH_SHORT).show()
      }
      micEnabled = !micEnabled
    }
    // toggle webcam
    findViewById<View>(R.id.btnWebcam).setOnClickListener { view: View? ->
      if (webcamEnabled) {
        // this will disable the local participant webcam
        meeting!!.disableWebcam()
        Toast.makeText(this@MeetingActivity, "Webcam Disabled", Toast.LENGTH_SHORT).show()
      } else {
        // this will enable the local participant webcam
        meeting!!.enableWebcam()
        Toast.makeText(this@MeetingActivity, "Webcam Enabled", Toast.LENGTH_SHORT).show()
      }
      webcamEnabled = !webcamEnabled
    }
    // leave meeting
    findViewById<View>(R.id.btnLeave).setOnClickListener { view: View? ->
      // this will make the local participant leave the meeting
      meeting!!.leave()
    }
  }
}