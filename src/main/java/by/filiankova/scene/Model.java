package by.filiankova.scene;

import by.filiankova.math.Matrix4f;
import by.filiankova.math.Vector3i;
import by.filiankova.math.Vector4f;
import lombok.Data;
import lombok.Getter;

import java.util.List;

@Data
public class Model {
    private Matrix4f translation;
    @Getter
    private Matrix4f rotation;
    private Matrix4f scale;

    private final List<Vector4f> vertices;
    private final List<Vector4f> normals;
    private final List<List<Vector3i>> faces;

    private Matrix4f model;

    private void setModel() {
        model = scale.multiply(rotation).multiply(translation);
    }

    public Model(Matrix4f translation, Matrix4f rotation, Matrix4f scale, List<Vector4f> vertices, List<List<Vector3i>> faces, List<Vector4f> normals) {
        this.translation = translation;
        this.rotation = rotation;
        this.scale = scale;
        this.vertices = vertices;
        this.faces = faces;
        this.normals = normals;

        setModel();
    }

    public void setRotation(Matrix4f rotation) {
        this.rotation = rotation;
        setModel();
    }

    void setTranslation(Matrix4f translation) {
        this.translation = translation;
        setModel();
    }

    public void setScale(Matrix4f scale) {
        this.scale = scale;
        setModel();
    }
}
