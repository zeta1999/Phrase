package xyz.belvi.phrase.translateMedium.medium

import android.content.Context
import androidx.annotation.RawRes
import androidx.core.content.ContextCompat
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.translate.Translate
import com.google.cloud.translate.TranslateOptions
import kotlinx.coroutines.*
import xyz.belvi.phrase.R
import xyz.belvi.phrase.options.PhraseDetected
import xyz.belvi.phrase.translateMedium.TranslationMedium
import java.io.InputStream

class FirebaseMLKitTranslate(
    context: Context, @RawRes authCredentials: Int
) : TranslationMedium() {
    val translate by lazy {
        GlobalScope.async(Dispatchers.IO) {
            val stream: InputStream = context.resources.openRawResource(authCredentials)
            val myCredentials = GoogleCredentials.fromStream(stream)
            val translateOptions: TranslateOptions =
                TranslateOptions.newBuilder().setCredentials(myCredentials).build()
            translateOptions.service
        }
    }

    override fun translate(text: String, targeting: String): String {
        return runBlocking {
            withContext(Dispatchers.IO) {
                translate.await().translate(
                    text,
                    Translate.TranslateOption.targetLanguage(targeting)
                ).translatedText
            }
        }
    }

    override fun name(): String {
        return "Google"
    }

    override fun detect(text: String): PhraseDetected {
        return runBlocking {
            withContext(Dispatchers.IO) {
                translate.await().let {

                    val detect = it.detect(text).language
                    val languageName =
                        it.listSupportedLanguages().find { it.code == detect }?.name ?: detect
                    PhraseDetected(text, detect, languageName, this@FirebaseMLKitTranslate)
                }
            }
        }
    }

}
