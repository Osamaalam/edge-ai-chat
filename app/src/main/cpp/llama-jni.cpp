#include <jni.h>
#include <string>
#include <vector>
#include <android/log.h>
#include <cmath>
#include <algorithm>
#include <chrono>

#include "llama.h"
#include "mtmd.h"
#include "mtmd-helper.h"

#define LOG_TAG "LlamaAndroid"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

struct InferenceState {
    llama_model* model = nullptr;
    llama_context* ctx = nullptr;
    
    // Vision
    mtmd_context* ctx_mtmd = nullptr;
    
    // Generation parameters
    int n_threads = 4;
    float temp = 0.7f;
    float top_p = 0.9f;
    int top_k = 40;
    float penalty = 1.1f;
    
    // Generation state
    std::vector<llama_token> last_tokens;
    int n_past = 0;
    int n_consumed = 0;
    bool completed = true;
    bool stop_requested = false;
    
    // Stats
    double load_time_ms = 0;
    int prompt_tokens_count = 0;
    int generated_tokens_count = 0;
    std::chrono::steady_clock::time_point generation_start;
};

static void llama_batch_add(
        struct llama_batch & batch,
               llama_token   id,
                 llama_pos   pos,
    const std::vector<llama_seq_id> & seq_ids,
                      bool   logits) {
    batch.token   [batch.n_tokens] = id;
    batch.pos     [batch.n_tokens] = pos;
    batch.n_seq_id[batch.n_tokens] = seq_ids.size();
    for (size_t i = 0; i < seq_ids.size(); ++i) {
        batch.seq_id[batch.n_tokens][i] = seq_ids[i];
    }
    batch.logits  [batch.n_tokens] = logits;

    batch.n_tokens++;
}

std::string token_to_piece(const llama_context* ctx, llama_token token) {
    const struct llama_vocab * vocab = llama_model_get_vocab(llama_get_model(ctx));
    std::vector<char> result(8, 0);
    const int n_tokens = llama_token_to_piece(vocab, token, result.data(), result.size(), 0, false);
    if (n_tokens < 0) {
        result.resize(-n_tokens);
        int check = llama_token_to_piece(vocab, token, result.data(), result.size(), 0, false);
        if (check < 0) {
            return "";
        }
    } else {
        result.resize(n_tokens);
    }
    return std::string(result.data(), result.size());
}

llama_token sample_token(llama_context* ctx, int last_token_idx, float temp, float top_p, int top_k, float penalty, const std::vector<llama_token>& last_tokens) {
    const struct llama_vocab * vocab = llama_model_get_vocab(llama_get_model(ctx));
    int n_vocab = llama_vocab_n_tokens(vocab);
    
    // Create a modern unified sampler chain instead of using deprecated sample functions
    struct llama_sampler_chain_params sparams = llama_sampler_chain_default_params();
    struct llama_sampler * smpl = llama_sampler_chain_init(sparams);
    
    // Add repetition penalties
    if (penalty != 1.0f) {
        llama_sampler_chain_add(smpl, llama_sampler_init_penalties(
            64,       // last n tokens to penalize
            penalty,  // repeat penalty
            0.0f,     // freq penalty
            0.0f      // present penalty
        ));
    }
    
    if (temp <= 0.0f) {
        llama_sampler_chain_add(smpl, llama_sampler_init_greedy());
    } else {
        llama_sampler_chain_add(smpl, llama_sampler_init_temp(temp));
        llama_sampler_chain_add(smpl, llama_sampler_init_top_k(top_k));
        llama_sampler_chain_add(smpl, llama_sampler_init_top_p(top_p, 1));
        llama_sampler_chain_add(smpl, llama_sampler_init_dist(42)); // Use stable seed
    }
    
    // Sample using the unified chain
    llama_token token = llama_sampler_sample(smpl, ctx, -1);
    
    // Free the chain to prevent memory leak
    llama_sampler_free(smpl);
    
    return token;
}

static void llama_log_callback_android(enum ggml_log_level level, const char * text, void * user_data) {
    android_LogPriority priority = ANDROID_LOG_INFO;
    if (level == GGML_LOG_LEVEL_WARN) {
        priority = ANDROID_LOG_WARN;
    } else if (level == GGML_LOG_LEVEL_ERROR) {
        priority = ANDROID_LOG_ERROR;
    }
    __android_log_print(priority, "LlamaAndroid_Native", "%s", text);
}

