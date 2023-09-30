package com.whyweloveyou.chatrtl.activites

import PreferenceManager
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.PopupMenu
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.whyweloveyou.chatrtl.R
import com.whyweloveyou.chatrtl.databinding.ActivityMainBinding
import com.whyweloveyou.chatrtl.projek4.ProjekEmpatMain
import com.whyweloveyou.chatrtl.utilities.Constants
import java.util.Base64
import android.app.Activity
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.whyweloveyou.chatrtl.models.User

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var preferenceManager: PreferenceManager
    val PROFILE_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferenceManager = PreferenceManager(applicationContext)
        loadUserDetails()
        getToken()
        onListener()
    }

    private fun loadUserDetails() {
        val currentUserId = preferenceManager.getString(Constants().KEY_USER_ID).toString()
        val database: FirebaseFirestore = FirebaseFirestore.getInstance()

        database.collection(Constants().KEY_COLLECTION_USERS)
            .document(currentUserId)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val documentSnapshot = task.result
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        val userName = documentSnapshot.getString(Constants().KEY_NAME)
                        val base64ImageString = documentSnapshot.getString(Constants().KEY_IMAGE)
                        binding.Tname.text = userName

                        if (!base64ImageString.isNullOrEmpty()) {
                            val decodedBytes = Base64.getDecoder().decode(base64ImageString)
                            val bitmap: Bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                            binding.imageProfile.setImageBitmap(bitmap)
                        }
                    }
                }
            }
    }

    private fun onListener() {
        binding.menuBB.setOnClickListener {
            val popupMenu: PopupMenu = PopupMenu(applicationContext, binding.menuBB)
            popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                when(item.itemId) {
                    R.id.groupM -> showToast("Make Group")
                    R.id.ProfileB -> getUpdate()
                    R.id.LogoutBE -> signOut()
                }
                true
            })
            popupMenu.show()
        }
        binding.BnewChat.setOnClickListener {
            startActivity(Intent(applicationContext, UsersActivity::class.java))
        }
    }

    private fun getUpdate() {
        val intent = Intent(applicationContext, ProjekEmpatMain::class.java)
        startActivityForResult(intent, PROFILE_REQUEST_CODE)
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    private fun getToken() {
        FirebaseMessaging.getInstance().token.addOnSuccessListener(this::updateToken)
    }

    private fun updateToken(token: String) {
        val database: FirebaseFirestore = FirebaseFirestore.getInstance()
        val documentReference: DocumentReference =
            database.collection(Constants().KEY_COLLECTION_USERS).document(
                preferenceManager.getString(Constants().KEY_USER_ID).toString()
            )
        documentReference.update(Constants().KEY_FCM_TOKEN, token)
    }

    private fun signOut() {
        showToast("Signing out...")
        val database: FirebaseFirestore = FirebaseFirestore.getInstance()
        val documentReference: DocumentReference =
            database.collection(Constants().KEY_COLLECTION_USERS).document(
                preferenceManager.getString(Constants().KEY_USER_ID).toString()
            )
        val updates: HashMap<String, Any> = HashMap<String, Any>()
        updates.put(Constants().KEY_FCM_TOKEN, FieldValue.delete())
        documentReference.update(updates)
            .addOnSuccessListener {
                preferenceManager.clear()
                startActivity(Intent(applicationContext, SignIn::class.java))
                finish()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PROFILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val updatedName = data?.getStringExtra(Constants().KEY_NAME)
            val updatedImage = data?.getStringExtra(Constants().KEY_IMAGE)

            if (updatedName != null) {
                binding.Tname.text = updatedName
            }

            if (!updatedImage.isNullOrEmpty()) {
                val decodedBytes = Base64.getDecoder().decode(updatedImage)
                val bitmap: Bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                binding.imageProfile.setImageBitmap(bitmap)
            }
        }
    }

}