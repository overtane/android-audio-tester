package com.github.overtane.audiotester.menu

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.overtane.audiotester.R
import com.github.overtane.audiotester.databinding.FragmentAudioDeviceListBinding

class AudioDeviceListFragment : Fragment() {

    private lateinit var binding: FragmentAudioDeviceListBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAudioDeviceListBinding.inflate(layoutInflater)
        binding.root.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = AudioDeviceRecyclerViewAdapter(audioDevices())
            return this
        }
    }

    private fun audioDevices(): List<AudioDeviceDetails> {
        val manager = context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return manager.getDevices(AudioManager.GET_DEVICES_ALL)?.map {
            AudioDeviceDetails(it.id, it.details())
        }?.toList().orEmpty()
    }

    private fun AudioDeviceInfo.details(): String {
        val direction = if (isSink) "Output" else "Input"
        val addr = if (address.isNotEmpty()) ", $address" else ""
        val rates = if (sampleRates.isNotEmpty()) sampleRates.joinToString() else "ARBITRARY"
        val channels = if (channelCounts.isNotEmpty()) channelCounts.joinToString() else "ARBITRARY"
        return "$productName$addr\n" +
                "Direction: $direction\n" +
                "Device type: ${type.asAudioDeviceType()}\n" +
                "Sample rates: $rates\n" +
                "Channel counts: $channels"
    }

    private fun Int.asAudioDeviceType(): String {
        return when (this) {
            AudioDeviceInfo.TYPE_BUILTIN_EARPIECE -> "BUILTIN EARPIECE"
            AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> "BUILTIN SPEAKER"
            AudioDeviceInfo.TYPE_WIRED_HEADSET -> "WIRED HEADSET"
            AudioDeviceInfo.TYPE_WIRED_HEADPHONES -> "WIRED HEADPHONES"
            AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> "BLUETOOTH SCO"
            AudioDeviceInfo.TYPE_BLUETOOTH_A2DP -> "BLUETOOTH A2DP"
            AudioDeviceInfo.TYPE_HDMI -> "HDMI"
            AudioDeviceInfo.TYPE_DOCK -> "DOCK"
            AudioDeviceInfo.TYPE_USB_ACCESSORY -> "USB ACCESSORY"
            AudioDeviceInfo.TYPE_USB_DEVICE -> "USB DEVICE"
            AudioDeviceInfo.TYPE_USB_HEADSET -> "USB HEADSET"
            AudioDeviceInfo.TYPE_TELEPHONY -> "TELEPHONY"
            AudioDeviceInfo.TYPE_LINE_ANALOG -> "LINE ANALOG"
            AudioDeviceInfo.TYPE_HDMI_ARC -> "HDMI ARC"
            AudioDeviceInfo.TYPE_HDMI_EARC -> "HDMI EARC"
            AudioDeviceInfo.TYPE_LINE_DIGITAL -> "LINE DIGITAL"
            AudioDeviceInfo.TYPE_FM -> "FM"
            AudioDeviceInfo.TYPE_AUX_LINE -> "AUX LINE"
            AudioDeviceInfo.TYPE_IP -> "IP"
            AudioDeviceInfo.TYPE_BUS -> "BUS"
            AudioDeviceInfo.TYPE_HEARING_AID -> "HEARING AID"
            AudioDeviceInfo.TYPE_BUILTIN_MIC -> "BUILTIN MIC"
            AudioDeviceInfo.TYPE_FM_TUNER -> "FM TUNER"
            AudioDeviceInfo.TYPE_TV_TUNER -> "TV TUNER"
            AudioDeviceInfo.TYPE_BUILTIN_SPEAKER_SAFE -> "BUILTIN SPEAKER SAFE"
            AudioDeviceInfo.TYPE_REMOTE_SUBMIX -> "REMOTE SUBMIX"
            AudioDeviceInfo.TYPE_BLE_HEADSET -> "BLE HEADSET"
            AudioDeviceInfo.TYPE_BLE_SPEAKER -> "BLE SPEAKER"
            AudioDeviceInfo.TYPE_BLE_BROADCAST -> "BLE BROADCAST"
            else -> "UNKNOWN"
        }
    }
}