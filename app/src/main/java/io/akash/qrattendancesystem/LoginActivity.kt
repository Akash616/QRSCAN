package io.akash.qrattendancesystem

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import io.akash.qrattendancesystem.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onStart() {
        super.onStart()

        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser

        if (user != null) {
            // Already logged in

            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

            db.collection("users")
                .document(user.uid)
                .get()
                .addOnSuccessListener { doc ->

                    val role = doc.getString("role")

                    if (role == "teacher") {
                        startActivity(Intent(this, TeacherActivity::class.java))
                    } else {
                        startActivity(Intent(this, StudentActivity::class.java))
                    }

                    finish()
                }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Teacher button click
        binding.btnTeacher.setOnClickListener {
            //startActivity(Intent(this, TeacherActivity::class.java))
            val intent = Intent(this, TeacherStudentActivity::class.java)
            intent.putExtra("ROLE", "teacher")
            startActivity(intent)
        }

        // Student button click
        binding.btnStudent.setOnClickListener {
            //startActivity(Intent(this, StudentActivity::class.java))
            val intent = Intent(this, TeacherStudentActivity::class.java)
            intent.putExtra("ROLE", "student")
            startActivity(intent)
        }
    }
}