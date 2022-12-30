package by.filiankova.scene;

import by.filiankova.math.*;
import by.filiankova.parser.PixelData;
import by.filiankova.parser.VertexData;
import lombok.Getter;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.util.List;

import static by.filiankova.math.Vector3f.normalize3;
import static by.filiankova.scene.ColorUtil.colorOf;
import static by.filiankova.scene.ColorUtil.rgbaVec;
import static java.lang.Integer.MAX_VALUE;
import static java.lang.Math.*;

public class Screen {
    private final int width;
    private final int height;

    @Getter
    private final Camera camera;
    private final Projection projection;

    @Getter
    private final BufferedImage bufferedImage;
    private float[] zBuffer;
    private LightSource lightSource;

    public Screen(int width, int height) {
        this.width = width;
        this.height = height;
        bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        camera = new Camera(0.3f, new Vector3f(0, 0, 0), new Vector3f(0, 0, 1), new Vector3f(0, 1, 0));
        projection = new Projection(65, 1.33f, 0, 100);
        zBuffer = new float[width * height];
        lightSource = new LightSource(rgbaVec(ColorUtil.WHITE), new Vector3f(10, 10 , 0), 0.3f, 0.3f);
    }

    public void drawModel(Model model) {
        Matrix4f modelMatr = model.getModel();
        PhongShader shader = new PhongShader(lightSource, 0.7f);
        Vector4f modelColor = rgbaVec(ColorUtil.BLUE);

        List<Vector3f> vertices = model.getVertices();
        List<Vector3f> normals = model.getNormals();
        List<Vector2f> uvTextures = model.getUvTextures();
        Raster texture = model.getTexture();
        for (List<Vector3i> face : model.getFaces()) {
            int verticesPerFace = face.size();
            for (int i = 1; i < verticesPerFace - 1; i++) {
                Vector3f v1 = vertices.get(face.get(0).x);
                Vector3f v2 = vertices.get(face.get(i).x);
                Vector3f v3 = vertices.get(face.get(i + 1).x);

                Vector3f vn1 = normals.get(face.get(0).z);
                Vector3f vn2 = normals.get(face.get(i).z);
                Vector3f vn3 = normals.get(face.get(i + 1).z);

                Vector2f vt1 = face.get(0).y == (MAX_VALUE - 1) ? new Vector2f(0, 0) : uvTextures.get(face.get(0).y);
                Vector2f vt2 = face.get(i).y == (MAX_VALUE - 1) ? new Vector2f(0, 0) : uvTextures.get(face.get(i).y);
                Vector2f vt3 = face.get(i + 1).y == (MAX_VALUE - 1) ? new Vector2f(0, 0) : uvTextures.get(face.get(i + 1).y);

                VertexData vd1 = new VertexData(v1, vn1, modelMatr, modelColor, vt1);
                VertexData vd2 = new VertexData(v2, vn2, modelMatr, modelColor, vt2);
                VertexData vd3 = new VertexData(v3, vn3, modelMatr, modelColor, vt3);

                drawTriangle(vd1, vd2, vd3, shader, model);
            }
        }
    }

    private boolean isBackface(Vector3f vm1, Vector3f vm2, Vector3f vm3) {
        Vector3f side1 = vm2.minus(vm1);
        Vector3f side2 = vm3.minus(vm1);
        Vector3f triangleNormal = normalize3(side1.cross(side2));
        Vector3f gaze = vm1.minus(camera.getEye());
        Vector3f direction = normalize3(gaze);
        return direction.dot(triangleNormal) > 0;
    }


    private int bound(int v, int max) {
        return Math.max(min(v, max), 0);
    }

