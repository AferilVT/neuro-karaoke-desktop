package com.soul.neurokaraoke.audio

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class EqualizerBand(
    val index: Int,
    val centerFrequency: Int, // in milliHertz
    val minLevel: Int,        // in milliBels
    val maxLevel: Int,        // in milliBels
    val currentLevel: Int     // in milliBels
)

data class EqualizerPreset(
    val index: Int,
    val name: String
)

data class AudioEffectsState(
    // Equalizer
    val isEnabled: Boolean = false,
    val bands: List<EqualizerBand> = emptyList(),
    val presets: List<EqualizerPreset> = emptyList(),
    val currentPresetIndex: Int = -1,
    val isAvailable: Boolean = false,
    // Bass Boost
    val bassBoostEnabled: Boolean = false,
    val bassBoostStrength: Int = 0, // 0-1000
    val bassBoostAvailable: Boolean = false
)

// Keep old name for compatibility
typealias EqualizerState = AudioEffectsState

object EqualizerManager {
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var audioSessionId: Int = 0

    private val _state = MutableStateFlow(AudioEffectsState())
    val state: StateFlow<AudioEffectsState> = _state.asStateFlow()

    fun initialize(audioSessionId: Int) {
        if (this.audioSessionId == audioSessionId && equalizer != null) {
            return // Already initialized with same session
        }

        release() // Release any existing effects

        this.audioSessionId = audioSessionId

        // Initialize Equalizer
        try {
            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = _state.value.isEnabled
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Initialize Bass Boost
        try {
            bassBoost = BassBoost(0, audioSessionId).apply {
                enabled = _state.value.bassBoostEnabled
                if (strengthSupported) {
                    setStrength(_state.value.bassBoostStrength.toShort())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        updateState()
    }

    private fun updateState() {
        val eq = equalizer

        // Equalizer state
        var bands = emptyList<EqualizerBand>()
        var presets = emptyList<EqualizerPreset>()
        var eqAvailable = false
        var eqEnabled = _state.value.isEnabled

        if (eq != null) {
            try {
                val bandCount = eq.numberOfBands.toInt()
                bands = (0 until bandCount).map { i ->
                    val bandIndex = i.toShort()
                    EqualizerBand(
                        index = i,
                        centerFrequency = eq.getCenterFreq(bandIndex),
                        minLevel = eq.bandLevelRange[0].toInt(),
                        maxLevel = eq.bandLevelRange[1].toInt(),
                        currentLevel = eq.getBandLevel(bandIndex).toInt()
                    )
                }

                val presetCount = eq.numberOfPresets.toInt()
                presets = (0 until presetCount).map { i ->
                    EqualizerPreset(
                        index = i,
                        name = eq.getPresetName(i.toShort())
                    )
                }
                eqAvailable = true
                eqEnabled = eq.enabled
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Bass Boost state
        val bb = bassBoost
        val bbAvailable = bb != null && (bb.strengthSupported)
        val bbEnabled = bb?.enabled ?: false
        val bbStrength = try { bb?.roundedStrength?.toInt() ?: 0 } catch (e: Exception) { 0 }

        _state.value = AudioEffectsState(
            isEnabled = eqEnabled,
            bands = bands,
            presets = presets,
            currentPresetIndex = _state.value.currentPresetIndex,
            isAvailable = eqAvailable,
            bassBoostEnabled = bbEnabled,
            bassBoostStrength = bbStrength,
            bassBoostAvailable = bbAvailable
        )
    }

    fun setEnabled(enabled: Boolean) {
        try {
            equalizer?.enabled = enabled
            _state.value = _state.value.copy(isEnabled = enabled)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setBandLevel(bandIndex: Int, level: Int) {
        try {
            equalizer?.setBandLevel(bandIndex.toShort(), level.toShort())
            val updatedBands = _state.value.bands.map { band ->
                if (band.index == bandIndex) band.copy(currentLevel = level)
                else band
            }
            _state.value = _state.value.copy(
                bands = updatedBands,
                currentPresetIndex = -1 // Custom when manually adjusted
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun usePreset(presetIndex: Int) {
        try {
            equalizer?.usePreset(presetIndex.toShort())
            _state.value = _state.value.copy(currentPresetIndex = presetIndex)
            updateState() // Refresh band levels after preset change
            _state.value = _state.value.copy(currentPresetIndex = presetIndex)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun resetToFlat() {
        try {
            val eq = equalizer ?: return
            val bandCount = eq.numberOfBands.toInt()
            for (i in 0 until bandCount) {
                eq.setBandLevel(i.toShort(), 0)
            }
            _state.value = _state.value.copy(currentPresetIndex = -1)
            updateState()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Bass Boost functions
    fun setBassBoostEnabled(enabled: Boolean) {
        try {
            bassBoost?.enabled = enabled
            _state.value = _state.value.copy(bassBoostEnabled = enabled)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setBassBoostStrength(strength: Int) {
        try {
            val clampedStrength = strength.coerceIn(0, 1000)
            bassBoost?.setStrength(clampedStrength.toShort())
            _state.value = _state.value.copy(bassBoostStrength = clampedStrength)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun release() {
        try {
            equalizer?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            bassBoost?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        equalizer = null
        bassBoost = null
        audioSessionId = 0
    }

    fun formatFrequency(milliHertz: Int): String {
        val hz = milliHertz / 1000
        return if (hz >= 1000) {
            "${hz / 1000}kHz"
        } else {
            "${hz}Hz"
        }
    }

    fun formatLevel(milliBels: Int): String {
        val db = milliBels / 100f
        return if (db >= 0) "+${db.toInt()}dB" else "${db.toInt()}dB"
    }
}
