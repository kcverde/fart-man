package com.kcverde.fartman.ui

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.kcverde.fartman.R

/**
 * The game's sound effects.
 *
 * An interface so the ViewModel can be unit tested off-device; see
 * [NoSoundPlayer].
 */
interface SoundPlayer {
  var isMuted: Boolean

  fun playCorrectBubble()

  fun playIncorrectPuff()

  fun playRoundStart()

  fun playVictoryDeflate()

  fun playMegaFartExplosion()

  fun release()
}

/**
 * [SoundPool]-backed effects.
 *
 * This used to be a global `object` whose pool was created on every Activity
 * start and never released. It is now owned by the ViewModel, which releases it
 * from `onCleared`, so the pool survives configuration changes and dies with
 * the screen.
 */
class AndroidSoundPlayer(context: Context) : SoundPlayer {

  override var isMuted: Boolean = false

  private val soundPool: SoundPool =
    SoundPool.Builder()
      .setMaxStreams(MAX_STREAMS)
      .setAudioAttributes(
        AudioAttributes.Builder()
          .setUsage(AudioAttributes.USAGE_GAME)
          .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
          .build()
      )
      .build()

  private val appContext = context.applicationContext
  private val fartExplosionId = soundPool.load(appContext, R.raw.fartexplosion, 1)
  private val pootId = soundPool.load(appContext, R.raw.poot, 1)
  private val tweetId = soundPool.load(appContext, R.raw.tweet, 1)
  private val tadaId = soundPool.load(appContext, R.raw.tada, 1)

  private fun play(soundId: Int) {
    if (isMuted || soundId == 0) return
    soundPool.play(soundId, VOLUME, VOLUME, PRIORITY, NO_LOOP, NORMAL_RATE)
  }

  override fun playCorrectBubble() = play(tweetId)

  override fun playIncorrectPuff() = play(pootId)

  override fun playRoundStart() = play(tadaId)

  override fun playVictoryDeflate() = play(tadaId)

  override fun playMegaFartExplosion() = play(fartExplosionId)

  override fun release() = soundPool.release()

  private companion object {
    const val MAX_STREAMS = 4
    const val VOLUME = 1f
    const val PRIORITY = 0
    const val NO_LOOP = 0
    const val NORMAL_RATE = 1f
  }
}

/** Silent stand-in for unit tests and Compose previews. */
class NoSoundPlayer : SoundPlayer {
  override var isMuted: Boolean = true

  override fun playCorrectBubble() = Unit

  override fun playIncorrectPuff() = Unit

  override fun playRoundStart() = Unit

  override fun playVictoryDeflate() = Unit

  override fun playMegaFartExplosion() = Unit

  override fun release() = Unit
}
