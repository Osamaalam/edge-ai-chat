package com.edgeai.chat.llama;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000P\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u0007\n\u0002\b\r\n\u0002\u0010\u0012\n\u0002\b\u0005\u0018\u0000 ,2\u00020\u0001:\u0001,B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0011\u0010\u0006\u001a\u00020\u00072\u0006\u0010\u0003\u001a\u00020\u0004H\u0082 J\u0011\u0010\b\u001a\u00020\u00072\u0006\u0010\u0005\u001a\u00020\u0004H\u0082 J \u0010\t\u001a\b\u0012\u0004\u0012\u00020\u000b0\n2\u0006\u0010\f\u001a\u00020\u000b2\n\b\u0002\u0010\r\u001a\u0004\u0018\u00010\u000eJ\u0011\u0010\u000f\u001a\u00020\u000b2\u0006\u0010\u0005\u001a\u00020\u0004H\u0082 J\u0006\u0010\u0010\u001a\u00020\u000bJ\t\u0010\u0011\u001a\u00020\u000bH\u0082 J\u0006\u0010\u0012\u001a\u00020\u000bJ\t\u0010\u0013\u001a\u00020\u0014H\u0082 JA\u0010\u0015\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00042\u0006\u0010\u0016\u001a\u00020\u00172\u0006\u0010\u0018\u001a\u00020\u00172\u0006\u0010\u0019\u001a\u00020\u001a2\u0006\u0010\u001b\u001a\u00020\u001a2\u0006\u0010\u001c\u001a\u00020\u00172\u0006\u0010\u001d\u001a\u00020\u001aH\u0082 J6\u0010\u001e\u001a\u00020\u00142\u0006\u0010\u0016\u001a\u00020\u00172\u0006\u0010\u0018\u001a\u00020\u00172\u0006\u0010\u0019\u001a\u00020\u001a2\u0006\u0010\u001b\u001a\u00020\u001a2\u0006\u0010\u001c\u001a\u00020\u00172\u0006\u0010\u001d\u001a\u00020\u001aJ\u0006\u0010\u001f\u001a\u00020\u0014J\"\u0010 \u001a\u00020\u00142\u0006\u0010!\u001a\u00020\u000b2\b\b\u0002\u0010\"\u001a\u00020\u000b2\b\b\u0002\u0010#\u001a\u00020\u0017J!\u0010$\u001a\u00020\u00042\u0006\u0010!\u001a\u00020\u000b2\u0006\u0010\"\u001a\u00020\u000b2\u0006\u0010#\u001a\u00020\u0017H\u0082 J\u0013\u0010%\u001a\u0004\u0018\u00010\u000b2\u0006\u0010\u0005\u001a\u00020\u0004H\u0082 J#\u0010&\u001a\u00020\u00142\u0006\u0010\u0005\u001a\u00020\u00042\u0006\u0010\f\u001a\u00020\u000b2\b\u0010\'\u001a\u0004\u0018\u00010(H\u0082 J\u0006\u0010)\u001a\u00020\u0007J\u0011\u0010*\u001a\u00020\u00072\u0006\u0010\u0005\u001a\u00020\u0004H\u0082 J\u0006\u0010+\u001a\u00020\u0007R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006-"}, d2 = {"Lcom/edgeai/chat/llama/LlamaModel;", "", "()V", "contextPtr", "", "modelPtr", "freeContext", "", "freeModel", "generate", "Lkotlinx/coroutines/flow/Flow;", "", "prompt", "bitmap", "Landroid/graphics/Bitmap;", "getModelInfo", "getModelInformation", "getSystemInfo", "getSystemInformation", "initBackend", "", "initContext", "nCtx", "", "nThreads", "temp", "", "topP", "topK", "repeatPenalty", "initializeContext", "isLoaded", "load", "modelPath", "mmprojPath", "nGpuLayers", "loadModel", "nextToken", "startCompletion", "imageBytes", "", "stop", "stopCompletion", "unload", "Companion", "app_debug"})
public final class LlamaModel {
    private long modelPtr = 0L;
    private long contextPtr = 0L;
    @org.jetbrains.annotations.NotNull()
    public static final com.edgeai.chat.llama.LlamaModel.Companion Companion = null;
    
    public LlamaModel() {
        super();
    }
    
    private final native boolean initBackend() {
        return false;
    }
    
    private final native long loadModel(java.lang.String modelPath, java.lang.String mmprojPath, int nGpuLayers) {
        return 0L;
    }
    
    private final native void freeModel(long modelPtr) {
    }
    
    private final native long initContext(long modelPtr, int nCtx, int nThreads, float temp, float topP, int topK, float repeatPenalty) {
        return 0L;
    }
    
    private final native void freeContext(long contextPtr) {
    }
    
    private final native boolean startCompletion(long modelPtr, java.lang.String prompt, byte[] imageBytes) {
        return false;
    }
    
    private final native java.lang.String nextToken(long modelPtr) {
        return null;
    }
    
    private final native void stopCompletion(long modelPtr) {
    }
    
    private final native java.lang.String getSystemInfo() {
        return null;
    }
    
    private final native java.lang.String getModelInfo(long modelPtr) {
        return null;
    }
    
    public final boolean isLoaded() {
        return false;
    }
    
    public final boolean load(@org.jetbrains.annotations.NotNull()
    java.lang.String modelPath, @org.jetbrains.annotations.NotNull()
    java.lang.String mmprojPath, int nGpuLayers) {
        return false;
    }
    
    public final boolean initializeContext(int nCtx, int nThreads, float temp, float topP, int topK, float repeatPenalty) {
        return false;
    }
    
    public final void unload() {
    }
    
    public final void stop() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getSystemInformation() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getModelInformation() {
        return null;
    }
    
    /**
     * Runs generation and streams back the generated tokens.
     */
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.lang.String> generate(@org.jetbrains.annotations.NotNull()
    java.lang.String prompt, @org.jetbrains.annotations.Nullable()
    android.graphics.Bitmap bitmap) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/edgeai/chat/llama/LlamaModel$Companion;", "", "()V", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}