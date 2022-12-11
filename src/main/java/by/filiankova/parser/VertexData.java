package by.filiankova.parser;

import by.filiankova.math.Matrix4f;
import by.filiankova.math.Vector4f;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class VertexData {
    public Vector4f position;
    public Vector4f normal;
    public Matrix4f modelMatr;
    public Vector4f color;
}
