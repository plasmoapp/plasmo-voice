//#if FABRIC
package su.plo.voice.client.integration

import me.shedaniel.clothconfig2.api.ConfigBuilder
import me.shedaniel.clothconfig2.api.ConfigCategory
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder
import me.shedaniel.clothconfig2.impl.builders.BooleanToggleBuilder
import me.shedaniel.clothconfig2.impl.builders.IntFieldBuilder
import net.minecraft.client.gui.screens.Screen
import su.plo.config.entry.BooleanConfigEntry
import su.plo.config.entry.IntConfigEntry
import su.plo.lib.mod.client.render.RenderUtil
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.voice.BaseVoice
import su.plo.voice.client.ModVoiceClient
import su.plo.voice.client.extension.getStringSplitToWidth
import su.plo.voice.client.extension.toMinecraft

private fun BooleanConfigEntry.entryBuilder(
    entryBuilder: ConfigEntryBuilder,
    text: McTextComponent,
    onSave: (Boolean) -> Unit = {},
): BooleanToggleBuilder =
    entryBuilder.startBooleanToggle(text.toMinecraft(), value())
        .setDefaultValue(default)
        .setSaveConsumer {
            set(it)
            onSave(it)
        }

private fun BooleanToggleBuilder.tooltip(
    text: McTextComponent,
) = setTooltip(
    *getStringSplitToWidth(
        RenderUtil.getFormattedString(text),
        180f,
        true,
        true
    )
        .map { McTextComponent.literal(it).toMinecraft() }
        .toTypedArray()
)

private fun IntConfigEntry.entryBuilder(
    entryBuilder: ConfigEntryBuilder,
    text: McTextComponent,
    onSave: (Int) -> Unit = {},
): IntFieldBuilder =
    entryBuilder.startIntField(text.toMinecraft(), value())
        .setDefaultValue(default)
        .setSaveConsumer {
            set(it)
            onSave(it)
        }

private fun configBuilder(builder: ConfigBuilder.() -> Unit) =
    ConfigBuilder.create()
        .apply(builder)

private fun ConfigBuilder.category(text: McTextComponent, builder: ConfigCategory.() -> Unit) =
    getOrCreateCategory(text.toMinecraft())
        .apply(builder)

fun createClothConfigMenu(parent: Screen): Screen =
    configBuilder {
        val voiceClient = ModVoiceClient.INSTANCE
        val config = voiceClient.config

        parentScreen = parent
        title = McTextComponent.literal("Plasmo Voice").toMinecraft()
        savingRunnable = Runnable { config.save(true) }

        category(McTextComponent.translatable("clothconfig.plasmovoice.devices")) {
            fun reloadInputDevice() {
                // todo: deduplicate?
                if (config.voice.disableInputDevice.value()) return

                val devices = voiceClient.deviceManager
                try {
                    devices.inputDevice.ifPresent { it.close() }
                    val newDevice = devices.openInputDevice(null)
                    devices.setInputDevice(newDevice)
                } catch (e: Exception) {
                    BaseVoice.LOGGER.error("Failed to open input device", e)
                }
            }

            addEntry(
                config.voice.useJavaxInput
                    .entryBuilder(
                        entryBuilder(),
                        McTextComponent.translatable("clothconfig.plasmovoice.devices.use_javax_input")
                    ) { _ ->
                        reloadInputDevice()
                    }
                    .tooltip(McTextComponent.translatable("clothconfig.plasmovoice.devices.use_javax_input.tooltip"))
                    .build()
            )
        }

        category(McTextComponent.translatable("clothconfig.plasmovoice.advanced")) {
            addEntry(
                config.checkForUpdates
                    .entryBuilder(
                        entryBuilder(),
                        McTextComponent.translatable("clothconfig.plasmovoice.advanced.check_for_updates")
                    )
                    .build()
            )

            addEntry(
                config.debug
                    .entryBuilder(
                        entryBuilder(),
                        McTextComponent.translatable("clothconfig.plasmovoice.advanced.debug_logger")
                    ) {
                        BaseVoice.DEBUG_LOGGER.enabled(it || System.getProperty("plasmovoice.debug") != null)
                    }
                    .build()
            )

            addEntry(
                config.advanced.cameraSoundListener
                    .entryBuilder(
                        entryBuilder(),
                        McTextComponent.translatable("clothconfig.plasmovoice.advanced.camera_sound_listener")
                    )
                    .tooltip(McTextComponent.translatable("clothconfig.plasmovoice.advanced.camera_sound_listener.tooltip"))
                    .build()
            )

            addEntry(
                config.advanced.jitterPacketDelay
                    .entryBuilder(
                        entryBuilder(),
                        McTextComponent.translatable("clothconfig.plasmovoice.advanced.jitter_buffer_packet_delay")
                    ) {
                        voiceClient.sourceManager.clear()
                    }
                    .build()
            )
        }
    }.build()
//#endif
