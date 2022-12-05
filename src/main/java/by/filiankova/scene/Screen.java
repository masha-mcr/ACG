package by.filiankova.scene;

import by.filiankova.math.Matrix4f;
import by.filiankova.math.Vector3i;
import by.filiankova.math.Vector4f;
import lombok.Getter;

import java.awt.image.BufferedImage;
import java.util.List;

import static by.filiankova.math.Vector4f.normalize;
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

    public Screen(int width, int height) {
        this.width = width;
        this.height = height;
        bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        camera = new Camera(0.3f, new Vector4f(0, 0, 0), new Vector4f(0, 0, 1), new Vector4f(0, 1, 0));
        projection = new Projection(65, 1.33f, 0, 100);
        zBuffer = new float[width * height];
    }

    public void drawModel(Model model) {
        Matrix4f modelMatr = model.getModel();
        Matrix4f prjMatrix = projection.getProjectionMatrix();
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f mvp = modelMatr.multiply(viewMatrix).multiply(prjMatrix);

        List<Vector4f> vertices = model.getVertices();
        for (List<Vector3i> face : model.getFaces()) {
            int verticesPerFace = face.size();
            for (int i = 1; i < verticesPerFace - 1; i++) {
                Vector4f v1 = vertices.get(face.get(0).x);
                Vector4f v2 = vertices.get(face.get(i).x);
                Vector4f v3 = vertices.get(face.get(i + 1).x);
                Vector4f v1mvp = mvp.multiply(v1);
                Vector4f v2mvp = mvp.multiply(v2);
                Vector4f v3mvp = mvp.multiply(v3);
                Vector4f v1model = modelMatr.multiply(v1);
                Vector4f v2model = modelMatr.multiply(v2);
                Vector4f v3model = modelMatr.multiply(v3);
                if (!isBackface(v1mvp,v2mvp,v3mvp)){
                    drawTriangle(v1mvp, v2mvp, v3mvp, getColorWithLight(v1model, v2model, v3model));
                }
            }
        }
    }

    private boolean isBackface(Vector4f v1, Vector4f v2, Vector4f v3) {
        Vector4f side1 = v2.minus(v1);
        Vector4f side2 = v3.minus(v1);
        double cos = side1.x * side2.y - side1.y * side2.x;
        return cos < 0;
    }


    private int bound(int max, int v) {
        return Math.max(min(v, max), 0);
    }

    public void drawTriangle(Vector4f v1, Vector4f v2, Vector4f v3, int color) {

        if (!(v1.z > 0 && v2.z > 0 && v3.z > 0)) {
            return;
        }

        int x1 = (int) (v1.x / v1.w * (float) width / 2.f) + width / 2;
        int x2 = (int) (v2.x / v2.w * (float) width / 2.f) + width / 2;
        int x3 = (int) (v3.x / v3.w * (float) width / 2.f) + width / 2;

        int y1 = (int) (v1.y / v1.w * (float) height / 2.f) + height / 2;
        int y2 = (int) (v2.y / v2.w * (float) height / 2.f) + height / 2;
        int y3 = (int) (v3.y / v3.w * (float) height / 2.f) + height / 2;

        float avgZ = (v1.z + v2.z + v3.z) / 3.f;

        if (y1 > y2) {
            int tmp = y1;
            y1 = y2;
            y2 = tmp;
            tmp = x1;
            x1 = x2;
            x2 = tmp;
        }
        if (y1 > y3) {
            int tmp = y1;
            y1 = y3;
            y3 = tmp;
            tmp = x1;
            x1 = x3;
            x3 = tmp;
        }
        if (y2 > y3) {
            int tmp = y2;
            y2 = y3;
            y3 = tmp;
            tmp = x2;
            x2 = x3;
            x3 = tmp;
        }


        float slope21 = y2 == y1 ? 0 : (float)(x2-x1)/(y2-y1);
        float slope31 = y3 == y1 ? 0 : (float)(x3-x1)/(y3-y1);
        float slope32 = y3 == y2 ? 0 : (float)(x3-x2)/(y3-y2);

        float x_start = bound(x1, width);
        float x_finish = bound(x1, width);

        if(y1 != y2){
            for (int i = bound(y1, height); i < bound(y2, height); i++) {
                if (isInViewport((int) ceil(x_start), width) & isInViewport((int) ceil(x_finish), width) & isInViewport(i, height)) {
                    drawHorizontal((int) ceil(x_start), (int) ceil(x_finish), i, color, avgZ);
                }
                x_start += slope21;
                x_finish += slope31;
            }
        }
        else{
            x_start = bound(x2, width);;
            x_finish = bound(x1, width);;
        }

        for (int i = bound(y2, height); i < bound(y3, height); i++) {
            if (isInViewport((int) ceil(x_start), width) & isInViewport((int) ceil(x_finish), width) & isInViewport(i, height)) {
                drawHorizontal((int) ceil(x_start), (int) ceil(x_finish), i, color, avgZ);
            }
            x_start += slope32;
            x_finish += slope31;
        }

        drawLine(x1, x2, y1, y2, color, avgZ);
        drawLine(x2, x3, y2, y3, color, avgZ);
        drawLine(x3, x1, y3, y1, color, avgZ);
    }

    public static int colorOf(int r, int g, int b, int a) {
        return ((a & 0xff) << 24) | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
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
            zBuffer[i] = 1.0f;
        }
    }

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
