package net.shelg.pawnbot.voice.tts

import com.google.cloud.texttospeech.v1.*
import org.apache.commons.io.IOUtils
import org.springframework.stereotype.Component
import java.io.File
import java.io.FileOutputStream

@Component
class GoogleTTSClient {
    fun textToSpeech(text: String, outputFile: File) {
        TextToSpeechClient.create().use { ttsClient ->
            ttsClient.synthesizeSpeech(
                    SynthesisInput.newBuilder()
                            .setText(text)
                            .build(),
                    VoiceSelectionParams.newBuilder()
                            .setLanguageCode(LANGUAGE_CODE)
                            .setName(VOICE_NAME)
                            .build(),
                    AudioConfig.newBuilder()
                            .setAudioEncoding(AudioEncoding.OGG_OPUS)
                            .setPitch(0.0)
                            .setSpeakingRate(1.0)
                            .build()
            ).audioContent.newInput().use { input ->
                FileOutputStream(outputFile).use { output ->
                    IOUtils.copy(input, output)
                }
            }
        }
    }

    companion object {
        private const val LANGUAGE_CODE = "en-US"
        private const val VOICE_NAME = "en-US-Wavenet-A"
    }
}