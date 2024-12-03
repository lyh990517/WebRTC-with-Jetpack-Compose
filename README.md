# Overview
This project is a video call application built using WebRTC technology. With this application, you can make video calls on both web browsers and mobile devices.

# Sample App Preview
<img width="330" alt="스크린샷 2024-11-23 오전 12 52 16" src="https://github.com/user-attachments/assets/dc7416ca-a3b8-4e84-8332-616ffb9c1daa">
<img width="330" alt="스크린샷 2024-11-23 오전 12 52 28" src="https://github.com/user-attachments/assets/38fc10da-a4ed-480a-b778-05edca8aaef7">
<img width="330" alt="스크린샷 2024-11-23 오전 12 53 17" src="https://github.com/user-attachments/assets/3f560402-1103-4fb9-990d-39c8e670fb7a">
<img width="330" alt="스크린샷 2024-11-23 오전 12 57 31" src="https://github.com/user-attachments/assets/daa9b2a8-8247-442b-9800-11567626b138">
<img width="330" alt="스크린샷 2024-11-23 오전 12 54 22" src="https://github.com/user-attachments/assets/87c0a4e3-3691-41a3-8700-4b190e0e7fdb">
<img width="330" alt="스크린샷 2024-11-23 오전 12 58 16" src="https://github.com/user-attachments/assets/0f8c5946-2442-4254-b9f4-a8ec79db5ffc">
<img width="330" alt="스크린샷 2024-11-23 오전 12 58 16" src="https://github.com/user-attachments/assets/1fa96fc2-2095-461f-b7cb-c1b8c6d504ea">

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

   - Launch the app and select the option to "Create a New Room" or a similar choice.
   - A unique number or word will be assigned to the created room.

6. **Joining a Room**

   - To join a room created by another user, enter the assigned number or word for that room and click the "Join" button.
   - When both users join the same room by using the same number or word, the video call will begin.

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
``` kotlin
@Composable
fun TestScreen() {
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

# Sdk Architecture
![sdk arch](https://github.com/user-attachments/assets/c6a66737-5760-45fc-bb6a-f963bfad5601)

# Sample App Modularization
![12412t5r3t1](https://github.com/user-attachments/assets/8ab7f77f-245d-4b0f-ba90-ff1efdd9c585)
