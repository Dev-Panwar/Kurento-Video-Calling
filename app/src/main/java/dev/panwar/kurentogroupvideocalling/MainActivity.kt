package dev.panwar.kurentogroupvideocalling

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import dev.panwar.kurentogroupvideocalling.databinding.ActivityMainBinding
import dev.panwar.kurentogroupvideocalling.models.Message
import dev.panwar.kurentogroupvideocalling.models.User
import org.json.JSONArray
import org.json.JSONObject
import org.webrtc.IceCandidate
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.SessionDescription


class MainActivity : AppCompatActivity() {

    private lateinit var socketManager: SocketManager
    private lateinit var binding: ActivityMainBinding
    private val participants = mutableMapOf<String, User>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        socketManager = SocketManager()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



        binding.apply {
            btnJoinRoom.setOnClickListener {
                val userName = etUsername.text.toString()
                val roomName = etRoomName.text.toString()

                if (userName.isEmpty() || roomName.isEmpty()) {
                    Toast.makeText(
                        this@MainActivity,
                        "Room and Name are required",
                        Toast.LENGTH_SHORT
                    ).show()
                }else{
                    val message = JSONObject()
                    message.put("event", "joinRoom")
                    message.put("userName", userName)
                    message.put("roomName", roomName)

                    socketManager.emitMessage(message)

                    PeerConnectionFactory.initialize(
                        PeerConnectionFactory.InitializationOptions.builder(this@MainActivity)
                            .createInitializationOptions()
                    )

                }
            }
        }
    }



    override fun onDestroy() {
        socketManager.disconnectSocket()
        super.onDestroy()
    }


}