    private float edge(float x1, float x2, float y1, float y2, float px, float py) {
        return (px - x1) * (y2 - y1) - (py - y1) * (x2 - x1);
    }
    public void drawTriangle(VertexData vd3, VertexData vd2, VertexData vd1, PhongShader shader, Model model) {

        Matrix4f mvp = vd1.modelMatr.multiply(camera.getViewMatrix()).multiply(projection.getProjectionMatrix());
        Vector4f v1 = mvp.multiply(new Vector4f(vd1.position, 1.0f));
        Vector4f v2 = mvp.multiply(new Vector4f(vd2.position, 1.0f));
        Vector4f v3 = mvp.multiply(new Vector4f(vd3.position, 1.0f));

        Vector3f v1n = vd1.modelMatr.multiply(new Vector4f(vd1.normal, 0)).getXYZ();
        Vector3f v2n = vd2.modelMatr.multiply(new Vector4f(vd2.normal, 0)).getXYZ();
        Vector3f v3n = vd3.modelMatr.multiply(new Vector4f(vd3.normal, 0)).getXYZ();

        Vector3f v1m = vd1.modelMatr.multiply(new Vector4f(vd1.position, 1)).getXYZ();
        Vector3f v2m = vd2.modelMatr.multiply(new Vector4f(vd2.position, 1)).getXYZ();
        Vector3f v3m = vd3.modelMatr.multiply(new Vector4f(vd3.position, 1)).getXYZ();

        if (isBackface(v3m, v2m, v1m)) {
            //System.out.println("backface");
            return;
        }

        float v1tx = vd1.texture.x;
        float v1ty = vd1.texture.y;
        float v2tx = vd2.texture.x;
        float v2ty = vd2.texture.y;
        float v3tx = vd3.texture.x;
        float v3ty = vd3.texture.y;

        float x1 = (v1.x / v1.w * width / 2.f) + width / 2.f;
        float x2 = (v2.x / v2.w * width / 2.f) + width / 2.f;
        float x3 = (v3.x / v3.w * width / 2.f) + width / 2.f;

        float y1 = (v1.y / v1.w * height / 2.f) + height / 2.f;
        float y2 = (v2.y / v2.w * height / 2.f) + height / 2.f;
        float y3 = (v3.y / v3.w * height / 2.f) + height / 2.f;

        float z1 = v1.z;
        float z2 = v2.z;
        float z3 = v3.z;

        v1tx /= z1; v1ty /= z1;
        v2tx /= z2; v2ty /= z2;
        v3tx /= z3; v3ty /= z3;

        // pre-compute 1 over z
        z1 = 1.f / z1;
        z2 = 1.f / z2;
        z3 = 1.f / z3;

        if (z1 < 0 || z2 < 0 || z3 < 0) {
            return;
        }

        float minX = min(min(x1, x2), x3);
        float maxX = max(max(x1, x2), x3);
        float minY = min(min(y1, y2), y3);
        float maxY = max(max(y1, y2), y3);

        Raster texture = model.getTexture();
        Raster normalMap = model.getNormalMap();
        Raster specularMap = model.getSpecularMap();
        int tWidth = texture.getWidth()-1;
        int tHeight = texture.getHeight() - 1;
        int cubeWidth = tWidth / 4;
        int cubeHeight = tHeight / 3;
        int[] cubefaceStartX = {2, 0, 1, 1, 1, 3};
        int[] cubefaceStartY = {1, 1, 0, 2, 1, 1};
        int nWidth = normalMap == null ? 0 : normalMap.getWidth() - 1;
        int nHeight = normalMap == null ? 0 : normalMap.getHeight() - 1;
        int[] color = new int[4];
        int[] normal = new int[4];
        int[] specular = new int[4];
        Vector3f cameraPosition = camera.getEye();
        for (float y = bound(round(minY), height); y <= bound(round(maxY), height); y += 1) {
            for (float x = bound(round(minX), width); x <= bound(round(maxX), width); x += 1) {
                float e1 = edge(x1, x2, y1, y2, x, y);
                float e2 = edge(x2, x3, y2, y3, x, y);
                float e3 = edge(x3, x1, y3, y1, x, y);
                if (e1 >= 0 && e3 >= 0 && e2 >= 0) {
                    float area = edge(x1, x2, y1, y2, x3, y3);
                    float w3 = e1 / area;
                    float w2 = e3 / area;
                    float w1 = e2 / area;

                    int idx = (int) y * width + (int) x;
                    if (x < 0 || x >= width || y < 0 || y >= height) {
                        continue;
                    }
                    float initialBuf = zBuffer[idx];
                    float z = 1 / (w1 * z1 + w2 * z2 + w3 * z3);
                    if (z < initialBuf) {
                        zBuffer[idx] = z;
                    } else {
                        continue;
                    }
                    float s = w1 * v1tx + w2 * v2tx + w3 * v3tx;
                    float t = w1 * v1ty + w2 * v2ty + w3 * v3ty;
                    s *= z;
                    t *= z;

                    int sTexture = (int)(s * tWidth);
                    int tTexture = (int)(tHeight * (1 - t));
                    int sNormal = (int)(s * nWidth);
                    int tNormal = (int)(nHeight * (1 - t));

                    int faceIndex = getFaceIndex(new Vector3f(
                            vd1.position.x * w1 + vd2.position.x * w2 + vd3.position.x * w3,
                            vd1.position.y * w1 + vd2.position.y * w2 + vd3.position.y * w3,
                            vd1.position.z * w1 + vd2.position.z * w2 + vd3.position.z * w3
                    ));
                    if (faceIndex == 4 || faceIndex == 0 || faceIndex == 3) {
                        s = 1 - s;
                        t = 1 - t;
                    }
                    if (faceIndex == 1) {
                        float temp = s;
                        s = 1 - t;
                        t = temp;
                    }
                    if (faceIndex == 2) {
                        float temp = s;
                        s = t;
                        t = 1 - temp;
                    }
                    if (s < 0 || t < 0) continue;

                    sTexture = (int)(s * cubeWidth) + cubefaceStartX[faceIndex]*cubeWidth;
                    tTexture = (int)(cubeHeight * (1 - t)) + cubefaceStartY[faceIndex]*cubeHeight;
                    int[] pixelColorArr = texture.getPixel(sTexture, tTexture, color);
                    int[] normalArr = normalMap == null ? null : normalMap.getPixel(sNormal, tNormal, normal);
                    int[] specularArr = specularMap == null ? null : specularMap.getPixel(sNormal, tNormal, specular);

                    Vector3f pixelModelPosition = v1m.mul(w1).plus(v2m.mul(w2)).plus(v3m.mul(w3));
                    float specularCoefficient = specularMap == null ? 0 : specularArr[0] / 255f;

                    Vector3f pixelNormal;
//                    if (normalMap == null) {
                        Vector3f normalVector = v1n.mul(w1).plus(v2n.mul(w2)).plus(v3n.mul(w3));
                        pixelNormal = normalize3(normalVector);
//                    }
//                    else {
//                        Vector3f normalFromMap = new Vector3f(normalArr[0], normalArr[1], normalArr[2]).mul(2)
//                                .minus(new Vector3f(256f, 256f, 256f));
//                        pixelNormal = normalize3(vd1.modelMatr.multiply(new Vector4f(normalFromMap, 0)).getXYZ());
//                    }
                    Vector4f pixelColor = rgbaVec(colorOf(pixelColorArr[0], pixelColorArr[1], pixelColorArr[2], 255));
                    PixelData pixelData = new PixelData(pixelNormal, pixelModelPosition, pixelColor, specularCoefficient);
                    Vector4f finalColor = shader.getPixelColor(cameraPosition, pixelData);

                    drawPixel((int) x, (int) y, colorOf(finalColor));
                }
            }
        }
    }

    public void drawPixel(int x, int y, int color) {
        bufferedImage.setRGB(x, height - 1 - y, color);
    }

    public void clear() {
        bufferedImage.getGraphics().clearRect(0, 0, width, height);
        zBuffer = new float[width * height];
        for (int i = 0; i < width * height; i++) {
            zBuffer[i] = 100.0f;
        }
    }

    private int getFaceIndex(Vector3f v)
    {
        Vector3f vAbs = v.abs();
        int faceIndex;
        if(vAbs.z >= vAbs.x && vAbs.z >= vAbs.y)
        {
            faceIndex = v.z < 0.0 ? 5 : 4;
        }
        else if(vAbs.y >= vAbs.x)
        {
            faceIndex = v.y < 0.0 ? 3 : 2;
        }
        else
        {
            faceIndex = v.x < 0.0 ? 1 : 0;
        }
        return faceIndex;
    }

}
