package io.akash.qrattendancesystem

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import io.akash.qrattendancesystem.databinding.ActivityTeacherBinding

class TeacherActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTeacherBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTeacherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnGenerate.setOnClickListener {

            val data = hashMapOf(
                "name" to "Akash",
                "time" to System.currentTimeMillis()
            )

            db.collection("test")
                .add(data)
                .addOnSuccessListener {
                    Log.d("FIREBASE", "Data added ✅")
                }
                .addOnFailureListener {
                    Log.e("FIREBASE", "Error ❌")
                }
        }
    }
}