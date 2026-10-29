package com.tapbi.spark.voiceai.common

object Constant {

    // Broadcast Actions
    const val ACTION_SECURITY_QUESTION_UNLOCK =
        "com.tapbi.spark.voiceai.ACTION_SECURITY_QUESTION_UNLOCK"
    const val ACTION_BIOMETRIC_UNLOCK = "com.tapbi.spark.voiceai.ACTION_BIOMETRIC_UNLOCK"
    const val ACTION_BIOMETRIC_STARTED = "com.tapbi.spark.voiceai.ACTION_BIOMETRIC_STARTED"
    const val ACTION_BIOMETRIC_CANCELLED = "com.tapbi.spark.voiceai.ACTION_BIOMETRIC_CANCELLED"

    // On-Device Model Paths
    const val MODEL_SILERO_VAD_PATH = "model/silero_vad_16k_op15.onnx"
    const val MODEL_SPEAKER_CAMPPLUS_PATH =
        "model/3dspeaker_speech_campplus_sv_zh_en_16k-common_advanced.onnx"
    const val MODEL_ZIPFORMER_ASSET_FOLDER = "model/zipformer"

    // Language Codes
    const val DEFAULT_LANGUAGE_CODE = "vi-VN"
    const val LANGUAGE_EN = "en"
    const val GERMAN_LANGUAGE_CODE = "de"
    const val SPANISH_LANGUAGE_CODE = "es"
    const val JAPAN_LANGUAGE_CODE = "ja"
    const val FRENCH_LANGUAGE_CODE = "fr"
    const val KOREA_LANGUAGE_CODE = "ko"
    const val PORTUGAL_LANGUAGE_CODE = "pt"
    const val RUSSIAN_LANGUAGE_CODE = "ru"
    const val HINDI_LANGUAGE_CODE = "hi"
    const val VIETNAM_LANGUAGE_CODE = "vi"
    const val ITALIA_LANGUAGE_CODE = "it"
    const val INDONESIA_LANGUAGE_CODE = "in"
    const val THAI_LANGUAGE_CODE = "th"
    const val CHINESE_LANGUAGE_CODE = "zh"
    const val ARABIC_LANGUAGE_CODE = "ar"
    const val GREEK_LANGUAGE_CODE = "el"
    const val MALAY_LANGUAGE_CODE = "ms"
    const val DUTCH_LANGUAGE_CODE = "nl"
    const val TAGALOG_LANGUAGE_CODE = "tl"

    // Official Zipformer ONNX Pretrained Model Download Links (.tar.bz2) - GitHub Releases (100% Verified)
    val ZIPFORMER_MODEL_URLS = mapOf(
        "vi-VN" to "https://github.com/k2-fsa/sherpa-onnx/releases/download/asr-models/sherpa-onnx-zipformer-vi-30M-int8-2026-02-09.tar.bz2",
        "ru-RU" to "https://github.com/k2-fsa/sherpa-onnx/releases/download/asr-models/sherpa-onnx-zipformer-ru-int8-2025-04-20.tar.bz2",
        "en-US" to "https://github.com/k2-fsa/sherpa-onnx/releases/download/asr-models/sherpa-onnx-zipformer-small-en-2023-06-26.tar.bz2",
        "zh-CN" to "https://github.com/k2-fsa/sherpa-onnx/releases/download/asr-models/sherpa-onnx-zipformer-multi-zh-hans-2023-9-2.tar.bz2",
        "ja-JP" to "https://github.com/k2-fsa/sherpa-onnx/releases/download/asr-models/sherpa-onnx-zipformer-ja-reazonspeech-2024-08-01.tar.bz2",
        "ko-KR" to "https://github.com/k2-fsa/sherpa-onnx/releases/download/asr-models/sherpa-onnx-zipformer-korean-2024-06-24.tar.bz2",
        "th-TH" to "https://github.com/k2-fsa/sherpa-onnx/releases/download/asr-models/sherpa-onnx-zipformer-thai-2024-06-20.tar.bz2",
        "yue-HK" to "https://github.com/k2-fsa/sherpa-onnx/releases/download/asr-models/sherpa-onnx-zipformer-cantonese-2024-03-13.tar.bz2"
    )

