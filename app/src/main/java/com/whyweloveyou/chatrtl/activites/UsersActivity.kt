package com.whyweloveyou.chatrtl.activites

import PreferenceManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.whyweloveyou.chatrtl.adapters.UsersAdapter
import com.whyweloveyou.chatrtl.databinding.ActivityUsersBinding
import com.whyweloveyou.chatrtl.listeners.UserListener
import com.whyweloveyou.chatrtl.models.User
import com.whyweloveyou.chatrtl.utilities.Constants
import java.io.Serializable

class UsersActivity : AppCompatActivity(), UserListener, Serializable {

    private lateinit var binding: ActivityUsersBinding
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUsersBinding.inflate(layoutInflater)
        preferenceManager = PreferenceManager(applicationContext)
        setContentView(binding.root)
        getUsers()
        onListener()
    }

    private fun onListener() {
        binding.ImgBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun getUsers() {
        loading(true)
        val database: FirebaseFirestore = FirebaseFirestore.getInstance()
        database.collection(Constants().KEY_COLLECTION_USERS)
            .get()
            .addOnCompleteListener {
                loading(false)
                val currentUserId: String =
                    preferenceManager.getString(Constants().KEY_USER_ID).toString()
                if (it.isSuccessful && it.result != null) {
                    val users: ArrayList<User> = ArrayList()
                    for (queryDocumentSnapshot: QueryDocumentSnapshot in it.result) {
                        if (currentUserId.equals(queryDocumentSnapshot.id)) {
                            continue
                        }
                        val user: User = User()
                        user.name = queryDocumentSnapshot.getString(Constants().KEY_NAME).toString()
                        user.email =
                            queryDocumentSnapshot.getString(Constants().KEY_EMAIL).toString()
                        user.image =
                            queryDocumentSnapshot.getString(Constants().KEY_IMAGE).toString()
                        user.token =
                            queryDocumentSnapshot.getString(Constants().KEY_FCM_TOKEN).toString()
                        user.id = queryDocumentSnapshot.id
                        users.add(user)
                    }
                    if (users.size > 0) {
                        val usersAdapter: UsersAdapter = UsersAdapter(users, this)
                        binding.userRecyclerView.adapter = usersAdapter
                        binding.userRecyclerView.visibility = View.VISIBLE
                    } else {
                        showErrorM()
                    }
                } else {
                    showErrorM()
                }
            }
    }

    private fun showErrorM() {
        binding.textErrorMsg.text = String.format("%s", "No User Avaible")
    }

    private fun loading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressB.visibility = View.VISIBLE
        } else {
            binding.progressB.visibility = View.INVISIBLE
        }
    }

    override fun onUserClicekd(user: User) {
        val intent: Intent = Intent(applicationContext, ChatAct::class.java)
        intent.putExtra(Constants().KEY_USER, user)
        startActivity(intent)
        finish()
    }
}