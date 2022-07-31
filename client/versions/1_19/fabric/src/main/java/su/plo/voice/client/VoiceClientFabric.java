package su.plo.voice.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.openal.EXTThreadLocalContext;
import su.plo.voice.api.client.audio.device.*;
import su.plo.voice.api.client.audio.source.AlSource;
import su.plo.voice.api.client.audio.source.SourceGroup;
import su.plo.voice.api.pos.Pos3d;
import su.plo.voice.api.util.Params;

import javax.sound.sampled.AudioFormat;
import java.io.File;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static com.google.common.base.Preconditions.checkNotNull;

@Environment(EnvType.CLIENT)
public final class VoiceClientFabric extends VoiceClientMod implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        super.onInitialize();

        // todo: должно ли это быть тут?
        ClientLifecycleEvents.CLIENT_STOPPING.register((minecraft) -> super.onShutdown());

        ClientPlayConnectionEvents.JOIN.register((handler, sender, minecraft) -> {
            System.out.println("joined the world");

            int sampleRate = 48_000;
            int bufferSize = (sampleRate / 1_000) * 2 * 20;
            var format = new AudioFormat((float) sampleRate, 16, 1, true, false);

            Optional<DeviceFactory> optInputFactory = getDeviceFactoryManager().getDeviceFactory("AL_INPUT");
            Optional<DeviceFactory> optOutputFactory = getDeviceFactoryManager().getDeviceFactory("AL_OUTPUT");
            if (optInputFactory.isEmpty() || optOutputFactory.isEmpty()) {
                logger.warn("Failed to get device factories");
                return;
            }

            DeviceFactory inputFactory = optInputFactory.get();
            DeviceFactory outputFactory = optOutputFactory.get();

            DeviceManager devices = getDeviceManager();

            new Thread(() -> {
                try {
                    Thread.sleep(1_000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                OutputDevice<AlSource> output;
                InputDevice input;
                SourceGroup<AlSource> sourceGroup;
                try {
                    output = (OutputDevice<AlSource>) outputFactory.openDevice(format, null, Params.EMPTY).get();

                    devices.add(output);

                    sourceGroup = devices.createSourceGroup(DeviceType.OUTPUT);
                    sourceGroup.add(
                            output.createSource(Params.builder().set("num_buffers", 16).build())
                    );
                    ((AlAudioDevice) output).runInContext(() -> {
                        sourceGroup.getSources().forEach(source -> {
                            source.setVolume(1.0F);
                            source.setRelative(true);
                            source.setPosition(new Pos3d(0, 0, 0));
                        });
                    });

                    input = (InputDevice) inputFactory.openDevice(
                            format,
                            null,
                            Params.EMPTY
                    ).get();
                } catch (InterruptedException | ExecutionException | DeviceException e) {
                    e.printStackTrace();
                    return;
                }

                AlSource source = sourceGroup.getSources().iterator().next();

//                SourceDataLine line;
//                DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
//                try {
//                    line = (SourceDataLine) AudioSystem.getLine(info);
//                    line.open(format);
//                    line.start();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    return;
//                }

                EXTThreadLocalContext.alcSetThreadContext(((AlAudioDevice) output).getContextPointer().get());

                while (true) {
                    input.start();

                    var samples = input.read(bufferSize);
                    if (samples == null) {
                        try {
                            Thread.sleep(5L);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        continue;
                    }

                    ((AlAudioDevice) output).runInContext(() -> {
                        source.play();
                        source.write(samples);
                    });
                }
            }).start();
        });
    }

    @Override
    public @NotNull String getVersion() {
        ModContainer modContainer = FabricLoader.getInstance()
                .getModContainer("plasmo_voice")
                .orElse(null);
        checkNotNull(modContainer, "modContainer cannot be null");
        return modContainer.getMetadata().getVersion().getFriendlyString();
    }

    @Override
    protected File configFolder() {
        return new File("config/" + modId);
    }

    @Override
    protected File modsFolder() {
        return new File("mods");
    }

    @Override
    protected File addonsFolder() {
        return new File(configFolder(), "addons");
    }
}