    // Tải TRỰC TIẾP từng file int8 đã giải nén từ HuggingFace — bỏ qua hoàn toàn bước giải nén .tar.bz2
    // (archive gốc chứa cả encoder FP32 rất lớn phải giải mã bz2 trước khi tới file int8).
    // Đã verify qua HF API. Ngôn ngữ không có ở đây sẽ fallback về ZIPFORMER_MODEL_URLS.
    private fun hfFiles(repo: String, tag: String) = listOf(
        "https://huggingface.co/$repo/resolve/main/encoder-$tag.int8.onnx",
        "https://huggingface.co/$repo/resolve/main/decoder-$tag.onnx",
        "https://huggingface.co/$repo/resolve/main/joiner-$tag.int8.onnx",
        "https://huggingface.co/$repo/resolve/main/tokens.txt"
    )

    val ZIPFORMER_DIRECT_FILES: Map<String, List<String>> = mapOf(
        "ja-JP" to hfFiles("reazon-research/reazonspeech-k2-v2", "epoch-99-avg-1"),
        "ko-KR" to hfFiles("k2-fsa/sherpa-onnx-zipformer-korean-2024-06-24", "epoch-99-avg-1"),
        "zh-CN" to hfFiles("zrjin/sherpa-onnx-zipformer-multi-zh-hans-2023-9-2", "epoch-20-avg-1"),
        "en-US" to hfFiles(
            "csukuangfj/sherpa-onnx-zipformer-small-en-2023-06-26",
            "epoch-99-avg-1"
        ),
        "th-TH" to listOf(
            "https://huggingface.co/yfyeung/icefall-asr-gigaspeech2-th-zipformer-2024-06-20/resolve/main/exp/encoder-epoch-12-avg-5.int8.onnx",
            "https://huggingface.co/yfyeung/icefall-asr-gigaspeech2-th-zipformer-2024-06-20/resolve/main/exp/decoder-epoch-12-avg-5.onnx",
            "https://huggingface.co/yfyeung/icefall-asr-gigaspeech2-th-zipformer-2024-06-20/resolve/main/exp/joiner-epoch-12-avg-5.int8.onnx",
            "https://huggingface.co/yfyeung/icefall-asr-gigaspeech2-th-zipformer-2024-06-20/resolve/main/data/lang_bpe_2000/tokens.txt"
        ),
        "yue-HK" to listOf(
            "https://huggingface.co/zrjin/icefall-asr-mdcc-zipformer-2024-03-11/resolve/main/exp/encoder-epoch-45-avg-35.int8.onnx",
            "https://huggingface.co/zrjin/icefall-asr-mdcc-zipformer-2024-03-11/resolve/main/exp/decoder-epoch-45-avg-35.onnx",
            "https://huggingface.co/zrjin/icefall-asr-mdcc-zipformer-2024-03-11/resolve/main/exp/joiner-epoch-45-avg-35.int8.onnx",
            "https://huggingface.co/zrjin/icefall-asr-mdcc-zipformer-2024-03-11/resolve/main/data/lang_char/tokens.txt"
        )
    )

    // Supported Speech Languages Display Names
    val SUPPORTED_SPEECH_LANGUAGES_MAP = mapOf(
        "vi-VN" to "🇻🇳 Tiếng Việt (vi - int8)",
        "ru-RU" to "🇷🇺 Tiếng Nga (ru - int8)",
        "en-US" to "🇺🇸 Tiếng Anh (en - int8)",
        "zh-CN" to "🇨🇳 Tiếng Trung (zh - int8)",
        "ja-JP" to "🇯🇵 Tiếng Nhật (ja - int8)",
        "ko-KR" to "🇰🇷 Tiếng Hàn (ko - int8)",
        "th-TH" to "🇹🇭 Tiếng Thái (th - int8)",
        "yue-HK" to "🇭🇰 Tiếng Quảng Đông (yue - int8)"
    )

    // db
    const val DB_VERSION = 1
    const val DB_NAME = "SOCIAL_LOCKSCREEN"
    const val TEMPLATE_TABLE: String = "TemplateTable"

    // remote api
    const val HEADER_X_API_KEY = "x-api-key"
    const val FIREBASE_CONFIG_X_API_KEY = "x_api_key"
    const val BASE_URL = "https://1ov8ijvsuk.execute-api.eu-central-1.amazonaws.com/"
    const val PARAM_UPDATE_THEME = "theme/update"
    const val PARAM_THEME = "theme"

    // remote config
    const val FIREBASE_REMOTE_VERSION_UPDATE_THEME = "version_update_theme"

    // file
    const val FILE_THEME_STORAGE = "ThemeStorage"
}
