# Overview
This project is a video call application built using WebRTC technology. With this application, you can make video calls on both web browsers and mobile devices.

# Getting Started
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
   - Place it in the `core/webrtc/impl` directory of your project.
  
   ![image](https://github.com/user-attachments/assets/88bf386f-82b7-4694-b0d5-9f8fc982381a)

5. **Creating a Room**

   - Launch the app and select the option to "Create a New Room" or a similar choice.
   - A unique number or word will be assigned to the created room.

6. **Joining a Room**

   - To join a room created by another user, enter the assigned number or word for that room and click the "Join" button.
   - When both users join the same room by using the same number or word, the video call will begin.

With these simple steps, you can set up and enjoy video calls. Share the room number or word with other users to start a call together.

# Modularization
![module](https://github.com/user-attachments/assets/17506611-d25c-40ef-879b-15639fb4d633)


# App Preview

<img src="https://github.com/lyh990517/WebRTC/assets/45873564/099c66ac-41a4-448f-8762-9ca9d6367218" width="400" height="800">
<img src="https://github.com/lyh990517/WebRTC/assets/45873564/896427fd-d097-4f14-bbc0-bded0b245fa0" width="400" height="800">


