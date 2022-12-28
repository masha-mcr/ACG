package by.filiankova.parser;

import by.filiankova.math.Vector3f;
import by.filiankova.math.Vector4f;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PixelData {
    private Vector3f pixelNormal;
    private Vector3f pixelModelPosition;
    private Vector4f pixelColor;
    private Float specularCoefficient;
}
