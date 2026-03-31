package io.akash.qrattendancesystem

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.integration.android.IntentIntegrator
import io.akash.qrattendancesystem.databinding.ActivityStudentBinding

class StudentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onStart() {
        super.onStart()

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnScan.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            startQRScanner()
        }

        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun startQRScanner() {
        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setPrompt("Scan QR Code")
        integrator.setBeepEnabled(true)
        integrator.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        binding.progressBar.visibility = View.GONE

        if (result != null) {
            if (result.contents != null) {

                val sessionId = result.contents
                Log.d("QR", "Scanned: $sessionId")

                saveAttendance(sessionId)

            } else {
                Log.d("QR", "Scan cancelled")
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun saveAttendance(sessionId: String) {

        binding.progressBar.visibility = View.VISIBLE

        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser

        val attendance = hashMapOf(
            "studentId" to user?.uid,
            "sessionId" to sessionId,
            "time" to System.currentTimeMillis()
        )

        /*val attendance = hashMapOf(
            "studentName" to "Akash",
            "sessionId" to sessionId,
            "time" to System.currentTimeMillis()
        )*/

        db.collection("attendance")
            .add(attendance)
            .addOnSuccessListener {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, "Attendance Marked ✅", Toast.LENGTH_SHORT).show()
                Log.d("FIREBASE", "Attendance Saved ✅")
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, "Error ❌", Toast.LENGTH_SHORT).show()
                Log.e("FIREBASE", "Error ❌")
            }
    }
}