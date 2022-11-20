package by.filiankova;

import by.filiankova.scene.MainWindow;


public class Main {

    public static int WIDTH = 800;
    public static int HEIGHT = 600;

    public static void main(String[] args) {
        MainWindow t = new MainWindow(WIDTH, HEIGHT);
        t.start();
    }
}
