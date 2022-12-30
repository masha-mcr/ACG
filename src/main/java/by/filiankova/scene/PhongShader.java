package by.filiankova.scene;

import by.filiankova.math.Vector3f;
import by.filiankova.math.Vector4f;
import by.filiankova.parser.PixelData;
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

    public Vector4f getPixelColor(Vector3f cameraPosition, PixelData pixelData) {

        Vector4f ambientColor = pixelData.getPixelColor().mul(ambientness);
        Vector4f diffuseColor = new Vector4f();
        Vector4f specularColor = new Vector4f();
        Vector3f pixelToCameraVector = normalize3(cameraPosition.minus(pixelData.getPixelModelPosition())); // V
        Vector3f pixelToLightVector = normalize3(lightSource.getPosition().minus(pixelData.getPixelModelPosition()));
        Vector3f reflectedLightVector = normalize3(reflect3(pixelToLightVector, pixelData.getPixelNormal()));// R


        float diffuseCoeff = max(0, pixelData.getPixelNormal().dot(normalize3(pixelToLightVector))) * lightSource.getDiffuseIntensity();
        float specularCoeff;


        if (pixelData.getSpecularCoefficient() == 0)
            specularCoeff = max(0, pixelData.getSpecularCoefficient() * (float) pow(pixelToCameraVector.dot(reflectedLightVector), SHININESS) * lightSource.getSpecularIntensity());
        else
            specularCoeff = max(0, (float) pow(pixelToCameraVector.dot(reflectedLightVector), SHININESS) * lightSource.getSpecularIntensity());

        diffuseColor = diffuseColor.plus(lightSource.getColor().mul(diffuseCoeff));
        specularColor = specularColor.plus(lightSource.getColor().mul(specularCoeff));
        return ambientColor.plus(diffuseColor).plus(specularColor);
    }
}
