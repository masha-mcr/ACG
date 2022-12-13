package by.filiankova.scene;

import by.filiankova.math.Vector3f;
import by.filiankova.math.Vector4f;
import lombok.AllArgsConstructor;

import java.util.List;

import static by.filiankova.math.Vector3f.normalize3;
import static by.filiankova.math.Vector3f.reflect3;
import static by.filiankova.math.Vector4f.normalize;
import static by.filiankova.math.Vector4f.reflect;
import static java.lang.Math.max;
import static java.lang.Math.pow;

@AllArgsConstructor
public class PhongShader {
    private LightSource lightSource;
    private float ambientness;
    private static final float SHININESS = 1.8f;

    public Vector4f getPixelColor(Vector4f cameraPosition, Vector4f pixelNormal, Vector4f pixelModelPosition, Vector4f pixelColor) {

        Vector4f ambientColor = pixelColor.mul(ambientness);
        Vector4f diffuseColor = new Vector4f();
        Vector4f specularColor = new Vector4f();
        if (Double.isNaN(ambientColor.x) | Double.isNaN(ambientColor.y) | Double.isNaN(ambientColor.z))
            System.out.println(ambientColor.x + " " + ambientColor.y + " " + ambientColor.z);
        Vector4f pixelToCameraVector = normalize(cameraPosition.minus(pixelModelPosition)); // V
        Vector4f pixelToLightVector = normalize(lightSource.getPosition().minus(pixelModelPosition));
        Vector4f reflectedLightVector = normalize(reflect(pixelToLightVector, pixelNormal));// R

        float diffuseCoeff = max(0, pixelNormal.dot(normalize(pixelToLightVector))) * lightSource.getDiffuseIntensity();
        float specularCoeff = 0;
        if (pixelToCameraVector.dot(reflectedLightVector) > 0)
            specularCoeff = max(0, (float) pow(pixelToCameraVector.dot(reflectedLightVector), SHININESS) * lightSource.getSpecularIntensity());

        diffuseColor = diffuseColor.plus(lightSource.getColor().mul(diffuseCoeff));
        specularColor = specularColor.plus(lightSource.getColor().mul(specularCoeff));
        if (Double.isNaN(diffuseColor.x) | Double.isNaN(diffuseColor.y) | Double.isNaN(diffuseColor.z))
            System.out.println(diffuseColor.x + " " + diffuseColor.y + " " + diffuseColor.z);
        if (Double.isNaN(specularColor.x) | Double.isNaN(specularColor.y) | Double.isNaN(specularColor.z))
            System.out.println(specularColor.x + " " + specularColor.y + " " + specularColor.z);
        return ambientColor.plus(diffuseColor).plus(specularColor);
    }
}
