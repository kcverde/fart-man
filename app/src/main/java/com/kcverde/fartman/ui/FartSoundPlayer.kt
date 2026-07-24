package com.kcverde.fartman.ui

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import com.kcverde.fartman.R

object FartSoundPlayer {
    private const val TAG = "FartSoundPlayer"
    var isMuted: Boolean = false

    private var soundPool: SoundPool? = null
    
    // Sound Ids
    private var fartExplosionId: Int = 0
    private var pootId: Int = 0
    private var tweetId: Int = 0
    private var tadaId: Int = 0

    fun init(context: Context) {
        if (soundPool != null) return
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
            
        soundPool = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(audioAttributes)
            .build()

        val appContext = context.applicationContext
        fartExplosionId = soundPool?.load(appContext, R.raw.fartexplosion, 1) ?: 0
        pootId = soundPool?.load(appContext, R.raw.poot, 1) ?: 0
        tweetId = soundPool?.load(appContext, R.raw.tweet, 1) ?: 0
        tadaId = soundPool?.load(appContext, R.raw.tada, 1) ?: 0
    }

    private fun playSound(soundId: Int) {
        if (isMuted || soundId == 0) return
        soundPool?.play(soundId, 1f, 1f, 0, 0, 1f)
    }

    fun playCorrectBubble() {
        playSound(tweetId) // tweet for correct letter
    }

    fun playIncorrectPuff() {
        playSound(pootId) // poot for incorrect letter
    }

    fun playVictoryDeflate() {
        playSound(tadaId) // tada for victory
    }

    fun playMegaFartExplosion() {
        playSound(fartExplosionId) // fartexplosion for loss
    }
    
    fun playTada() {
        playSound(tadaId) // tada when starting
    }
}
