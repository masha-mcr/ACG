package by.filiankova.scene;

import by.filiankova.math.Matrix4f;
import by.filiankova.math.Vector4f;

import static java.lang.Math.tan;

public class Projection {
    float fov, aspect, near, far;
    Matrix4f projection;

    public Projection(float fov, float aspect, float near, float far) {
        this.fov = fov;
        this.aspect = aspect;
        this.near = near;
        this.far = far;
        setProjection();
    }

    public Matrix4f getProjectionMatrix() {
        return projection;
    }

    void setProjection() {
        float radians = (float) Math.PI / 180.f * fov;
        float sx = (1.f / (float) tan(radians / 2.f)) / aspect;
        float sy = (1.f / (float) tan(radians / 2.f));
        float sz = (far + near) / (near - far);
        float dz = (2.f * far * near) / (near - far);
        projection = new Matrix4f(
                new Vector4f(sx, 0, 0, 0),
                new Vector4f(0, sy, 0, 0),
                new Vector4f(0, 0, sz, dz),
                new Vector4f(0, 0, -1, 0)
        );
    }
}
