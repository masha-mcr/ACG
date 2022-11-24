package by.filiankova.scene.listener;

import by.filiankova.scene.Camera;
import lombok.RequiredArgsConstructor;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

@RequiredArgsConstructor
public class CameraMouseListener implements MouseListener, MouseMotionListener {
    private final Camera camera;
    private int prevX = 500;
    private int prevY = 400;
    private boolean isMoving;

    @Override
    public void mouseDragged(MouseEvent e) {
        int dx = e.getX() - prevX;
        int dy = e.getY() - prevY;
        prevX += dx;
        prevY += dy;
        camera.setPitch(camera.getPitch() - dy / 5.f);
        camera.setYaw(camera.getYaw() + dx / 5.f);
    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        prevX = e.getX();
        prevY = e.getY();
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
