package dev.panwar.kurentogroupvideocalling

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import dev.panwar.kurentogroupvideocalling.databinding.ActivityMainBinding
import dev.panwar.kurentogroupvideocalling.models.Message
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var socketManager: SocketManager
    private lateinit var binding: ActivityMainBinding
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
                    val message = Message(
                        event = "joinRoom",
                        userName = userName,
                        roomName = roomName
                    )
                    socketManager.emitMessage(message)
                }
            }
        }
    }

    override fun onDestroy() {
        socketManager.disconnectSocket()
        super.onDestroy()
    }
}
