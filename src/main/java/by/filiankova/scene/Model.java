package by.filiankova.scene;

import by.filiankova.math.Matrix4f;
import by.filiankova.math.Vector2f;
import by.filiankova.math.Vector3f;
import by.filiankova.math.Vector3i;
import lombok.Data;
import lombok.Getter;

import javax.imageio.ImageIO;
import java.awt.image.Raster;
import java.io.File;
import java.util.List;

@Data
public class Model {
    private Matrix4f translation;
    @Getter
    private Matrix4f rotation;
    private Matrix4f scale;

    private final List<Vector3f> vertices;
    private final List<Vector3f> normals;
    private final List<Vector2f> uvTextures;
    private final List<List<Vector3i>> faces;
    private final Raster texture;
    private final Raster normalMap;
    private final Raster specularMap;
    private Matrix4f model;

    private void setModel() {
        model = scale.multiply(rotation).multiply(translation);
    }

    public Model(Matrix4f translation, Matrix4f rotation, Matrix4f scale, List<Vector3f> vertices, List<List<Vector3i>> faces, List<Vector3f> normals, List<Vector2f> uvTextures, String texturePath, String normalPath, String specPath) {
        this.translation = translation;
        this.rotation = rotation;
        this.scale = scale;
        this.vertices = vertices;
        this.faces = faces;
        this.normals = normals;
        Raster imgTemp;
        try {
            imgTemp = ImageIO.read(new File(texturePath)).getRaster();
        } catch (Exception e) {
            imgTemp = null;
        }
        this.texture = imgTemp;
        try {
            imgTemp = ImageIO.read(new File(normalPath)).getRaster();
        } catch (Exception e) {
            imgTemp = null;
        }
        this.normalMap = imgTemp;
        try {
            imgTemp = ImageIO.read(new File(specPath)).getRaster();
        } catch (Exception e) {
            imgTemp = null;
        }
        this.specularMap = imgTemp;
        this.uvTextures = uvTextures;
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
