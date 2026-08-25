package su.plo.voice.mac.probe

import su.plo.voice.mac.protocol.audio.CaptureFormat
import java.io.ByteArrayInputStream
import java.io.File
import javax.sound.sampled.AudioFileFormat
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem

/**
 * Writes raw PCM samples to disk as a WAV file.
 */
object WavFile {
    fun write(file: File, format: CaptureFormat, samples: ByteArray) {
        val audioFormat = AudioFormat(
            format.sampleRate.toFloat(),
            16, // Bits per sample
            format.channels,
            true, // Signed
            false // Little endian
        )

        val inputStream = AudioInputStream(
            ByteArrayInputStream(samples),
            audioFormat,
            samples.size / audioFormat.frameSize.toLong()
        )

        AudioSystem.write(inputStream, AudioFileFormat.Type.WAVE, file)
    }
}
