### CDBv4 Android Doorbell App

Welcome to the **Cat Doorbell Version 4 (CDBv4)** application repository. This application transforms decommissioned Pixel phones into a smart cat doorbell system, monitoring and alerting when your cat wants to come inside.

### Overview

The CDBv4 app leverages sound and image recognition to detect when a cat is meowing outside your door and sends an alert to let you know your cat is waiting. The app runs continuously in a kiosk-like mode, ensuring it performs its duties reliably and without interruption.

### Key Functionalities

1. **Sound Detection**:
    - The device passively listens for a cat meowing.
    - Upon detecting a meow, it activates the camera.

2. **Visual Verification**:
    - If it is dark, the flashlight will be turned on to aid the camera.
    - The camera remains active for 45 seconds to identify a cat.
    - If no cat is identified, the Doorbell returns to passive listening, and the flashlight is turned off if it was on.
    - If a cat is identified within the 45-second window, a text message alert is sent.

3. **Alert System**:
    - Text message alerts are triggered via an HTTP request to an AWS API Gateway REST API.
    - To prevent repeated alerts, the system pauses for 2 minutes before resuming monitoring.
    - If it is dark, the flashlight remains on until the end of this 2-minute pause.

### Technical Details

- **Sound and Image Processing**: Uses TensorFlow models for recognizing cat sounds and appearances.
- **Alert Mechanism**: Sends text messages through HTTP requests to an AWS API Gateway REST API.
- **Flashlight Control**: Manages the flashlight to assist in low-light conditions.
- **Application Architecture**: Written in Kotlin, designed for single-purpose use on decommissioned Pixel phones.

### Requirements

- Android Studio Jellyfish (2023.3.1 Patch 1)
- Kotlin programming language
- TensorFlow for machine learning models
- AWS API Gateway for sending alerts
- Pixel 3XL (Android 12) and Pixel 4XL (Android 13) phones with root access

### Setup Instructions

1. **Clone the Repository**:
    ```sh
    git clone https://github.com/yourusername/CDBv4-android-doorbell-app.git
    cd CDBv4-android-doorbell-app
    ```

2. **Open in Android Studio**:
    - Open Android Studio.
    - Select "Open an existing project" and navigate to the cloned repository.

3. **Build the Project**:
    - Ensure you have the necessary SDKs installed (Android SDK 31 and 33).
    - Sync the project with Gradle files.
    - Build the project to ensure all dependencies are resolved.

4. **Deploy to Device**:
    - Connect your Pixel device via USB.
    - Ensure the device is in developer mode and USB debugging is enabled.
    - Deploy the application to the device from Android Studio.

5. **Run the Shell Scripts**:
    - Navigate to the `shell` directory.
    - Run the necessary setup scripts:
      ```sh
      ./factory-reset.sh
      ./set-owner.sh
      ./sysinfo.sh
      ./startup/99-disable-mac-randomization.sh
      ./startup/enable_wifi_debug.sh
      ```

### State Machine

The state machine manages the different states and transitions for the CDBv4 application, ensuring efficient operation and accurate detection. The main states include:

- **INIT**: Initial state when the state machine starts.
- **LISTEN**: Passively listens for a cat meowing.
- **LOOK**: Activates the camera to verify the presence of a cat.
- **RING**: Sends a notification if a cat is detected.

### Directory Structure

```plaintext
CDBv4-android-doorbell-app
├── app
│   ├── src
│   │   ├── main
│   │   │   ├── java
│   │   │   ├── res
│   │   │   └── AndroidManifest.xml
├── gradle
├── shell
│   ├── factory-reset.sh
│   ├── set-owner.sh
│   ├── sysinfo.sh
│   └── startup
│       ├── 99-disable-mac-randomization.sh
│       └── enable_wifi_debug.sh
├── build.gradle.kts
├── gradle.properties
├── gradlew
├── gradlew.bat
├── settings.gradle.kts
└── README.md
```

### Contribution

Feel free to fork this repository and submit pull requests. For major changes, please open an issue first to discuss what you would like to change.

### License

This project is licensed under the MIT License.
