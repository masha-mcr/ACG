package by.filiankova.scene;

import by.filiankova.math.Matrix4f;
import by.filiankova.math.Vector3i;
import by.filiankova.math.Vector4f;
import lombok.Getter;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static by.filiankova.math.Vector4f.normalize;
import static java.lang.Math.*;

public class Screen {
    private final int width;
    private final int height;

    @Getter
    private final Camera camera;
    private final Projection projection;

    @Getter
    private final BufferedImage bufferedImage;

    public Screen(int width, int height) {
        this.width = width;
        this.height = height;
        bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        camera = new Camera(0.3f, new Vector4f(0, 0, 0), new Vector4f(0, 0, 1), new Vector4f(0, 1, 0));
        projection = new Projection(65, 1.33f, 0, 100);
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
                Vector4f v1 = mvp.multiply(vertices.get(face.get(0).x));
                Vector4f v2 = mvp.multiply(vertices.get(face.get(i).x));
                Vector4f v3 = mvp.multiply(vertices.get(face.get(i + 1).x));
                if (!isBackface(v1,v2,v3)){
                    drawTriangle(v1, v2, v3);
                }
            }
        }
    }

    private boolean isBackface(Vector4f v1, Vector4f v2, Vector4f v3) {
        Vector4f side1 = v2.minus(v1);
        Vector4f side2 = v3.minus(v1);
        Vector4f triangleNormal = normalize(side1).cross(normalize(side2));
        Vector4f gaze = camera.getTarget().minus(camera.getEye());
        gaze.w = 0;
        return triangleNormal.dot(normalize(gaze)) < cos(Math.PI / 180.f * 120.f);
    }

    public void drawTriangle(Vector4f v1, Vector4f v2, Vector4f v3) {

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


        int color = colorOf(0, 255, 0, 255);

        Map<Integer, Integer> side1 = getLineCoords(x1, x2, y1, y2);
        Map<Integer, Integer> side2 = getLineCoords(x1, x3, y1, y3);
        Map<Integer, Integer> side3 = getLineCoords(x2, x3, y2, y3);

        for (int i = bound(height, y1); i <= bound(height, y2); i++) {
            if (side1.containsKey(i) && side2.containsKey(i)) {
                drawLine(bound(width, side1.get(i)), bound(width, side2.get(i)), i, i, color, avgZ);
            }
        }

        for (int i = bound(height, y2); i <= bound(height, y3); i++) {
            if (side2.containsKey(i) && side3.containsKey(i)) {
                drawLine(bound(width, side3.get(i)), bound(width, side2.get(i)), i, i, color, avgZ);
            }
        }

        drawLine(x1, x2, y1, y2, color, avgZ);
        drawLine(x2, x3, y2, y3, color, avgZ);
        drawLine(x3, x1, y3, y1, color, avgZ);
    }

    public static int colorOf(int r, int g, int b, int a) {
        return ((a & 0xff) << 24) | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
    }

    private Map<Integer, Integer> getLineCoords(int x1, int x2, int y1, int y2) {
        Map<Integer, Integer> lineCoord = new HashMap<>();
        int deltaX = abs(x2 - x1);
        int deltaY = -abs(y2 - y1);
        int signX = x1 < x2 ? 1 : -1;
        int signY = y1 < y2 ? 1 : -1;

        int err = deltaX + deltaY;
        lineCoord.putIfAbsent(y1, x1);
        while (x1 != x2 || y1 != y2) {
            lineCoord.putIfAbsent(y1, x1);
            int err2 = err * 2; // -8
            if (err2 >= deltaY) {
                err += deltaY;
                x1 += signX;
            }
            if (err2 <= deltaX) {
                err += deltaX;
                y1 += signY;
            }
        }
        lineCoord.putIfAbsent(y1, x1);
        return lineCoord;
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

    public void drawPixel(int x, int y, int color, float z) {
        if (x >= 0 & x < width && y >= 0 && y < height)
            bufferedImage.setRGB(x, height - 1 - y, color);
    }

    private int bound(int max, int v) {
        return Math.max(min(v, max), 0);
    }

    public void clear() {
        bufferedImage.getGraphics().clearRect(0, 0, width, height);
    }
}
