package su.plo.voice.addon;

import su.plo.voice.api.PlasmoVoice;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public final class AddonClassLoader extends URLClassLoader {

    private static final Set<AddonClassLoader> loaders = new CopyOnWriteArraySet<>();

    static {
        ClassLoader.registerAsParallelCapable();
    }

    public AddonClassLoader(URL[] urls) {
        super(urls, PlasmoVoice.class.getClassLoader());
    }

    public void addToClassloaders() {
        loaders.add(this);
    }

    @Override
    public void close() throws IOException {
        loaders.remove(this);
        super.close();
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        return loadClass0(name, resolve, true);
    }

    private Class<?> loadClass0(String name, boolean resolve, boolean checkOther)
            throws ClassNotFoundException {
        try {
            return super.loadClass(name, resolve);
        } catch (ClassNotFoundException ignored) {
        }

        if (checkOther) {
            for (AddonClassLoader loader : loaders) {
                if (loader != this) {
                    try {
                        return loader.loadClass0(name, resolve, false);
                    } catch (ClassNotFoundException ignored) {
                    }
                }
            }
        }

        throw new ClassNotFoundException(name);
    }
}
