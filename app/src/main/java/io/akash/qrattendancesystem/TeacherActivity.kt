package io.akash.qrattendancesystem

import android.R
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import io.akash.qrattendancesystem.databinding.ActivityTeacherBinding

class TeacherActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTeacherBinding
    private val db = FirebaseFirestore.getInstance()

    private val classList = ArrayList<String>()
    private val classIdList = ArrayList<String>()

    override fun onStart() {
        super.onStart()

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityTeacherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Insets handling (optional now)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, systemBars.top, 0, systemBars.bottom)
            insets
        }

        WindowCompat.getInsetsController(window, window.decorView)
            ?.isAppearanceLightStatusBars = false

        loadClasses()

        binding.btnGenerate.setOnClickListener {

            // 👇 NEW: selected class (abhi use nahi kar rahe, next step me use hoga)
            val position = binding.spinnerClass.selectedItemPosition
            val selectedClassId = if (classIdList.isNotEmpty()) classIdList[position] else ""

            val user = FirebaseAuth.getInstance().currentUser

            val sessionId = "session_${System.currentTimeMillis()}_${user?.uid}"

            val qrBitmap = generateQR(sessionId)

            binding.qrImage.setImageBitmap(qrBitmap)

        }

        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun loadClasses() {

        db.collection("classes")
            .get()
            .addOnSuccessListener { result ->

                classList.clear()
                classIdList.clear()

                for (doc in result) {
                    val className = doc.getString("className") ?: ""

                    classList.add(className)
                    classIdList.add(doc.id)
                }

                val adapter = ArrayAdapter(
                    this,
                    R.layout.simple_spinner_item,
                    classList
                )

                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                binding.spinnerClass.adapter = adapter
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load classes ❌", Toast.LENGTH_SHORT).show()
            }
    }

    fun generateQR(text: String): Bitmap {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 512, 512)

        val bmp = Bitmap.createBitmap(512, 512, Bitmap.Config.RGB_565)
        for (x in 0 until 512) {
            for (y in 0 until 512) {
                bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bmp
    }

}