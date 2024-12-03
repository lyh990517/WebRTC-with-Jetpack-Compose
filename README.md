# Overview  
This project uses a custom SDK that simplifies WebRTC integration to deliver seamless communication capabilities. It includes **video call**, real-time **chat** via data channel, and **screen sharing** using MediaProjection.

| Feature          | Status |
|-------------------|--------|
| **Video Call**    | ✅      |
| **Chat**          | ✅      |
| **Screen Sharing**| ✅      |
| **File Transfer** | ❌      |

# Sample App Preview
<img width="330" alt="스크린샷 2024-11-23 오전 12 52 16" src="https://github.com/user-attachments/assets/283fa7b7-8dfe-4bf3-8b63-e047ec510615">
<img width="330" alt="스크린샷 2024-11-23 오전 12 52 28" src="https://github.com/user-attachments/assets/38fc10da-a4ed-480a-b778-05edca8aaef7">
<img width="330" alt="스크린샷 2024-11-23 오전 12 53 17" src="https://github.com/user-attachments/assets/3f560402-1103-4fb9-990d-39c8e670fb7a">
<img width="330" alt="스크린샷 2024-11-23 오전 12 57 31" src="https://github.com/user-attachments/assets/daa9b2a8-8247-442b-9800-11567626b138">
<img width="330" alt="스크린샷 2024-11-23 오전 12 54 22" src="https://github.com/user-attachments/assets/87c0a4e3-3691-41a3-8700-4b190e0e7fdb">
<img width="330" alt="스크린샷 2024-11-23 오전 12 58 16" src="https://github.com/user-attachments/assets/0f8c5946-2442-4254-b9f4-a8ec79db5ffc">
<img width="330" alt="스크린샷 2024-11-23 오전 12 58 16" src="https://github.com/user-attachments/assets/1fa96fc2-2095-461f-b7cb-c1b8c6d504ea">
![KakaoTalk_20241203_233520440](https://github.com/user-attachments/assets/283fa7b7-8dfe-4bf3-8b63-e047ec510615)

# How To Start Sample
1. **Create a Firebase Project**:
   - Go to the [Firebase Console](https://console.firebase.google.com/).
   - Click on **Add project** and follow the steps to create your Firebase project.

2. **Create Firestore Database**:
   - In the Firebase project overview, navigate to **Build** > **Firestore Database**.
   - Click **Create database** and follow the prompts to set up Firestore in either test or production mode.

3. **Download `google-services.json`**:
   - Go to **Project settings** (click on the gear icon).
   - Under **Your apps**, select your Android app and download the `google-services.json` configuration file.

4. **Place `google-services.json` in Your Project**:
   - Copy the downloaded `google-services.json` file.
   - Place it in the `app` directory of your project.
   - 
   <img width="352" alt="스크린샷 2024-11-23 오전 12 51 09" src="https://github.com/user-attachments/assets/8842baf5-a4fc-4bbb-baae-02be8701e6f6">

5. **Creating a Room**

   - Launch the app and select the option to "connect" or "connect with projection"
   - A unique number or word will be assigned to the created room.

6. **Joining a Room**

   - To join a room created by another user, enter the assigned number or word for that room and click the "connect" or "connect with projection".
   - When both users connect to the same room by using the same number or word, the video call will begin.

With these simple steps, you can set up and enjoy video calls. Share the room number or word with other users to start a call together.

# WebRTC Sdk Interface
``` kotlin
interface WebRtcClient {
    fun connect(roomID: String)

    fun getEvent(): Flow<WebRtcEvent>

    suspend fun getRoomList(): Flow<List<String>?>

    fun sendMessage(message: String)

    fun sendInputEvent()

    fun getMessages(): Flow<Message>

    fun disconnect()

    fun toggleVoice()

    fun toggleVideo()

    fun getLocalSurface(): SurfaceViewRenderer

    fun getRemoteSurface(): SurfaceViewRenderer
}
```

# WebRTC Sdk Usage

### in Video Call
``` kotlin
@Composable
fun VideoCallScreen() {
    val context = LocalContext.current
    val webrtcClient = remember { WebRtcClientFactory.create(context) } // 1. create client

    AndroidView(
        factory = {
            webrtcClient.getRemoteSurface() // 2. Initialize the UI to receive the other person's video
        }
    )
    AndroidView(
        factory = {
            webrtcClient.getLocalSurface() // 3. Initialize the UI to receive my video
        }
    )
    Button(
        onClick = {
            webrtcClient.connect("input roomId") // 4. try to connect
        },
    ) {
        Text("Connect")
    }
}
```

### in Screen Share
``` kotlin
@Composable
fun MediaProjectionScreen() {
    val context = LocalContext.current
    var webrtcClient by remember { mutableStateOf<WebRtcClient?>(null) }
    val mediaProjectionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.let { intent ->
                    WebRtcCaptureService.startService(context, intent) // 2. start capture service
                }
            }
        }
    )

    LaunchedEffect(Unit) {
        val mediaProjectionManager =
            context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val captureIntent = mediaProjectionManager.createScreenCaptureIntent()

        mediaProjectionLauncher.launch(captureIntent) // 1. get media projection

        WebRtcCaptureService.getClient().collect { client -> webrtcClient = client } // 3. after the process of getting the media projection, initialize your client
    }

    webrtcClient?.getRemoteSurface()?.let { remote ->
        AndroidView(
            factory = { remote }
        )
    }
    webrtcClient?.getLocalSurface()?.let { local ->
        AndroidView(
            factory = { local }
        )
    }
    Button(
        onClick = {
            webrtcClient?.connect("input roomId") // 4. try to connect
        },
    ) {
        Text("Connect")
    }
}
```

# Sdk Architecture
![sdk arch](https://github.com/user-attachments/assets/c6a66737-5760-45fc-bb6a-f963bfad5601)

# Sample App Modularization
![12412t5r3t1](https://github.com/user-attachments/assets/8ab7f77f-245d-4b0f-ba90-ff1efdd9c585)
