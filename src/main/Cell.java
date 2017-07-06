package main;

import java.awt.*;
import java.util.Arrays;


class Cell {

    private byte dir;
    private boolean alive = true;
    int energy, x, y;
    byte[] acts;
    int generation;
    Color color;
    int mut1 = 0, mut2 = 0, mut3 = 0;


    static byte[][] dirs = new byte[][]{{0, 1}, {1, 0}, {1, 1}, {0, -1}, {-1, 0}, {-1, -1}, {1, -1}, {-1, 1}};
    static int peacefulness = 10;
    static float mutation = .1f;
    static int lightPower = 150;
    static int energyStep = 10;
    static int energySptitDeathGap = 100;
    static int energyLim = 1000;
    static int attackForce = 400;


    Cell(int x, int y) {
        this.x = x;
        this.y = y;
        energy = 50;
        dir = (byte) (Math.random() * 8);
        acts = new byte[]{0};
        color = calcColor();
        generation = 1;
    }

    Cell(Cell parent, int x, int y) {
        this.x = x;
        this.y = y;
        energy = parent.energy;
        mut1 = parent.mut1;
        mut2 = parent.mut2;
        mut3 = parent.mut3;
        double r = Math.random();
        dir = (byte) (r * 8);
        generation = parent.generation;
        if (Math.random() < mutation) {
            generation = parent.generation + 1;
            if (r > .666666 && parent.acts.length < 33) {
                acts = new byte[parent.acts.length + 1];
                System.arraycopy(parent.acts, 0, acts, 0, parent.acts.length);
                acts[parent.acts.length] = (byte) (Math.random() * 6);
                mut1++;
            } else {
                if (r > .333333 && parent.acts.length > 1) {
                    acts = new byte[parent.acts.length - 1];
                    System.arraycopy(parent.acts, 0, acts, 0, parent.acts.length - 1);
                    mut2++;
                } else {
                    acts = Arrays.copyOf(parent.acts, parent.acts.length);
                    acts[(int) (Math.random() * acts.length)] = (byte) (Math.random() * 6);
                    mut3++;
                }
            }
            color = calcColor();
        } else {
            acts = Arrays.copyOf(parent.acts, parent.acts.length);
            color = parent.color;

        }


    }

    private Color calcColor() {
        int r, g, b;


        r = (255 & (mut1 * 33));
        g = (255 & (255 - mut2 * 33));
        b = (255 & (mut3 * 33));

        return new Color(r, g, b);
    }

    boolean act() {     // false - dead cell
        boolean grow = false, move = false;
        if (energy > 0) {
            for (int i = 0; i < acts.length; i++) {
                energy -= energyStep;
                switch (acts[i]) {
                    case 0:
                        if (!grow) {
                            energy += Cells.lightMap[x][y];  // move
                            grow = true;
                        }
                        break;
                    case 1:
                        i += observe();
                        break;
                    case 2:
                        if (++dir > 7) dir = 0;
                        break;
                    case 3:
                        if (--dir < 0) dir = 7;
                        break;
                    case 4:
                        if (!move) {
                            if (move()) {
                                return false;
                            }
                            move = true;
                        }
                        break;
                    case 5:
                        eat();
                }
            }
        }


        if (energy >= energyLim - energySptitDeathGap) {
            split();
        }
        if (energy > energyLim) {
            kill();
            return false;
        }
        if (energy <= 0) {
            erase();
            return false;
        }

        return true;
    }

    private void split() {
        for (int i = 0; i < 8; i++) {
            int j = (i + dir) % 8;
            int xx = x + dirs[j][0];
            int yy = y + dirs[j][1];
            if (Cells.check(xx, yy)) {
                if (!Cells.hasCell(xx, yy)) {
                    energy = energy / 2;
                    Cells.setCell(xx, yy, new Cell(this, xx, yy));
                    Cells.queue.add(Cells.cells[xx][yy]);
                    return;
                }

            }
        }
    }

    private int observe() {   // 1 - bad, 0 - good
        int xx = dirs[dir][0] + x, yy = dirs[dir][1] + y;

        if (xx < 0 || xx >= Cells.getWidth() || yy < 0 || yy >= Cells.getHeight())
            return 1;

        else if (Cells.hasCell(xx, yy)) {
            Cell c = Cells.getCell(xx, yy);
            if (!c.isAlive()) {
                return 0;
            } else {
                if (checkDifference(c)) {
                    if (checkStrength(c)) {
                        return 0;
                    } else {
                        return 1;
                    }
                } else {
                    return 1;
                }
            }
        } else {
            if (Cells.lightMap[x][y] > Cells.lightMap[xx][yy] || Cells.lightMap[xx][yy] == 0) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    private void eat() {

        int eatX = dirs[dir][0] + this.x;
        int eatY = dirs[dir][1] + this.y;
        if (eatX < 0 || eatX >= Cells.getWidth() ||
                eatY < 0 || eatY >= Cells.getHeight())
            return;

        if (Cells.hasCell(eatX, eatY)) {
            Cell c = Cells.getCell(eatX, eatY);
            if (!c.isAlive()) {
                energy += c.energy;
                c.erase();
            }
        }
    }

    private boolean move() {         // if true cell is dead

        int newX = dirs[dir][0] + this.x;
        int newY = dirs[dir][1] + this.y;
        if (newX < 0 || newX >= Cells.getWidth() ||
                newY < 0 || newY >= Cells.getHeight())
            return false;
        if (Cells.hasCell(newX, newY)) {
            Cell c = Cells.getCell(newX, newY);
            if (c.isAlive()) {
                if (checkDifference(c)) {
                    if (checkStrength(c)) {
                        if (c.energy > attackForce) {
                            c.energy -= attackForce;
                            energy += attackForce;
                        } else {
                            energy += c.energy;
                            c.erase();
                        }
                    } else {
                        if (energy > attackForce) {
                            energy -= attackForce;
                            c.energy += attackForce;
                        } else {
                            c.energy += energy;
                            erase();
                            return true;
                        }
                    }

                }
            } else {
                energy += attackForce;
                eat(newX, newY);
            }
        } else {
            step(newX, newY);
        }
        return false;
    }

    private boolean checkDifference(Cell c) {   // true is enemy
        return Math.abs(mut1 - c.mut1) + Math.abs(mut2 - c.mut2) + Math.abs(mut3 - c.mut3) > peacefulness;
    }

    private boolean checkStrength(Cell c) {   // true - c is weaker
        return acts.length + .05 * energy > c.acts.length + .05 * c.energy;
    }

    private void step(int x, int y) {
        Cells.setCell(x, y, this);
        Cells.deleteCell(this.x, this.y);
        this.x = x;
        this.y = y;
    }

    private void eat(int x, int y) {
        Cells.setCell(x, y, null);
    }

    boolean isAlive() {
        return alive;
    }

    private Cell kill() {
        alive = false;
        color = Color.GRAY;
        return this;
    }

    private void erase() {
        Cells.deleteCell(x, y);
    }

}
