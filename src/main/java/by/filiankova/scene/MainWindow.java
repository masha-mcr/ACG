package by.filiankova.scene;

import by.filiankova.math.Vector3i;
import by.filiankova.math.Vector4f;
import by.filiankova.parser.ObjData;
import by.filiankova.parser.ObjParser;

import javax.swing.*;


public class MainWindow {
    private JFrame frame;
    private final int width, height;
    //private Screen screen;

    private JFrame initFrame() {
        frame = new JFrame("WINDOW");
        frame.setVisible(true);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(width, height);
        return frame;
    }
    public MainWindow(int width, int height) {
        this.width = width;
        this.height = height;
        frame = initFrame();
        //screen = new Screen(width, height);
        System.out.println("window created");
    }
    public void start(){
        ObjParser parser = new ObjParser();
        ObjData objData = parser.parseFile("src/main/resources/cube.obj");
        for (Vector4f vertex : objData.getVertices())
            System.out.println(vertex.x + " " + vertex.y + " " + vertex.z + " " + vertex.w);
        for (var face : objData.getSurfaces())
            for (Vector3i f: face)
                System.out.println(f.x + " " + f.y + " " + f.z);
    }
}
