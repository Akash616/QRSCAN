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

    private lateinit var fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient

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

        fusedLocationClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(this)

        loadClasses()

        binding.btnGenerate.setOnClickListener {

            if (!isLocationEnabled()) {
                showLocationDialog()
                return@setOnClickListener
            }

            val position = binding.spinnerClass.selectedItemPosition
            if (classIdList.isEmpty()) {
                Toast.makeText(this, "No class found ❌", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedClassId = classIdList[position]

            val teacherId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener

            // Get location first
            getLocation { lat, lng ->

                val lectureId = "lec_${System.currentTimeMillis()}"

                // Save lecture in Firebase
                val lecture = hashMapOf(
                    "classId" to selectedClassId,
                    "teacherId" to teacherId,
                    "latitude" to lat,
                    "longitude" to lng,
                    "timestamp" to System.currentTimeMillis()
                )

                db.collection("lectures")
                    .document(lectureId)
                    .set(lecture)
                    .addOnSuccessListener {

                        //QR me sirf lectureId bhejna
                        val qrData = """
                {
                    "lectureId": "$lectureId"
                }
                """.trimIndent()

                        val qrBitmap = generateQR(qrData)
                        binding.qrImage.setImageBitmap(qrBitmap)

                        Toast.makeText(this, "QR Generated ✅", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error creating lecture ❌", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun getLocation(onLocationReady: (Double, Double) -> Unit) {

        if (androidx.core.content.ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {

            androidx.core.app.ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                100
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                onLocationReady(location.latitude, location.longitude)
            } else {
                Toast.makeText(this, "Location not found ❌", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as android.location.LocationManager
        return locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)
    }

    private fun showLocationDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Location Required 📍")
            .setMessage("Please enable location to generate QR")
            .setPositiveButton("Turn ON") { _, _ ->
                startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton("Cancel", null)
            .show()
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