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
import com.simats.cakeordering.adapter.ChatAdapter
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
 * Chat screen for Baker to message Customer
 * Connected to database via API
 */
class ChatActivity : AppCompatActivity() {

    private lateinit var rvMessages: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: ImageView
    private lateinit var btnUploadImage: ImageView
    private lateinit var btnBack: ImageView
    private lateinit var txtCustomerName: TextView
    private lateinit var chatAdapter: ChatAdapter

    private var bakerId: Int = 0
    private var userId: Int = 0
    private var customerName: String = ""
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
        setContentView(R.layout.activity_chat)

        // Get data from intent
        bakerId = intent.getIntExtra("baker_id", 0)
        userId = intent.getIntExtra("user_id", 0)
        customerName = intent.getStringExtra("customer_name") ?: "Customer"

        // Initialize views
        rvMessages = findViewById(R.id.rvMessages)
        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)
        btnUploadImage = findViewById(R.id.btnUploadImage)
        btnBack = findViewById(R.id.btnBack)
        txtCustomerName = findViewById(R.id.txtCustomerName)

        txtCustomerName.text = customerName

        // Back button
        btnBack.setOnClickListener { finish() }

        // Setup RecyclerView
        chatAdapter = ChatAdapter(mutableListOf())
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
        ApiClient.api.getChatMessages(bakerId, userId)
            .enqueue(object : Callback<ChatMessagesResponse> {
                override fun onResponse(
                    call: Call<ChatMessagesResponse>,
                    response: Response<ChatMessagesResponse>
                ) {
                    if (response.isSuccessful && response.body()?.status == "success") {
                        val messages = response.body()?.messages ?: emptyList()
                        chatAdapter.updateData(messages)
                        if (messages.isNotEmpty()) {
                            rvMessages.scrollToPosition(messages.size - 1)
                        }
                    }
                }

                override fun onFailure(call: Call<ChatMessagesResponse>, t: Throwable) {
                    Log.e("ChatActivity", "Failed to load messages: ${t.message}")
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
                            Toast.makeText(this@ChatActivity, "Failed to upload image", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<ImageUploadResponse>, t: Throwable) {
                        tempFile.delete() // Clean up temp file
                        btnSend.isEnabled = true
                        Toast.makeText(this@ChatActivity, "Image upload failed: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        } catch (e: Exception) {
            btnSend.isEnabled = true
            Toast.makeText(this, "Error processing image: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendMessageToApi(message: String?, imageUrl: String?) {
        Log.d("ChatActivity", "Sending message - bakerId: $bakerId, userId: $userId, message: $message, imageUrl: $imageUrl")
        
        ApiClient.api.sendChatMessage(
            bakerId = bakerId,
            userId = userId,
            senderType = "baker",
            message = message,
            imageUrl = imageUrl
        ).enqueue(object : Callback<BasicResponse> {
            override fun onResponse(call: Call<BasicResponse>, response: Response<BasicResponse>) {
                btnSend.isEnabled = true
                
                Log.d("ChatActivity", "Send response code: ${response.code()}")
                Log.d("ChatActivity", "Send response body: ${response.body()}")
                
                if (response.isSuccessful && response.body()?.status == "success") {
                    // Clear input
                    etMessage.text.clear()
                    selectedImageUri = null
                    
                    Toast.makeText(this@ChatActivity, "Message sent!", Toast.LENGTH_SHORT).show()
                    
                    // Reload messages to show new message
                    loadMessages()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("ChatActivity", "Send failed - errorBody: $errorBody")
                    Toast.makeText(this@ChatActivity, "Failed to send message", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                btnSend.isEnabled = true
                Log.e("ChatActivity", "Send network error", t)
                Toast.makeText(this@ChatActivity, "Send failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
