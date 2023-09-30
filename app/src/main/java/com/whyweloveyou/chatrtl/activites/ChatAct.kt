package com.whyweloveyou.chatrtl.activites

import PreferenceManager
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.whyweloveyou.chatrtl.adapters.ChatAdapter
import com.whyweloveyou.chatrtl.databinding.ActivityChatBinding
import com.whyweloveyou.chatrtl.models.ChatMessage
import com.whyweloveyou.chatrtl.models.User
import com.whyweloveyou.chatrtl.utilities.Constants
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.properties.Delegates

class ChatAct : AppCompatActivity() {

    private lateinit var receiverUser: User
    private lateinit var binding: ActivityChatBinding
    private lateinit var chatMessages: MutableList<ChatMessage>
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var database: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadReceiverDetails()
        onListener()
        init()
        listenMessages()
    }

    private fun init() {
        preferenceManager = PreferenceManager(applicationContext)
        chatMessages = ArrayList()
        chatAdapter = ChatAdapter(
            chatMessages,
            getBitMapFromEncodedString(receiverUser.image),
            preferenceManager.getString(Constants().KEY_USER_ID).toString()
        )
        binding.chatRecycleView.adapter = chatAdapter
        database = FirebaseFirestore.getInstance()
    }

    private fun sendMessage() {
        val message: HashMap<String, Any> = HashMap<String, Any>()
        message.put(
            Constants().KEY_SENDER_ID,
            preferenceManager.getString(Constants().KEY_USER_ID).toString()
        )
        message.put(Constants().KEY_RECEIVER_ID, receiverUser.id)
        message.put(Constants().KEY_MESSAGE, binding.inputMessage.text.toString())
        message.put(Constants().KEY_TIMESTAMP, Date())
        database.collection(Constants().KEY_COLLECTION_CHAT).add(message)
        binding.inputMessage.text = null
    }

    private fun listenMessages() {
        database.collection(Constants().KEY_COLLECTION_CHAT)
            .whereEqualTo(
                Constants().KEY_SENDER_ID,
                preferenceManager.getString(Constants().KEY_USER_ID).toString()
            )
            .whereEqualTo(Constants().KEY_RECEIVER_ID, receiverUser.id)
            .addSnapshotListener(eventListener())

        database.collection(Constants().KEY_COLLECTION_CHAT)
            .whereEqualTo(Constants().KEY_SENDER_ID, receiverUser.id)
            .whereEqualTo(
                Constants().KEY_RECEIVER_ID,
                preferenceManager.getString(Constants().KEY_USER_ID).toString()
            )
            .addSnapshotListener(eventListener())
    }

    private fun eventListener(): EventListener<QuerySnapshot> {
        return EventListener<QuerySnapshot> { value, error ->
            if (error != null) {
                return@EventListener
            }
            if (value != null) {
                val count: Int = chatMessages.size
                for (documentChange: DocumentChange in value.documentChanges) {
                    if (documentChange.type == DocumentChange.Type.ADDED) {
                        val chatMessage = ChatMessage()
                        chatMessage.senderId =
                            documentChange.document.getString(Constants().KEY_SENDER_ID).toString()
                        chatMessage.receiverId =
                            documentChange.document.getString(Constants().KEY_RECEIVER_ID)
                                .toString()
                        chatMessage.message =
                            documentChange.document.getString(Constants().KEY_MESSAGE).toString()
                        chatMessage.dateTime =
                            getReadableDateTime(documentChange.document.getDate(Constants().KEY_TIMESTAMP)!!)
                        chatMessage.dateObject =
                            documentChange.document.getDate(Constants().KEY_TIMESTAMP)!!
                        chatMessages.add(chatMessage)
                    }
                }

                chatMessages.sortBy { it.dateObject }
                if (count == 0) {
                    chatAdapter.notifyDataSetChanged()
                } else {
                    chatAdapter.notifyItemRangeInserted(chatMessages.size, chatMessages.size)
                    binding.chatRecycleView.smoothScrollToPosition(chatMessages.size - 1)
                }
                binding.chatRecycleView.visibility = View.VISIBLE
            }
            binding.ProgressB.visibility = View.GONE
        }
    }


    private fun getBitMapFromEncodedString(encodedImage: String): Bitmap {
        val decodedBytes = Base64.decode(encodedImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }

    private fun loadReceiverDetails() {
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            receiverUser = intent.getSerializableExtra(Constants().KEY_USER) as User
            binding.textName.text = receiverUser.name
        } else {
            @Suppress("DEPRECATION")
            receiverUser = intent.getSerializableExtra(Constants().KEY_USER) as User
            binding.textName.text = receiverUser.name
        }
    }

    private fun onListener() {
        binding.imgBack.setOnClickListener {
            val pindahhal1 = Intent(applicationContext, MainActivity::class.java)
            startActivity(pindahhal1)
            finish()
        }

        binding.imgInfo.setOnClickListener {
            val call = Intent(Intent.ACTION_DIAL, Uri.parse("tel: 081914981500"))
            startActivity(call)
        }

        binding.addingImage.setOnClickListener {
            Toast.makeText(applicationContext, "Belum jadi", Toast.LENGTH_SHORT).show()
        }

        binding.sendCommand.setOnClickListener {
            if (binding.inputMessage.text.isBlank() || binding.inputMessage.text.isNullOrEmpty() || binding.inputMessage.text.equals(" ")) {
                binding.inputMessage.hint = "Message cannot be empty"
            } else {
                binding.inputMessage.hint = "Type a message"
                sendMessage()
            }
        }
    }

    private fun getReadableDateTime(date: Date): String {
        return SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date)
    }
}