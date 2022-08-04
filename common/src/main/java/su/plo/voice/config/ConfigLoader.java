package su.plo.voice.config;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.conversion.ObjectConverter;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.file.FileNotFoundAction;
import lombok.AllArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

@AllArgsConstructor
public final class ConfigLoader {

    private final File configFile;
    private final InputStream defaultConfig;

    private final ObjectConverter converter = new ObjectConverter();

    public <T> T getConfig(Class<T> configClass) throws IOException {
        CommentedFileConfig config = CommentedFileConfig
                .builder(configFile)
                .onFileNotFound(FileNotFoundAction.copyData(defaultConfig))
                .concurrent()
                .build();

        // load defaults
        configFile.getParentFile().mkdirs();
        File tmpDefaults = new File(configFile.getParentFile(), "default_" + configFile.getName());
        if (tmpDefaults.exists()) tmpDefaults.delete();

        CommentedFileConfig defaultConfig = loadDefaults(tmpDefaults);
        defaultConfig.load();
        replaceDefaults(config, defaultConfig);

        tmpDefaults.delete();
        config.save();

        return converter.toObject(config, () -> {
            try {
                return configClass.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new IllegalArgumentException("provided class doesn't have an empty constructor");
            }
        });
    }

    private CommentedFileConfig loadDefaults(File tmpFile) throws IOException {
        Files.copy(defaultConfig, tmpFile.toPath());
        CommentedFileConfig fileConfig = CommentedFileConfig.builder(tmpFile).build();
        fileConfig.load();
        return fileConfig;
    }

    private void replaceDefaults(CommentedFileConfig config, CommentedFileConfig defaultConfig) {
        for (UnmodifiableConfig.Entry ue : defaultConfig.entrySet()) {
            List<String> key = Collections.singletonList(ue.getKey());
            Object value = ue.getRawValue();
            config.add(key, value);

            if (defaultConfig.getComment(key) != null) {
                config.setComment(key, defaultConfig.getComment(key));
            }

            if (value instanceof CommentedFileConfig) {
                replaceDefaults(config.get(key), (CommentedFileConfig) value);
            }
        }
    }
}
