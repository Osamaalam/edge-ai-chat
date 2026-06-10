<img width="1823" height="855" alt="EdgeAI" src="https://github.com/user-attachments/assets/210549c6-716f-489f-9ba6-123d9a407428" />
# Edge AI Chat - Local GGUF LLM Android App

Edge AI Chat is a state-of-the-art, fully offline Android application that lets you run Large Language Models (LLMs) and Multimodal Vision-Language Models (VLMs) completely on-device. 

By utilizing **llama.cpp** native JNI bindings compiled via CMake, the app performs high-performance local inference on your phone without any internet connection, ensuring 100% privacy, zero latency, and no API subscription costs.

---

## 🚀 Key Features

1. **Fully Local & Offline**: Run inference, format responses, and process images entirely on-device. No APIs, cloud services, or internet access required.
2. **High-Performance Multimodal Support**: Automatically detects and loads text-only GGUF models or combined LLM + Projector GGUF models (e.g., Qwen-3.5 Vision, LLaVA) using the modern unified **libmtmd** (multimodal) engine of `llama.cpp`.
3. **Advanced Performance Tracking**: Displays live tokens-per-second, prompt evaluation time, total generation time, and physical RAM / storage metrics.
4. **Glitch-Free Memory Streaming**: Incorporates an in-memory streaming state architecture that updates tokens 100% in memory during generation. This bypasses slow database disk I/O, eliminating chat flickering, lag, and UI stutter completely.
5. **Active "AI Thinking" Control**: Toggle model reasoning steps (like DeepSeek `<think>` blocks) on-the-fly inside the settings panel. Disabling thinking actively instructs the model's system prompt to skip reasoning steps, saving battery and drastically speeding up response times.
6. **Local Conversation History**: Persists chat sessions and messages locally using a Room SQLite Database. Includes support for attaching images to conversation blocks.
7. **Dynamic Inference Settings**: Configure context length, threads count, GPU offloading, temperature, Top-P, Top-K, and repetition penalty on-the-fly.
8. **Beautiful Material 3 Design**: Features a modern ChatGPT-style UI with automatic light/dark mode adaptation, responsive layouts, side navigation drawers, and fluid animations.

---

## 📂 Project Architecture

The app is built following **Clean Architecture** and **MVVM (Model-View-ViewModel)** guidelines:

```
app/
├── src/main/
│   ├── cpp/
│   │   ├── CMakeLists.txt        # Native compiler script linking llama.cpp core and libmtmd
│   │   └── llama-jni.cpp         # High-performance C++ JNI bridge mapping llama.cpp, JNI, and MTMD
│   ├── java/com/edgeai/chat/
│   │   ├── database/             # Room SQLite DB classes (Sessions, Messages, DAOs)
│   │   ├── llama/                # JNI Kotlin Wrapper (LlamaModel.kt)
│   │   ├── repository/           # Data & Inference Repository (ChatRepository.kt)
│   │   ├── settings/             # SharedPreferences manager (AppSettings.kt, SettingsRepository.kt)
│   │   ├── ui/
│   │   │   ├── theme/            # Material 3 typography and dark/light color schemes
│   │   │   ├── screens/          # Jetpack Compose Screens (Main, ModelManager, Settings)
│   │   │   └── Navigation.kt     # App NavHost configuration
│   │   ├── utils/                # File and system metric utilities (FileUtils.kt)
│   │   └── MainActivity.kt       # Launcher activity, requests camera/storage permissions
│   └── AndroidManifest.xml       # App declarations, permissions & theme configurations
```

---

## 🛠️ Step-by-Step Build Instructions

Follow these steps to compile and build the Android APK yourself:

### Prerequisites
* **Android Studio** (Koala / Ladybug or newer recommended)
* **Android NDK** and **CMake** installed via Android Studio SDK Manager:
  1. Open Android Studio, go to **Tools > SDK Manager** (or Settings > Languages & Frameworks > Android SDK).
  2. Select the **SDK Tools** tab.
  3. Check **NDK (Side by side)** and **CMake**.
  4. Click **Apply** and wait for installation to complete.

### Step 1: Open Project in Android Studio
1. Launch Android Studio.
2. Click **Open** and select the folder `d:\AI\edgeai` (the root directory containing `settings.gradle`).
3. Wait for Gradle to download dependencies and sync.

### Step 2: Download llama.cpp Source (Automated)
The provided `CMakeLists.txt` is configured with CMake's native `FetchContent` utility. When you run your first build, Gradle/CMake will **automatically clone** the modern tag of the `llama.cpp` repository from GitHub and compile the C++ binaries directly for the active device's ABI (e.g., `arm64-v8a` or `x86_64`). No manual pre-compilation is required!

### Step 3: Run / Build APK
1. Connect an Android phone via USB with **Developer Options** and **USB Debugging** enabled (or start an Android Emulator with `arm64-v8a` or `x86_64` support).
2. Click the **Run 'app'** button (Green Play button) in the top toolbar to build and deploy.
3. To generate a standalone build:
   * Go to **Build > Build Bundle(s) / APK(s) > Build APK(s)** in the top menu.
   * Once finished, a popup will appear. Click **Locate** to retrieve your generated debug/release APK (`app-debug.apk`).

---

## 💾 Model Setup & Loading Guide

Your workspace contains:
* **LLM Model**: `Qwen3.5-0.8B-Q8_0.gguf`
* **CLIP Vision Projector**: `mmproj-F32.gguf`

To transfer and run these models inside the application:

### Step 1: Transfer Models to Device Storage
1. Connect your Android phone to your computer.
2. Transfer the folder `Qwen3.5-0.8B-GGUF` into your phone's **Download** folder.
   * Path on device: `/storage/emulated/0/Download/Qwen3.5-0.8B-GGUF/`
3. Alternatively, you can copy individual `.gguf` files to any local folder on your phone.

### Step 2: Select and Load in App
1. Open **Edge AI Chat** on your phone.
2. Tap the **Folder icon** in the top right corner to open the **Model Manager**.
3. Tap **Rescan Files** (Refresh icon in the top right) to scan storage recursively.
4. Select `Qwen3.5-0.8B-Q8_0.gguf` as your main LLM model.
5. Select `mmproj-F32.gguf` as your vision projector model (optional).
6. Tap **Apply & Load Model** to initialize the model. A status progress bar will track loading.
7. Return to the Chat screen and enjoy private, blazing-fast edge inference!

---

## ⚡ Inference Configuration
Tap the **Settings icon** (Gear) in the top right to customize generation:
* **Enable AI Thinking**: Toggle reasoning processes on-the-fly. If disabled, instructs the system prompt to skip `<think>` outputs, speeding up responses.
* **Threads Count**: Set to your CPU's physical core count (e.g., 4 or 6) for maximum speed.
* **GPU Offload**: Offload layers to Vulkan/OpenCL (where hardware drivers support it).
* **Temperature / Top-P**: Balance creativity vs deterministic reasoning.
* **Context length**: Restrict RAM footprint by sliding between 512 and 8192 tokens.
