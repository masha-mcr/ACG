package by.filiankova.scene;

import by.filiankova.math.Matrix4f;
import by.filiankova.math.Vector3f;
import by.filiankova.math.Vector3i;
import by.filiankova.math.Vector4f;
import by.filiankova.parser.VertexData;
import lombok.Getter;

import java.awt.image.BufferedImage;
import java.util.List;

import static by.filiankova.math.Vector3f.normalize3;
import static by.filiankova.math.Vector4f.normalize;
import static by.filiankova.scene.ColorUtil.colorOf;
import static java.lang.Math.*;

public class Screen {
    static final Vector4f lightDirection = new Vector4f(2, -1, 1);
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
        lightSource = new LightSource(ColorUtil.rgbaVec(ColorUtil.WHITE), new Vector3f(10, 10 , 0), 0.5f, 0.3f);
    }

    public void drawModel(Model model) {
        Matrix4f modelMatr = model.getModel();
        PhongShader shader = new PhongShader(lightSource, 0.7f);
        Vector4f modelColor = ColorUtil.rgbaVec(ColorUtil.BLUE);

        List<Vector3f> vertices = model.getVertices();
        List<Vector3f> normals = model.getNormals();
        for (List<Vector3i> face : model.getFaces()) {
            int verticesPerFace = face.size();
            for (int i = 1; i < verticesPerFace - 1; i++) {
                Vector3f v1 = vertices.get(face.get(0).x);
                Vector3f v2 = vertices.get(face.get(i).x);
                Vector3f v3 = vertices.get(face.get(i + 1).x);

                Vector3f vn1 = normals.get(face.get(0).z);
                Vector3f vn2 = normals.get(face.get(i).z);
                Vector3f vn3 = normals.get(face.get(i + 1).z);

                VertexData vd1 = new VertexData(v1, vn1, modelMatr, modelColor);
                VertexData vd2 = new VertexData(v2, vn2, modelMatr, modelColor);
                VertexData vd3 = new VertexData(v3, vn3, modelMatr, modelColor);

                drawTriangle(vd1, vd2, vd3, shader);
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
    public void drawTriangle(VertexData vd3, VertexData vd2, VertexData vd1, PhongShader shader) {

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

        float x1 = (v1.x / v1.w * width / 2.f) + width / 2.f;
        float x2 = (v2.x / v2.w * width / 2.f) + width / 2.f;
        float x3 = (v3.x / v3.w * width / 2.f) + width / 2.f;

        float y1 = (v1.y / v1.w * height / 2.f) + height / 2.f;
        float y2 = (v2.y / v2.w * height / 2.f) + height / 2.f;
        float y3 = (v3.y / v3.w * height / 2.f) + height / 2.f;

        float z1 = v1.z;
        float z2 = v2.z;
        float z3 = v3.z;

        if (z1 < 0 || z2 < 0 || z3 < 0) {
            return;
        }

        float minX = min(min(x1, x2), x3);
        float maxX = max(max(x1, x2), x3);
        float minY = min(min(y1, y2), y3);
        float maxY = max(max(y1, y2), y3);
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

                    Vector3f pixelNormal = normalize3(v1n.mul(w1).plus(v2n.mul(w2)).plus(v3n.mul(w3)));
                    Vector3f pixelModelPosition = v1m.mul(w1).plus(v2m.mul(w2)).plus(v3m.mul(w3));
                    Vector4f pixelColor = vd1.color.mul(w1).plus(vd2.color.mul(w2)).plus(vd3.color.mul(w3));

                    Vector4f finalColor = shader.getPixelColor(cameraPosition, pixelNormal, pixelModelPosition, pixelColor);

                    drawPixel((int) x, (int) y, colorOf(finalColor), z1 * w1 + z2 * w2 + z3 * w3);
                }
            }
        }
    }

    public static float bound(float v, int max) {
        return Math.max(min(v, max), 0);
    }

    private boolean isInViewport(int coord, int boundary){
        return coord >= 0 & coord <= boundary;
    }

    public void drawLine(int x1, int x2, int y1, int y2, int color, float z) {
        int dx = Math.abs(x2 - x1);
        int sx = x1 < x2 ? 1 : -1;
        int dy = -Math.abs(y2-y1);
        int sy = y1 < y2 ? 1 : -1;
        int err = dx + dy;
        drawPixel(x1, y1, color, z);
        while (x1 != x2 || y1 != y2) {
            drawPixel(x1, y1, color, z);
            int err2 = err * 2;
            if (err2 >= dy) {
                err += dy;
                x1 += sx;
            }
            if (err2 <= dx) {
                err += dx;
                y1 += sy;
            }
        }
        drawPixel(x1, y1, color, z);
    }

    public void drawHorizontal(int x_start, int x_finish, int y, int color, float z) {
        if (x_start > x_finish){
            int tmp = x_start;
            x_start = x_finish;
            x_finish = tmp;
        }
        for (int i = x_start; i <= x_finish; i++){

            drawPixel(i, y, color, z);
        }
    }

    public void drawPixel(int x, int y, int color, float z) {
        if (x >= 0 & x < width && y >= 0 && y < height) {
            float normalizedZ = z / projection.far;
            float initialBuf = zBuffer[y * width + x];
            if (normalizedZ < initialBuf) {
                zBuffer[y * width + x] = normalizedZ;
                bufferedImage.setRGB(x, height - 1 - y, color);
            }
        }
    }

    public void clear() {
        bufferedImage.getGraphics().clearRect(0, 0, width, height);
        zBuffer = new float[width * height];
        for (int i = 0; i < width * height; i++) {
            zBuffer[i] = 100.0f;
        }
    }

    // lambert lighting
    public int getColorWithLight(Vector4f v1, Vector4f v2, Vector4f v3) {

        Vector4f side1 = new Vector4f(v2.x - v1.x, v2.y - v1.y, v2.z - v1.z, 0);
        Vector4f side2 = new Vector4f(v3.x - v1.x, v3.y - v1.y, v3.z - v1.z, 0);
        Vector4f triangleNormal = normalize(side1).cross(normalize(side2));
        Vector4f light = new Vector4f(-lightDirection.x, -lightDirection.y, -lightDirection.z, 0);
        float coeff = normalize(triangleNormal).dot(normalize(light));
        coeff = (coeff + 1.f) / 2.f;
        return colorOf((int)(150.f * coeff), (int)(150.f * coeff), (int)(150.f * coeff), 255);
    }
}
