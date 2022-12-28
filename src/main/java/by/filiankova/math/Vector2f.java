package by.filiankova.math;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Vector2f {
    public float x,y;
    public Vector2f plus(float value) {
        return new Vector2f(x + value, y + value);
    }
    public Vector2f minus(float value) {
        return new Vector2f(x - value, y - value);
    }

    public Vector2f mul(float scale) {
        return new Vector2f(x * scale, y * scale);
    }
}
