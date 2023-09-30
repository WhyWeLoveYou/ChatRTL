package com.whyweloveyou.chatrtl.activites

import PreferenceManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.whyweloveyou.chatrtl.databinding.ActivitySignInBinding
import com.whyweloveyou.chatrtl.utilities.Constants


class SignIn : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferenceManager = PreferenceManager(applicationContext)
        if (preferenceManager.getBoolean(Constants().KEY_IS_SIGNED_IN)) {
            val intet: Intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intet)
            finish()
        }
        OnListener()
    }

    private fun OnListener() {
        binding.CreateAccount.setOnClickListener {
            val pindahsatu = Intent(this@SignIn, SignUp::class.java)
            startActivity(pindahsatu)
        }
        binding.ButtonLogin.setOnClickListener {
            if (isValidorNot()) {
                signIn()
            }
        }
        binding.ForgotPw.setOnClickListener {
            showToast("Error")
        }
    }

    private fun signIn() {
        loading(true)
        val database: FirebaseFirestore = FirebaseFirestore.getInstance()
        database.collection(Constants().KEY_COLLECTION_USERS)
            .whereEqualTo(Constants().KEY_EMAIL, binding.InputEmal.text.toString())
            .whereEqualTo(Constants().KEY_PASSWORD, binding.InputPw.text.toString())
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful && it.result != null && it.result.documents.size > 0) {
                    val documentSnapshot: DocumentSnapshot = it.result.documents.get(0)
                    preferenceManager.putBoolean(Constants().KEY_IS_SIGNED_IN, true)
                    preferenceManager.putString(Constants().KEY_USER_ID, documentSnapshot.id)
                    preferenceManager.putString(
                        Constants().KEY_NAME,
                        documentSnapshot.getString(Constants().KEY_NAME) ?: ""
                    )
                    preferenceManager.putString(
                        Constants().KEY_IMAGE,
                        documentSnapshot.getString(Constants().KEY_IMAGE) ?: ""
                    )
                    val intent: Intent = Intent(applicationContext, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                } else {
                    loading(false)
                    showToast("Unable to Login")
                }
            }
    }

    private fun loading(isLoading: Boolean) {
        if (isLoading) {
            binding.ButtonLogin.visibility = View.INVISIBLE
            binding.proglesBar.visibility = View.VISIBLE
        } else {
            binding.ButtonLogin.visibility = View.VISIBLE
            binding.proglesBar.visibility = View.INVISIBLE
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    private fun isValidorNot(): Boolean {
        if (binding.InputEmal.text.toString().isNullOrEmpty()) {
            showToast("Input Email")
            return false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.InputEmal.text.toString()).matches()) {
            showToast("Input Valid Email")
            return false
        } else if (binding.InputPw.text.toString().isNullOrEmpty()) {
            showToast("Input Password")
            return false
        } else {
            return true
        }
    }
}