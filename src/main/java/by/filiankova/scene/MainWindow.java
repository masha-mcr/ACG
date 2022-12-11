package by.filiankova.scene;

import by.filiankova.math.Matrix4f;
import by.filiankova.math.Vector4f;
import by.filiankova.parser.ObjData;
import by.filiankova.parser.ObjParser;
import by.filiankova.scene.listener.CameraMouseListener;
import by.filiankova.scene.listener.CameraKeyboardListener;
import by.filiankova.scene.listener.ModelKeyboardListener;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;



public class MainWindow {
    private JFrame frame;
    private final int width, height;
    private Screen screen;

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
        screen = new Screen(width, height);
    }
    public void start(){
        frame.createBufferStrategy(1);
        BufferStrategy bs = frame.getBufferStrategy();
        Graphics g = bs.getDrawGraphics();

        ObjParser parser = new ObjParser();
        ObjData objData = parser.parseFile("src/main/resources/cube.obj");


        Model model = new Model(
                Matrix4f.translation(new Vector4f(0, 0, 10)),
                Matrix4f.rotation(new Vector4f(0, 0, 0)),
                Matrix4f.scale(new Vector4f(1.0f, 1.0f, 1.0f)),
                objData.getVertices(),
                objData.getSurfaces(),
                objData.getNormals());


        frame.addKeyListener(new CameraKeyboardListener(screen.getCamera()));
        CameraMouseListener cameraMouseListener = new CameraMouseListener(screen.getCamera());
        frame.addMouseListener(cameraMouseListener);
        frame.addMouseMotionListener(cameraMouseListener);
        frame.addKeyListener(new ModelKeyboardListener(model));

        while (true) {
            screen.clear();
            screen.drawModel(model);
            g.drawImage(screen.getBufferedImage(), 0, 0, width, height, null);
            bs.show();
        }
    }
}
