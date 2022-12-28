package by.filiankova.parser;

import by.filiankova.math.Matrix4f;
import by.filiankova.math.Vector2f;
import by.filiankova.math.Vector3f;
import by.filiankova.math.Vector4f;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class VertexData {
    public Vector3f position;
    public Vector3f normal;
    public Matrix4f modelMatr;
    public Vector4f color;
    public Vector2f texture;
}
