package io.akash.qrattendancesystem

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import io.akash.qrattendancesystem.databinding.ActivityTeacherStudentBinding

class TeacherStudentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTeacherStudentBinding
    private lateinit var auth: FirebaseAuth

    private var role: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTeacherStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        role = intent.getStringExtra("ROLE") ?: ""

        binding.titleTxt.text = if (role == "teacher") {
            "Teacher Login"
        } else {
            "Student Login"
        }

        binding.loginBtn.setOnClickListener {

            val email = binding.emailEdt.text.toString().trim()
            val password = binding.passwordEdt.text.toString().trim()

            if (email.isEmpty()) {
                binding.emailLayout.error = "Enter email"
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                binding.passwordLayout.error = "Enter password"
                return@setOnClickListener
            }

            loginUser(email, password)
        }
    }

    private fun loginUser(email: String, password: String) {

        binding.progressBar.visibility = View.VISIBLE

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->

                val uid = result.user?.uid ?: return@addOnSuccessListener

                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .get()
                    .addOnSuccessListener { doc ->

                        binding.progressBar.visibility = View.GONE

                        val userRole = doc.getString("role")

                        if (userRole == role) {

                            Toast.makeText(this, "Login Success ✅", Toast.LENGTH_SHORT).show()

                            if (role == "teacher") {
                                startActivity(Intent(this, TeacherActivity::class.java))
                            } else {
                                startActivity(Intent(this, StudentActivity::class.java))
                            }

                            finish()

                        } else {
                            Toast.makeText(this, "Wrong role ❌", Toast.LENGTH_SHORT).show()
                            auth.signOut()
                        }
                    }
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, "Login Failed ❌", Toast.LENGTH_SHORT).show()
            }
    }
}