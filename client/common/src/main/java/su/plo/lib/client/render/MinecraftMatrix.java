package su.plo.lib.client.render;

public interface MinecraftMatrix {

    void translate(double x, double y, double z);

    void scale(float x, float y, float z) ;

    void multiply(float x, float y, float z, float w);

    void push();

    void pop();
}
