package com.whyweloveyou.chatrtl.projek4

import PreferenceManager
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.whyweloveyou.chatrtl.R
import com.whyweloveyou.chatrtl.activites.MainActivity
import com.whyweloveyou.chatrtl.activites.SignIn
import com.whyweloveyou.chatrtl.databinding.ActivityProjekEmpatMainBinding
import com.whyweloveyou.chatrtl.utilities.Constants
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.InputStream
import java.util.Base64
import android.app.Activity

class ProjekEmpatMain : AppCompatActivity() {
    private lateinit var binding: ActivityProjekEmpatMainBinding
    private lateinit var preferenceManager: PreferenceManager
    private var encodedImage: String? = null
    private lateinit var firebaseFirestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProjekEmpatMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferenceManager = PreferenceManager(applicationContext)

        clickListener()
        getImageUser()
    }

    fun clickListener() {
        binding.BukaUrl.setOnClickListener {
            val nama = binding.Inputneme.text.toString()
            val gantiG: Intent = Intent(Intent.ACTION_VIEW, Uri.parse(nama))
            startActivity(gantiG)
        }
        binding.imageProfile.setOnClickListener {
            val pindahtiga =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pindahtiga.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            pickImage.launch(pindahtiga)
        }
        binding.ImgBack.setOnClickListener {
            val pindh = Intent(this@ProjekEmpatMain, MainActivity::class.java)
            pindh.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(pindh)
        }
        binding.Bsimaaao.setOnClickListener {
            if (valdiOrNot()) {
                Simpend()
            }
        }
    }

    private fun getImageUser() {
        val currentUserId = preferenceManager.getString(Constants().KEY_USER_ID).toString()
        val database: FirebaseFirestore = FirebaseFirestore.getInstance()

        database.collection(Constants().KEY_COLLECTION_USERS)
            .document(currentUserId)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val documentSnapshot = task.result
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        val base64ImageString = documentSnapshot.getString(Constants().KEY_IMAGE)

                        if (!base64ImageString.isNullOrEmpty()) {
                            val decodedBytes = Base64.getDecoder().decode(base64ImageString)
                            val bitmap: Bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                            binding.imageProfile.setImageBitmap(bitmap)
                        }
                    }
                }
            }
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
                    encodedImage = encodeImage(bitmap)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
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

    private fun showToast(message: String) {
        val toast = Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    private fun Simpend() {
        val nama = binding.Inputneme.text.toString()
        val emall = binding.InputEmal.text.toString()
        val database: FirebaseFirestore = FirebaseFirestore.getInstance()
        database.collection(Constants().KEY_COLLECTION_USERS)
            .whereEqualTo(Constants().KEY_EMAIL, emall)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (task.result?.isEmpty == true) {
                        val dbt: HashMap<String, Any> = HashMap()
                        dbt[Constants().KEY_NAME] = nama
                        dbt[Constants().KEY_EMAIL] = emall
                        if (encodedImage != null) {
                            dbt[Constants().KEY_IMAGE] = encodedImage ?: ""
                        }
                        val documentReference: DocumentReference =
                            database.collection(Constants().KEY_COLLECTION_USERS).document(
                                preferenceManager.getString(Constants().KEY_USER_ID).toString()
                            )
                        val updates: HashMap<String, Any> = HashMap<String, Any>()
                        updates.put(Constants().KEY_NAME, nama)
                        updates.put(Constants().KEY_EMAIL, emall)
                        if (encodedImage != null) {
                            updates.put(Constants().KEY_IMAGE, encodedImage ?: "")
                        }
                        documentReference.update(updates)
                            .addOnSuccessListener {
                                showToast("Data Berhasil Diubah")
                                val intent = Intent()
                                intent.putExtra(Constants().KEY_NAME, nama)
                                intent.putExtra(Constants().KEY_IMAGE, encodedImage ?: "")
                                setResult(Activity.RESULT_OK, intent)
                                finish()
                            }
                    } else {
                        showToast("Email sudah digunakan, silakan gunakan email lain.")
                    }
                } else {
                    showToast("Terjadi kesalahan saat memeriksa email: ${task.exception?.message}")
                }
            }
    }

    fun valdiOrNot(): Boolean {
        if (binding.Inputneme.text.isEmpty()) {
            showToast("Nama Tidak Boleh Kosong")
            return false
        } else if (binding.InputEmal.text.isEmpty()) {
            showToast("Email Tidak boleh Kosong")
            return false
        } else {
            return true
        }
    }
}