package by.filiankova.scene;

import by.filiankova.math.Vector3f;
import by.filiankova.math.Vector4f;
import lombok.AllArgsConstructor;

import static by.filiankova.math.Vector3f.normalize3;
import static by.filiankova.math.Vector3f.reflect3;
import static java.lang.Math.max;
import static java.lang.Math.pow;

@AllArgsConstructor
public class PhongShader {
    private LightSource lightSource;
    private float ambientness;
    private static final float SHININESS = 44f;

    public Vector4f getPixelColor(Vector3f cameraPosition, Vector3f pixelNormal, Vector3f pixelModelPosition, Vector4f pixelColor) {

        Vector4f ambientColor = pixelColor.mul(ambientness);
        Vector4f diffuseColor = new Vector4f();
        Vector4f specularColor = new Vector4f();
        Vector3f pixelToCameraVector = normalize3(cameraPosition.minus(pixelModelPosition)); // V
        Vector3f pixelToLightVector = normalize3(lightSource.getPosition().minus(pixelModelPosition));
        Vector3f reflectedLightVector = normalize3(reflect3(pixelToLightVector, pixelNormal));// R

        float diffuseCoeff = max(0, pixelNormal.dot(normalize3(pixelToLightVector))) * lightSource.getDiffuseIntensity();
        float specularCoeff = max(0, (float) pow(pixelToCameraVector.dot(reflectedLightVector), SHININESS) * lightSource.getSpecularIntensity());

        diffuseColor = diffuseColor.plus(lightSource.getColor().mul(diffuseCoeff));
        specularColor = specularColor.plus(lightSource.getColor().mul(specularCoeff));
        return ambientColor.plus(diffuseColor).plus(specularColor);
    }
}
