package su.plo.lib.api.client.render;

import org.jetbrains.annotations.NotNull;

public interface MinecraftMatrix {

    void translate(double x, double y, double z);

    void scale(float x, float y, float z) ;

    void multiply(@NotNull MinecraftQuaternion quaternion);

    void push();

    void pop();
}
