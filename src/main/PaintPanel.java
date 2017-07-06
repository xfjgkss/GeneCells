package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;


public class PaintPanel extends JPanel  {

    double sizeX = 1;

    double sizeY = 1;

    PaintPanel() {
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                recalcSize();
            }
        });
    }

    public void recalcSize() {
        sizeX = ((double) getWidth() / Cells.getWidth());
        sizeY = ((double) getHeight() / Cells.getHeight());
    }

    public double getSizeX() {
        return sizeX;
    }

    public double getSizeY() {
        return sizeY;
    }

    @Override
    public void paintComponent(Graphics g) {
        g.setColor(Color.black);
        g.fillRect(0, 0, getWidth(), getHeight());

        for (int i = 0; i < Cells.getWidth(); i++) {
            int x = (int) (i * sizeX);
            for (int j = 0; j < Cells.getHeight(); j++) {
                if (Cells.hasCell(i, j)) {
                    try {
                        g.setColor(Cells.getCell(i, j).color);
                        g.fillRect(x, (int) (j * sizeY), (int) sizeX + 1, (int) sizeY + 1);
                    } catch (Exception ignored) {
                    }
                }
            }
        }
    }


}