extern "C" {

JNIEXPORT jboolean JNICALL
Java_com_edgeai_chat_llama_LlamaModel_initBackend(JNIEnv* env, jobject thiz) {
    LOGI("Initializing llama.cpp backend");
    llama_backend_init();
    llama_log_set(llama_log_callback_android, nullptr);
    return JNI_TRUE;
}

JNIEXPORT jlong JNICALL
Java_com_edgeai_chat_llama_LlamaModel_loadModel(JNIEnv* env, jobject thiz, jstring model_path, jstring mmproj_path, jint n_gpu_layers) {
    auto start_time = std::chrono::steady_clock::now();
    
    const char* model_path_str = env->GetStringUTFChars(model_path, nullptr);
    const char* mmproj_path_str = env->GetStringUTFChars(mmproj_path, nullptr);
    
    LOGI("Loading model from %s", model_path_str);
    
    InferenceState* state = new InferenceState();
    
    llama_model_params mparams = llama_model_default_params();
    mparams.n_gpu_layers = n_gpu_layers;
    mparams.use_mmap = false;  // Force standard heap buffer allocation, bypassing restricted emulator mmap sandbox
    mparams.use_mlock = true;  // Keep the model securely pinned in physical RAM
    
    state->model = llama_load_model_from_file(model_path_str, mparams);
    if (!state->model) {
        LOGE("Failed to load model from %s", model_path_str);
        delete state;
        env->ReleaseStringUTFChars(model_path, model_path_str);
        env->ReleaseStringUTFChars(mmproj_path, mmproj_path_str);
        return 0;
    }
    
    // Load MTMD context for vision if path is provided and not empty
    if (mmproj_path_str && strlen(mmproj_path_str) > 0) {
        LOGI("Loading MTMD context from %s", mmproj_path_str);
        mtmd_context_params mparams = mtmd_context_params_default();
        mparams.use_gpu = n_gpu_layers > 0;
        mparams.print_timings = true;
        mparams.n_threads = 4;
        state->ctx_mtmd = mtmd_init_from_file(mmproj_path_str, state->model, mparams);
        if (!state->ctx_mtmd) {
            LOGE("Failed to load MTMD context");
        }
    }
    
    auto end_time = std::chrono::steady_clock::now();
    state->load_time_ms = std::chrono::duration<double, std::milli>(end_time - start_time).count();
    LOGI("Model loaded successfully in %.2f ms", state->load_time_ms);
    
    env->ReleaseStringUTFChars(model_path, model_path_str);
    env->ReleaseStringUTFChars(mmproj_path, mmproj_path_str);
    
    return reinterpret_cast<jlong>(state);
}

JNIEXPORT void JNICALL
Java_com_edgeai_chat_llama_LlamaModel_freeModel(JNIEnv* env, jobject thiz, jlong model_ptr) {
    InferenceState* state = reinterpret_cast<InferenceState*>(model_ptr);
    if (state) {
        if (state->ctx) {
            llama_free(state->ctx);
        }
        if (state->model) {
            llama_free_model(state->model);
        }
        if (state->ctx_mtmd) {
            mtmd_free(state->ctx_mtmd);
        }
        delete state;
        LOGI("Model freed");
    }
}

JNIEXPORT jlong JNICALL
Java_com_edgeai_chat_llama_LlamaModel_initContext(JNIEnv* env, jobject thiz, jlong model_ptr, jint n_ctx, jint n_threads, jfloat temp, jfloat top_p, jint top_k, jfloat repeat_penalty) {
    InferenceState* state = reinterpret_cast<InferenceState*>(model_ptr);
    if (!state || !state->model) return 0;
    
    LOGI("Initializing context with n_ctx=%d, n_threads=%d", n_ctx, n_threads);
    
    if (state->ctx) {
        llama_free(state->ctx);
        state->ctx = nullptr;
    }
    
    llama_context_params cparams = llama_context_default_params();
    cparams.n_ctx = n_ctx;
    cparams.n_threads = n_threads;
    cparams.n_threads_batch = n_threads;
    
    state->ctx = llama_new_context_with_model(state->model, cparams);
    if (!state->ctx) {
        LOGE("Failed to create context");
        return 0;
    }
    
    state->n_threads = n_threads;
    state->temp = temp;
    state->top_p = top_p;
    state->top_k = top_k;
    state->penalty = repeat_penalty;
    
    return reinterpret_cast<jlong>(state->ctx);
}

JNIEXPORT void JNICALL
Java_com_edgeai_chat_llama_LlamaModel_freeContext(JNIEnv* env, jobject thiz, jlong context_ptr) {
    // Context is freed as part of freeModel to avoid crash
}

JNIEXPORT jboolean JNICALL
Java_com_edgeai_chat_llama_LlamaModel_startCompletion(JNIEnv* env, jobject thiz, jlong model_ptr, jstring prompt, jbyteArray image_bytes) {
    InferenceState* state = reinterpret_cast<InferenceState*>(model_ptr);
    if (!state || !state->ctx) return JNI_FALSE;
    
    const char* prompt_str = env->GetStringUTFChars(prompt, nullptr);
    
    state->stop_requested = false;
    state->completed = false;
    state->n_past = 0;
    state->prompt_tokens_count = 0;
    state->generated_tokens_count = 0;
    state->last_tokens.clear();

    // Clear previous KV cache memory to reset context state for sequence 0
    llama_memory_t mem = llama_get_memory(state->ctx);
    if (mem != nullptr) {
        llama_memory_clear(mem, true);
    }
    
    if (state->ctx_mtmd != nullptr) {
        std::string final_prompt;
        if (image_bytes != nullptr) {
            std::string p_str(prompt_str);
            std::string marker = mtmd_default_marker();
            if (p_str.find(marker) == std::string::npos) {
                final_prompt = marker + "\n" + p_str;
            } else {
                final_prompt = p_str;
            }
        } else {
            final_prompt = prompt_str;
        }

        mtmd_input_text text;
        text.text = final_prompt.c_str();
        text.add_special = llama_vocab_get_add_bos(llama_model_get_vocab(state->model));
        text.parse_special = true;

        mtmd_input_chunks* chunks = mtmd_input_chunks_init();
        int32_t res = 0;

        if (image_bytes != nullptr) {
            LOGI("Processing image input via MTMD");
            jsize len = env->GetArrayLength(image_bytes);
            jbyte* body = env->GetByteArrayElements(image_bytes, nullptr);
            
            struct mtmd_helper_bitmap_wrapper wrap = mtmd_helper_bitmap_init_from_buf(
                state->ctx_mtmd,
                reinterpret_cast<const unsigned char*>(body),
                len,
                false
            );
            
            env->ReleaseByteArrayElements(image_bytes, body, JNI_ABORT);
            
            if (wrap.bitmap) {
                const mtmd_bitmap* bitmaps[1] = { wrap.bitmap };
                res = mtmd_tokenize(
                    state->ctx_mtmd,
                    chunks,
                    &text,
                    bitmaps,
                    1
                );
                mtmd_bitmap_free(wrap.bitmap);
                if (wrap.video_ctx) {
                    mtmd_helper_video_free(wrap.video_ctx);
                }
            } else {
                LOGE("Failed to create mtmd_bitmap from bytes");
                res = -1;
            }
        } else {
            res = mtmd_tokenize(
                state->ctx_mtmd,
                chunks,
                &text,
                nullptr,
                0
            );
        }

        if (res != 0) {
            LOGE("Failed to tokenize prompt with MTMD: %d", res);
            mtmd_input_chunks_free(chunks);
            env->ReleaseStringUTFChars(prompt, prompt_str);
            return JNI_FALSE;
        }

        llama_pos new_n_past = state->n_past;
        int32_t eval_res = mtmd_helper_eval_chunks(
            state->ctx_mtmd,
            state->ctx,
            chunks,
            state->n_past,
            0,
            2048,
            true,
            &new_n_past
        );

        if (eval_res != 0) {
            LOGE("Failed to evaluate MTMD chunks: %d", eval_res);
            mtmd_input_chunks_free(chunks);
            env->ReleaseStringUTFChars(prompt, prompt_str);
            return JNI_FALSE;
        }

        state->n_past = new_n_past;
        state->prompt_tokens_count = mtmd_helper_get_n_tokens(chunks);
        LOGI("Evaluated %d MTMD prompt tokens", state->prompt_tokens_count);

        mtmd_input_chunks_free(chunks);
    } else {
        // Fallback to text-only mode
        const struct llama_vocab * vocab = llama_model_get_vocab(state->model);
        std::vector<llama_token> tokens(llama_n_ctx(state->ctx));
        bool add_bos = llama_vocab_get_add_bos(vocab);
        int n_tokens = llama_tokenize(vocab, prompt_str, strlen(prompt_str), tokens.data(), tokens.size(), add_bos, true);
        
        if (n_tokens < 0) {
            LOGE("Failed to tokenize prompt");
            env->ReleaseStringUTFChars(prompt, prompt_str);
            return JNI_FALSE;
        }
        
        LOGI("Tokenized %d prompt tokens", n_tokens);
        state->prompt_tokens_count = n_tokens;
        
        llama_batch batch = llama_batch_init(n_tokens, 0, 1);
        for (int i = 0; i < n_tokens; i++) {
            llama_batch_add(batch, tokens[i], state->n_past + i, {0}, i == n_tokens - 1);
        }
        
        if (llama_decode(state->ctx, batch) != 0) {
            LOGE("Failed to decode prompt");
            llama_batch_free(batch);
            env->ReleaseStringUTFChars(prompt, prompt_str);
            return JNI_FALSE;
        }
        
        state->n_past += n_tokens;
        llama_batch_free(batch);
    }
    
    state->generation_start = std::chrono::steady_clock::now();
    env->ReleaseStringUTFChars(prompt, prompt_str);
    return JNI_TRUE;
}

JNIEXPORT jstring JNICALL
Java_com_edgeai_chat_llama_LlamaModel_nextToken(JNIEnv* env, jobject thiz, jlong model_ptr) {
    InferenceState* state = reinterpret_cast<InferenceState*>(model_ptr);
    if (!state || !state->ctx || state->completed || state->stop_requested) return nullptr;
    
    // Sample next token
    llama_token token = sample_token(state->ctx, -1, state->temp, state->top_p, state->top_k, state->penalty, state->last_tokens);
    
    // Check for EOS
    if (llama_vocab_is_eog(llama_model_get_vocab(state->model), token)) {
        state->completed = true;
        LOGI("EOS reached");
        return nullptr;
    }
    
    state->last_tokens.push_back(token);
    if (state->last_tokens.size() > 64) {
        state->last_tokens.erase(state->last_tokens.begin());
    }
    
    state->generated_tokens_count++;
    
    // Convert to string
    std::string piece = token_to_piece(state->ctx, token);
    
    // Decode next token
    llama_batch batch = llama_batch_init(1, 0, 1);
    llama_batch_add(batch, token, state->n_past, {0}, true);
    
    if (llama_decode(state->ctx, batch) != 0) {
        LOGE("Failed to decode token");
        state->completed = true;
        llama_batch_free(batch);
        return nullptr;
    }
    
    state->n_past++;
    llama_batch_free(batch);
    
    return env->NewStringUTF(piece.c_str());
}

JNIEXPORT void JNICALL
Java_com_edgeai_chat_llama_LlamaModel_stopCompletion(JNIEnv* env, jobject thiz, jlong model_ptr) {
    InferenceState* state = reinterpret_cast<InferenceState*>(model_ptr);
    if (state) {
        state->stop_requested = true;
        LOGI("Completion stop requested");
    }
}

JNIEXPORT jstring JNICALL
Java_com_edgeai_chat_llama_LlamaModel_getSystemInfo(JNIEnv* env, jobject thiz) {
    std::string info = llama_print_system_info();
    return env->NewStringUTF(info.c_str());
}

JNIEXPORT jstring JNICALL
Java_com_edgeai_chat_llama_LlamaModel_getModelInfo(JNIEnv* env, jobject thiz, jlong model_ptr) {
    InferenceState* state = reinterpret_cast<InferenceState*>(model_ptr);
    if (!state || !state->model) return env->NewStringUTF("No Model Loaded");
    
    // Build a nice model metadata string
    const struct llama_vocab * vocab = llama_model_get_vocab(state->model);
    int n_vocab = llama_vocab_n_tokens(vocab);
    int n_ctx_train = llama_n_ctx_train(state->model);
    
    char buffer[512];
    snprintf(buffer, sizeof(buffer), 
             "Architecture: Qwen/Llama\n"
             "Vocab Size: %d\n"
             "Context Train: %d\n"
             "Load Time: %.2f ms\n"
             "Prompt Tokens: %d\n"
             "Generated Tokens: %d", 
             n_vocab, n_ctx_train, state->load_time_ms, state->prompt_tokens_count, state->generated_tokens_count);
             
    return env->NewStringUTF(buffer);
}

}
