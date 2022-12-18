package by.filiankova.scene;

import by.filiankova.math.Vector3f;
import by.filiankova.math.Vector4f;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class LightSource {
    private Vector4f color;
    private Vector3f position;
    private float diffuseIntensity;
    private float specularIntensity;
}
