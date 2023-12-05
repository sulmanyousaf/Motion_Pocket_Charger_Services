package com.funprimetechnology.app.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.funprimetechnology.app.se.ChargerService
import com.funprimetechnology.app.se.MotionService
import com.funprimetechnology.app.se.PocketService
import com.funprimetechnology.app.R
import com.funprimetechnology.app.utils.SessionManager

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var pocketButton: Button
    private lateinit var chargerButton: Button
    private lateinit var motionButton: Button

    private var isPocketServiceRunning = false
    private var isChargerServiceRunning = false
    private var isMotionServiceRunning = false

    private var pocketServiceIntent: Intent? = null
    private var chargerServiceIntent: Intent? = null
    private var motionServiceIntent: Intent? = null
    private var sessionManager: SessionManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize buttons
        pocketButton = findViewById(R.id.pocketButton)
        chargerButton = findViewById(R.id.chargerButton)
        motionButton = findViewById(R.id.motionButton)

        sessionManager = SessionManager(this@MainActivity)
        readBtnSession()

        // Set click listeners for buttons
        pocketButton.setOnClickListener(this)
        chargerButton.setOnClickListener(this)
        motionButton.setOnClickListener(this)

        // Initialize the intents for the services
        pocketServiceIntent = Intent(this, PocketService::class.java)
        chargerServiceIntent = Intent(this, ChargerService::class.java)
        motionServiceIntent = Intent(this, MotionService::class.java)
    }

    private fun readBtnSession(){
        //pocket service check
        if (sessionManager?.fetchPocketBtnState() == false){
            isPocketServiceRunning = false
            pocketButton.text = "Start Pocket Service"
        } else{
            isPocketServiceRunning = true
            pocketButton.text = "Stop Pocket Service"
        }
        //charger service check
        if (sessionManager?.fetchChargerBtnState() == false){
            isChargerServiceRunning = false
            chargerButton.text = "Start Charger Service"
        } else{
            isChargerServiceRunning = true
            chargerButton.text = "Stop Charger Service"
        }
        //motion service check
        if (sessionManager?.fetchMotionBtnState() == false){
            isMotionServiceRunning = false
            motionButton.text = "Start Motion Service"
        } else{
            isMotionServiceRunning = true
            motionButton.text = "Stop Motion Service"
        }
    }
    private fun upDateBtnSession(btnType : String, sessionType : Boolean){
        when(btnType){
            "Pocket"->{
                sessionManager?.savePocketBtnState(sessionType)
            }
            "Charger"->{
                sessionManager?.saveChargerBtnState(sessionType)
            }
            "Motion"->{
                sessionManager?.saveMotionBtnState(sessionType)
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.pocketButton -> {
                if (isPocketServiceRunning) {
                    upDateBtnSession("Pocket",false)
                    stopPocketService()
                } else {
                    upDateBtnSession("Pocket",true)
                    startPocketService()
                }
            }
            R.id.chargerButton -> {
                if (isChargerServiceRunning) {
                    upDateBtnSession("Charger",false)
                    stopChargerService()
                } else {
                    upDateBtnSession("Charger",true)
                    startChargerService()
                }
            }
            R.id.motionButton -> {
                if (isMotionServiceRunning) {
                    upDateBtnSession("Motion",false)
                    stopMotionService()
                } else {
                    upDateBtnSession("Motion    ",true)
                    startMotionService()
                }
            }
        }
    }

    private fun startPocketService() {
        isPocketServiceRunning = true
        pocketButton.text = "Stop Pocket Service"
        startService(pocketServiceIntent)
    }

    private fun stopPocketService() {
        isPocketServiceRunning = false
        pocketButton.text = "Start Pocket Service"
        stopService(pocketServiceIntent)
    }

    private fun startChargerService() {
        isChargerServiceRunning = true
        chargerButton.text = "Stop Charger Service"
        startService(chargerServiceIntent)
    }

    private fun stopChargerService() {
        isChargerServiceRunning = false
        chargerButton.text = "Start Charger Service"
        stopService(chargerServiceIntent)
    }

    private fun startMotionService() {
        isMotionServiceRunning = true
        motionButton.text = "Stop Motion Service"
        startService(motionServiceIntent)
    }

    private fun stopMotionService() {
        isMotionServiceRunning = false
        motionButton.text = "Start Motion Service"
        stopService(motionServiceIntent)
    }

}