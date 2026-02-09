package com.mrstride.gui;

import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

public class Line extends Line2D.Double {

    public Line(int x1, int y1, int x2, int y2) {
        super(x1, y1, x2, y2);
    }

    public boolean linesIntersect(Line other) {
        return Line2D.linesIntersect(x1, y1, x2, y2, other.x1, other.y1, other.x2, other.y2);
    }

    public String toString() {
        return String.format("(%d, %d) - (%d, %d)", (int) x1, (int) y1, (int) x2, (int) y2);
    }

    public boolean intersectsRect(Rectangle2D rect) {
        return rect.intersectsLine(x1, y1, x2, y2);
    }

    public static Rectangle2D.Double getUnionRect(Rectangle before, Rectangle after) {
        // take a union of the before/after rectangles and put into
        // a Rectangle2D object
        int xMin = Math.min(before.x, after.x);
        int xMax = Math.max(before.x, after.x);
        int yMin = Math.min(before.y, after.y);
        int yMax = Math.max(before.y, after.y);
        int height = (yMax - yMin) + after.height;
        int width = (xMax - xMin) + after.width;
        return new Rectangle2D.Double(xMin, yMin, width, height);
    }

}
