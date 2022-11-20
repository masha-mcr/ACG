package by.filiankova.parser;

import by.filiankova.math.Vector3i;
import by.filiankova.math.Vector4f;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@Builder
@RequiredArgsConstructor
public class ObjData {
    private final List<Vector4f> vertices;
    private final List<Vector4f> textures;
    private final List<Vector4f> normals;
    private final List<Vector4f> vertexParams;
    private final List<List<Vector3i>> surfaces;
}