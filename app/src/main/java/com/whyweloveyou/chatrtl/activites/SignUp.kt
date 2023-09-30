package com.whyweloveyou.chatrtl.activites

import PreferenceManager
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.whyweloveyou.chatrtl.databinding.ActivitySignUpBinding
import com.whyweloveyou.chatrtl.utilities.Constants
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.InputStream
import java.util.Base64


class SignUp : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private var encodedImage: String? = null
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferenceManager = PreferenceManager(applicationContext)
        OnListener()
    }

    private fun OnListener() {
        binding.ButtonLogin.setOnClickListener {
            if (isValidOrNot()) {
                signUp()
            }
        }
        binding.LoginAccount.setOnClickListener {
            val pindahdua = Intent(this@SignUp, SignIn::class.java)
            startActivity(pindahdua)
        }
        binding.layoutIme.setOnClickListener {
            val pindahtiga = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pindahtiga.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            pickImage.launch(pindahtiga)
        }
    }

    private fun showToast(message: String) {
        val show = Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    private fun signUp() {
        loading(true)
        val database: FirebaseFirestore = FirebaseFirestore.getInstance()
        val email = binding.InputEmal.text.toString()

        database.collection(Constants().KEY_COLLECTION_USERS)
            .whereEqualTo(Constants().KEY_EMAIL, email)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (task.result?.isEmpty == true) {
                        val dbt: HashMap<String, Any> = HashMap()
                        dbt[Constants().KEY_NAME] = binding.InputName.text.toString()
                        dbt[Constants().KEY_EMAIL] = email
                        dbt[Constants().KEY_PASSWORD] = binding.InputPw.text.toString()
                        dbt[Constants().KEY_IMAGE] = encodedImage ?: ""

                        database.collection(Constants().KEY_COLLECTION_USERS)
                            .add(dbt)
                            .addOnSuccessListener { documentReference ->
                                loading(false)
                                preferenceManager.putBoolean(Constants().KEY_IS_SIGNED_IN, true)
                                preferenceManager.putString(Constants().KEY_USER_ID, documentReference.id)
                                preferenceManager.putString(Constants().KEY_NAME, binding.InputName.text.toString())
                                preferenceManager.putString(Constants().KEY_IMAGE, encodedImage ?: "")
                                val intent = Intent(applicationContext, MainActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                startActivity(intent)
                            }
                            .addOnFailureListener { e ->
                                loading(false)
                                showToast(e.message.toString())
                            }
                    } else {
                        loading(false)
                        showToast("Email sudah digunakan, silakan gunakan email lain.")
                    }
                } else {
                    loading(false)
                    showToast("Terjadi kesalahan saat memeriksa email: ${task.exception?.message}")
                }
            }
    }

    private fun encodeImage(bitmap: Bitmap): String {
        var previewWidth = 150
        var previewHeight = bitmap.height * previewWidth / bitmap.width
        val previewBitmap: Bitmap =
            Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false)
        val byteArrayOutputStream: ByteArrayOutputStream = ByteArrayOutputStream()
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
        val bytes: ByteArray = byteArrayOutputStream.toByteArray()
        return Base64.getEncoder().encodeToString(bytes)
    }

    private val pickImage: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            if (result.data != null) {
                val imageUri = result.data!!.data
                try {
                    val inputStream: InputStream? = contentResolver.openInputStream(imageUri!!)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    binding.imageProfile.setImageBitmap(bitmap)
                    binding.addingImage.visibility = View.GONE
                    encodedImage = encodeImage(bitmap)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun isValidOrNot(): Boolean {
        val pw = binding.InputPw.text.toString()
        val cpw = binding.InputCPw.text.toString()
        if (encodedImage == null) {
            showToast("Select Image")
            return false
        } else if (binding.InputName.text.toString().equals("")) {
            showToast("Enter Name")
            return false
        } else if (binding.InputEmal.text.toString().equals("")) {
            showToast("Input Email")
            return false
        } else if (binding.InputPw.text.toString().equals("")) {
            showToast("Input Password")
            return false
        } else if (pw != cpw) {
            showToast("Password and confirm password doesn't match")
            return false
        } else {
            return true
        }
    }

    private fun loading(isLoading: Boolean) {
        if (isLoading) {
            binding.ButtonLogin.visibility = View.INVISIBLE
            binding.progesLoading.visibility = View.VISIBLE
        } else {
            binding.progesLoading.visibility = View.INVISIBLE
            binding.ButtonLogin.visibility = View.VISIBLE
        }
    }

}