package com.simats.cakeordering

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.simats.cakeordering.adapter.CustomerChatAdapter
import com.simats.cakeordering.api.ApiClient
import com.simats.cakeordering.model.BasicResponse
import com.simats.cakeordering.model.ChatMessage
import com.simats.cakeordering.model.ChatMessagesResponse
import com.simats.cakeordering.model.ImageUploadResponse
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

/**
 * Chat screen for Customer to message Baker
 * Connected to database via API
 */
class CustomerChatActivity : AppCompatActivity() {

    private lateinit var rvMessages: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: ImageView
    private lateinit var btnUploadImage: ImageView
    private lateinit var btnBack: ImageView
    private lateinit var txtBakerName: TextView
    private lateinit var chatAdapter: CustomerChatAdapter

    private var bakerId: Int = 0
    private var userId: Int = 0
    private var bakerName: String = ""
    private var selectedImageUri: Uri? = null

    // Handler for periodic refresh
    private val refreshHandler = Handler(Looper.getMainLooper())
    private val refreshInterval = 5000L // Refresh every 5 seconds
    private val refreshRunnable = object : Runnable {
        override fun run() {
            loadMessages()
            refreshHandler.postDelayed(this, refreshInterval)
        }
    }

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            Toast.makeText(this, "Image selected. Click send to share.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_chat)

        // Get data from intent
        bakerId = intent.getIntExtra("baker_id", 0)
        userId = intent.getIntExtra("user_id", 0)
        bakerName = intent.getStringExtra("baker_name") ?: "Baker"

        // Initialize views
        rvMessages = findViewById(R.id.rvMessages)
        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)
        btnUploadImage = findViewById(R.id.btnUploadImage)
        btnBack = findViewById(R.id.btnBack)
        txtBakerName = findViewById(R.id.txtBakerName)

        txtBakerName.text = bakerName

        // Back button
        btnBack.setOnClickListener { finish() }

        // Setup RecyclerView with customer view (customer messages on right)
        chatAdapter = CustomerChatAdapter(mutableListOf())
        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        rvMessages.layoutManager = layoutManager
        rvMessages.adapter = chatAdapter

        // Upload image
        btnUploadImage.setOnClickListener {
            pickImage.launch("image/*")
        }

        // Send message
        btnSend.setOnClickListener {
            sendMessage()
        }

        // Load messages from database
        loadMessages()
        
        // Start periodic refresh
        refreshHandler.postDelayed(refreshRunnable, refreshInterval)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Stop periodic refresh
        refreshHandler.removeCallbacks(refreshRunnable)
    }

    private fun loadMessages() {
        Log.d("CustomerChatActivity", "loadMessages called - bakerId: $bakerId, userId: $userId")
        
        ApiClient.api.getChatMessages(bakerId, userId)
            .enqueue(object : Callback<ChatMessagesResponse> {
                override fun onResponse(
                    call: Call<ChatMessagesResponse>,
                    response: Response<ChatMessagesResponse>
                ) {
                    Log.d("CustomerChatActivity", "loadMessages response code: ${response.code()}")
                    Log.d("CustomerChatActivity", "loadMessages response body: ${response.body()}")
                    Log.d("CustomerChatActivity", "loadMessages status: ${response.body()?.status}")
                    
                    if (response.isSuccessful && response.body()?.status == "success") {
                        val messages = response.body()?.messages ?: emptyList()
                        Log.d("CustomerChatActivity", "Messages count: ${messages.size}")
                        messages.forEach { msg ->
                            Log.d("CustomerChatActivity", "Message: id=${msg.message_id}, sender=${msg.sender_type}, text=${msg.message}")
                        }
                        chatAdapter.updateData(messages)
                        if (messages.isNotEmpty()) {
                            rvMessages.scrollToPosition(messages.size - 1)
                        }
                    } else {
                        Log.e("CustomerChatActivity", "loadMessages failed - not successful or wrong status")
                    }
                }

                override fun onFailure(call: Call<ChatMessagesResponse>, t: Throwable) {
                    Log.e("CustomerChatActivity", "Failed to load messages: ${t.message}", t)
                }
            })
    }

    private fun sendMessage() {
        val messageText = etMessage.text.toString().trim()
        
        if (messageText.isEmpty() && selectedImageUri == null) {
            Toast.makeText(this, "Please enter a message or select an image", Toast.LENGTH_SHORT).show()
            return
        }

        // Disable send button while sending
        btnSend.isEnabled = false

        if (selectedImageUri != null) {
            // First upload the image, then send message with image URL
            uploadImageAndSend(messageText)
        } else {
            // Send text message only
            sendMessageToApi(messageText, null)
        }
    }

    private fun uploadImageAndSend(messageText: String) {
        val uri = selectedImageUri ?: return

        try {
            // Convert Uri to File
            val inputStream = contentResolver.openInputStream(uri)
            val tempFile = File.createTempFile("chat_", ".jpg", cacheDir)
            val outputStream = FileOutputStream(tempFile)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()

            // Create multipart request (OkHttp 3.x style)
            val mediaType = MediaType.parse("image/*")
            val requestBody = RequestBody.create(mediaType, tempFile)
            val imagePart = MultipartBody.Part.createFormData("image", tempFile.name, requestBody)

            ApiClient.api.uploadChatImage(imagePart)
                .enqueue(object : Callback<ImageUploadResponse> {
                    override fun onResponse(
                        call: Call<ImageUploadResponse>,
                        response: Response<ImageUploadResponse>
                    ) {
                        tempFile.delete() // Clean up temp file
                        
                        if (response.isSuccessful && response.body()?.status == "success") {
                            val imageUrl = response.body()?.image_url
                            sendMessageToApi(
                                if (messageText.isEmpty()) null else messageText,
                                imageUrl
                            )
                        } else {
                            btnSend.isEnabled = true
                            Toast.makeText(this@CustomerChatActivity, "Failed to upload image", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<ImageUploadResponse>, t: Throwable) {
                        tempFile.delete() // Clean up temp file
                        btnSend.isEnabled = true
                        Toast.makeText(this@CustomerChatActivity, "Image upload failed: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        } catch (e: Exception) {
            btnSend.isEnabled = true
            Toast.makeText(this, "Error processing image: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendMessageToApi(message: String?, imageUrl: String?) {
        Log.d("CustomerChatActivity", "Sending message - bakerId: $bakerId, userId: $userId, message: $message, imageUrl: $imageUrl")
        
        ApiClient.api.sendChatMessage(
            bakerId = bakerId,
            userId = userId,
            senderType = "customer",
            message = message,
            imageUrl = imageUrl
        ).enqueue(object : Callback<BasicResponse> {
            override fun onResponse(call: Call<BasicResponse>, response: Response<BasicResponse>) {
                btnSend.isEnabled = true
                
                Log.d("CustomerChatActivity", "Send response code: ${response.code()}")
                Log.d("CustomerChatActivity", "Send response body: ${response.body()}")
                
                if (response.isSuccessful && response.body()?.status == "success") {
                    // Clear input
                    etMessage.text.clear()
                    selectedImageUri = null
                    
                    Toast.makeText(this@CustomerChatActivity, "Message sent!", Toast.LENGTH_SHORT).show()
                    
                    // Reload messages to show new message
                    loadMessages()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("CustomerChatActivity", "Send failed - errorBody: $errorBody")
                    Toast.makeText(this@CustomerChatActivity, "Failed to send message", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                btnSend.isEnabled = true
                Log.e("CustomerChatActivity", "Send network error", t)
                Toast.makeText(this@CustomerChatActivity, "Send failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
