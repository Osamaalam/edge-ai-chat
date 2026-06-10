package com.edgeai.chat.repository;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000r\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\r\n\u0002\u0010\u0006\n\u0002\b\u0006\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\f\u0018\u00002\u00020\u0001B\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006JF\u0010\u0014\u001a\u00020\b2\u0006\u0010\u0015\u001a\u00020\b2\u0006\u0010\u0016\u001a\u00020\f2\u0006\u0010\u0017\u001a\u00020\f2\n\b\u0002\u0010\u0018\u001a\u0004\u0018\u00010\f2\b\b\u0002\u0010\u0019\u001a\u00020\u001a2\b\b\u0002\u0010\u001b\u001a\u00020\bH\u0086@\u00a2\u0006\u0002\u0010\u001cJ\u0016\u0010\u001d\u001a\u00020\b2\u0006\u0010\u001e\u001a\u00020\fH\u0086@\u00a2\u0006\u0002\u0010\u001fJ\u0016\u0010 \u001a\u00020!2\u0006\u0010\u0015\u001a\u00020\bH\u0086@\u00a2\u0006\u0002\u0010\"J\u0016\u0010#\u001a\u00020!2\u0006\u0010\u0015\u001a\u00020\bH\u0086@\u00a2\u0006\u0002\u0010\"JT\u0010$\u001a\u00020%2\u0006\u0010\u0015\u001a\u00020\b2\u0006\u0010&\u001a\u00020\f2\b\u0010\'\u001a\u0004\u0018\u00010(2\u0006\u0010)\u001a\u00020*2\"\u0010+\u001a\u001e\b\u0001\u0012\u0004\u0012\u00020\f\u0012\n\u0012\b\u0012\u0004\u0012\u00020!0-\u0012\u0006\u0012\u0004\u0018\u00010\u00010,H\u0086@\u00a2\u0006\u0002\u0010.J\u001a\u0010/\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020%01002\u0006\u0010\u0015\u001a\u00020\bJ\u0012\u00102\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u0002030100J\u0006\u00104\u001a\u00020\fJ\u0006\u00105\u001a\u000206J&\u00107\u001a\u0002062\u0006\u00108\u001a\u00020\f2\u0006\u00109\u001a\u00020\f2\u0006\u0010)\u001a\u00020*H\u0086@\u00a2\u0006\u0002\u0010:J\u0006\u0010;\u001a\u00020!J\u0016\u0010<\u001a\u00020\b2\u0006\u0010=\u001a\u00020%H\u0086@\u00a2\u0006\u0002\u0010>J\u001e\u0010?\u001a\u00020\b2\u0006\u0010\u0015\u001a\u00020\b2\u0006\u0010@\u001a\u00020\fH\u0086@\u00a2\u0006\u0002\u0010AR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001e\u0010\t\u001a\u00020\b2\u0006\u0010\u0007\u001a\u00020\b@BX\u0086\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000bR\u001e\u0010\r\u001a\u00020\f2\u0006\u0010\u0007\u001a\u00020\f@BX\u0086\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000fR\u001e\u0010\u0010\u001a\u00020\f2\u0006\u0010\u0007\u001a\u00020\f@BX\u0086\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u000fR\u001e\u0010\u0012\u001a\u00020\f2\u0006\u0010\u0007\u001a\u00020\f@BX\u0086\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u000f\u00a8\u0006B"}, d2 = {"Lcom/edgeai/chat/repository/ChatRepository;", "", "chatDao", "Lcom/edgeai/chat/database/ChatDao;", "llamaModel", "Lcom/edgeai/chat/llama/LlamaModel;", "(Lcom/edgeai/chat/database/ChatDao;Lcom/edgeai/chat/llama/LlamaModel;)V", "<set-?>", "", "loadTimeMs", "getLoadTimeMs", "()J", "", "loadedMmprojPath", "getLoadedMmprojPath", "()Ljava/lang/String;", "loadedModelPath", "getLoadedModelPath", "modelInfo", "getModelInfo", "addMessage", "sessionId", "role", "content", "imagePath", "tokensPerSecond", "", "generationTimeMs", "(JLjava/lang/String;Ljava/lang/String;Ljava/lang/String;DJLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "createSession", "title", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteMessagesForSession", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteSession", "generateResponse", "Lcom/edgeai/chat/database/ChatMessage;", "prompt", "imageBitmap", "Landroid/graphics/Bitmap;", "settings", "Lcom/edgeai/chat/settings/AppSettings;", "onToken", "Lkotlin/Function2;", "Lkotlin/coroutines/Continuation;", "(JLjava/lang/String;Landroid/graphics/Bitmap;Lcom/edgeai/chat/settings/AppSettings;Lkotlin/jvm/functions/Function2;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getMessages", "Lkotlinx/coroutines/flow/Flow;", "", "getSessions", "Lcom/edgeai/chat/database/ChatSession;", "getSystemInfo", "isModelLoaded", "", "loadModel", "modelPath", "mmprojPath", "(Ljava/lang/String;Ljava/lang/String;Lcom/edgeai/chat/settings/AppSettings;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "stopGeneration", "updateMessage", "message", "(Lcom/edgeai/chat/database/ChatMessage;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateSessionTitle", "newTitle", "(JLjava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public final class ChatRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.edgeai.chat.database.ChatDao chatDao = null;
    @org.jetbrains.annotations.NotNull()
    private final com.edgeai.chat.llama.LlamaModel llamaModel = null;
    @org.jetbrains.annotations.NotNull()
    private java.lang.String loadedModelPath = "";
    @org.jetbrains.annotations.NotNull()
    private java.lang.String loadedMmprojPath = "";
    private long loadTimeMs = 0L;
    @org.jetbrains.annotations.NotNull()
    private java.lang.String modelInfo = "No Model Loaded";
    
    public ChatRepository(@org.jetbrains.annotations.NotNull()
    com.edgeai.chat.database.ChatDao chatDao, @org.jetbrains.annotations.NotNull()
    com.edgeai.chat.llama.LlamaModel llamaModel) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getLoadedModelPath() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getLoadedMmprojPath() {
        return null;
    }
    
    public final long getLoadTimeMs() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getModelInfo() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.edgeai.chat.database.ChatSession>> getSessions() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.edgeai.chat.database.ChatMessage>> getMessages(long sessionId) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object createSession(@org.jetbrains.annotations.NotNull()
    java.lang.String title, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object updateSessionTitle(long sessionId, @org.jetbrains.annotations.NotNull()
    java.lang.String newTitle, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object deleteSession(long sessionId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object addMessage(long sessionId, @org.jetbrains.annotations.NotNull()
    java.lang.String role, @org.jetbrains.annotations.NotNull()
    java.lang.String content, @org.jetbrains.annotations.Nullable()
    java.lang.String imagePath, double tokensPerSecond, long generationTimeMs, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object deleteMessagesForSession(long sessionId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object updateMessage(@org.jetbrains.annotations.NotNull()
    com.edgeai.chat.database.ChatMessage message, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion) {
        return null;
    }
    
    public final boolean isModelLoaded() {
        return false;
    }
    
    public final void stopGeneration() {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object loadModel(@org.jetbrains.annotations.NotNull()
    java.lang.String modelPath, @org.jetbrains.annotations.NotNull()
    java.lang.String mmprojPath, @org.jetbrains.annotations.NotNull()
    com.edgeai.chat.settings.AppSettings settings, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getSystemInfo() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object generateResponse(long sessionId, @org.jetbrains.annotations.NotNull()
    java.lang.String prompt, @org.jetbrains.annotations.Nullable()
    android.graphics.Bitmap imageBitmap, @org.jetbrains.annotations.NotNull()
    com.edgeai.chat.settings.AppSettings settings, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.String, ? super kotlin.coroutines.Continuation<? super kotlin.Unit>, ? extends java.lang.Object> onToken, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.edgeai.chat.database.ChatMessage> $completion) {
        return null;
    }
}