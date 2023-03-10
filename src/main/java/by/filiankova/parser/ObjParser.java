package by.filiankova.parser;

import by.filiankova.math.Vector2f;
import by.filiankova.math.Vector3f;
import by.filiankova.math.Vector3i;
import by.filiankova.math.Vector4f;
import lombok.SneakyThrows;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ObjParser {

    @SneakyThrows
    public ObjData parseFile(String filename) {
        List<String> lines = Files.readAllLines(Paths.get(filename));
        List<Vector3f> vertices = new ArrayList<>();
        List<Vector2f> textures = new ArrayList<>();
        List<Vector3f> normals = new ArrayList<>();
        List<Vector4f> vertexParams = new ArrayList<>();
        List<List<Vector3i>> surfaces = new ArrayList<>();

        for (String line : lines) {
            Scanner scanner = new Scanner(new StringReader(line));
            scanner.useDelimiter(" ");
            String symbol = scanner.next();
            if (symbol.charAt(0) != 'v' && !symbol.equals("vn") && !symbol.equals("vt") &&
                    symbol.charAt(0) != 'f' && !symbol.equals("vp")) {
                continue;
            }
            switch (symbol) {
                case "v": {
                    float[] f = {0, 0, 0};
                    int i = 0;
                    while (scanner.hasNext()) {
                        float value = Float.parseFloat(scanner.next());
                        f[i] = value;
                        i++;
                    }
                    vertices.add(new Vector3f(f[0], f[1], f[2]));
                    break;
                }
                case "vt": {
                    float[] f = {0, 0, 0};
                    int i = 0;
                    while (scanner.hasNext()) {
                        float value = Float.parseFloat(scanner.next());
                        f[i] = value;
                        i++;
                    }
                    textures.add(new Vector2f(f[0], f[1]));
                    break;
                }
                case "vn": {
                    float[] f = {0, 0, 0};
                    int i = 0;
                    while (scanner.hasNext()) {
                        float value = Float.parseFloat(scanner.next());
                        f[i] = value;
                        i++;
                    }
                    normals.add(new Vector3f(f[0], f[1], f[2]));
                    break;
                }
                case "vp": {
                    float[] f = {0, 0, 0};
                    int i = 0;
                    while (scanner.hasNext()) {
                        float value = Float.parseFloat(scanner.next());
                        f[i] = value;
                        i++;
                    }
                    vertexParams.add(new Vector4f(f[0], f[1], f[2]));
                    break;
                }
                case "f":
                    List<Vector3i> tmp = new ArrayList<>();
                    while (scanner.hasNext()) {
                        symbol = scanner.next();
                        int[] params = {Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE};
                        int ind = 0;
                        int number = 0;
                        boolean sign = false;
                        for (int i = 0; i < symbol.length(); i++) {
                            if (symbol.charAt(i) == '-') {
                                sign = true;
                            } else if (Character.isDigit(symbol.charAt(i))) {
                                number *= 10;
                                number += (symbol.charAt(i) - '0');
                            } else {
                                params[ind] = sign ? -number : number;
                                number = 0;
                                sign = false;
                                ind++;
                                while (i + 1 < symbol.length() && symbol.charAt(i + 1) == '/') {
                                    i++;
                                    ind++;
                                }
                            }
                        }
                        params[ind] = sign ? -number : number;
                        if (params[0] < 0) params[0] = vertices.size() + params[0] + 1;
                        if (params[1] < 0) params[1] = textures.size() + params[1] + 1;
                        if (params[2] < 0) params[2] = normals.size() + params[2] + 1;
                        tmp.add(new Vector3i(params[0] - 1, params[1] - 1, params[2] - 1));
                    }
                    surfaces.add(tmp);
                    break;
            }
        }
        return new ObjData(vertices, textures, normals, vertexParams, surfaces);
    }

}
