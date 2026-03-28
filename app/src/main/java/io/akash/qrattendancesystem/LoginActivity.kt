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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Teacher button click
        binding.btnTeacher.setOnClickListener {
            startActivity(Intent(this, TeacherActivity::class.java))
        }

        // Student button click
        binding.btnStudent.setOnClickListener {
            startActivity(Intent(this, StudentActivity::class.java))
        }
    }
}