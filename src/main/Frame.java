package main;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Frame extends JFrame {
    private static JFrame frame;
    private static Lock lock = new ReentrantLock();
    private static boolean hasLock = false;
    private static int ms = 0;
    private static int count = 0;
    private static JLabel info, info2;
    private static int ms2 = 33;

    private JTextField width, height;
    private PaintPanel paint;

    public Frame() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(true);
        setTitle("Gene Cells");

        paint = new PaintPanel();
        setLayout(new BorderLayout());
        add(paint, BorderLayout.CENTER);

        paint.addMouseMotionListener(new MouseAdapter() {

            @Override
            public void mouseDragged(MouseEvent e) {
                mouseEvent(e);
            }
        });

        paint.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                mouseEvent(e);
            }
        });

        JPanel side = new JPanel(new BorderLayout());

        JPanel instruments = new JPanel();
        instruments.setPreferredSize(new Dimension(210, 200));
        instruments.setLayout(new GridLayout(11,1));

        JButton reset = new JButton("Сброс");
        reset.setMinimumSize(new Dimension(100, 20));
        reset.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                lock.lock();
                try {
                    int x = Integer.valueOf(width.getText());
                    int y = Integer.valueOf(height.getText());
                    Cells.init(x, y);
                    paint.recalcSize();
                } catch (Exception ex) {
                    Cells.init(Cells.getWidth(), Cells.getHeight());
                }

                frame.repaint();
                count = 0;
                info.setText(String.format("<html> Итерация: %d<br>Количество клеток: %d<br>Время тика (мс): %d<html>", count++, Cells.queue.size(), 0));

                lock.unlock();
            }
        });
        JButton start = new JButton("Пауза");
        start.setMinimumSize(new Dimension(100, 20));
        start.addActionListener(new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (hasLock) {
                    lock.unlock();
                    hasLock = false;
                    start.setText("Пауза");
                } else {
                    lock.lock();
                    hasLock = true;
                    start.setText("Продолжить");
                }
            }
        });

        JSlider repaint = new JSlider(10, 200);
        repaint.setValue(ms2);
        repaint.setBorder(BorderFactory.createTitledBorder("FPS " + 1000 / repaint.getValue()));
        repaint.addChangeListener(e -> {
            ms = repaint.getValue();
            ((TitledBorder) repaint.getBorder()).setTitle("FPS " + 1000 / repaint.getValue());
        });

        JSlider step = new JSlider(0, 100);
        step.setValue(ms);
        step.setBorder(BorderFactory.createTitledBorder("Шаг симуляции " + step.getValue() + " мс"));
        step.addChangeListener(e -> {
            ms = step.getValue();
            ((TitledBorder) step.getBorder()).setTitle("Шаг симуляции " + step.getValue() + " мс");
        });

        JSlider light = new JSlider(20, 500);
        light.setValue(Cell.lightPower);
        light.setBorder(BorderFactory.createTitledBorder("Интенсивность света " + light.getValue()));
        light.addChangeListener(e -> {
            Cell.lightPower = light.getValue();
            ((TitledBorder) light.getBorder()).setTitle("Интенсивность света " + light.getValue());
            Cells.calcLightMap();
        });

        JSlider mut = new JSlider(0, 500);
        mut.setValue((int) (Cell.mutation * 1000));

        mut.setBorder(BorderFactory.createTitledBorder("Шанс мутации " + (float) mut.getValue() / 10 + "%"));
        mut.addChangeListener(e -> {
            Cell.mutation = (float) mut.getValue() / 1000;
            ((TitledBorder) mut.getBorder()).setTitle("Шанс мутации " + (float) mut.getValue() / 10 + "%");
        });

        JSlider peace = new JSlider(-1, 100);
        peace.setValue(Cell.peacefulness);
        peace.setBorder(BorderFactory.createTitledBorder("Миролюбивость " + peace.getValue()));
        peace.addChangeListener(e -> {
            Cell.peacefulness = peace.getValue();
            if (peace.getValue() < 0) {
                ((TitledBorder) peace.getBorder()).setTitle("КАЖДЫЙ САМ ЗА СЕБЯ!");
            } else {
                ((TitledBorder) peace.getBorder()).setTitle("Миролюбивость " + peace.getValue());
            }

        });

        JSlider energyLim = new JSlider(100, 2000);
        energyLim.setValue(Cell.energyLim);
        energyLim.setBorder(BorderFactory.createTitledBorder("Предел энергии " + energyLim.getValue()));
        energyLim.addChangeListener(e -> {
            Cell.energyLim = energyLim.getValue();
            ((TitledBorder) energyLim.getBorder()).setTitle("Предел энергии " + energyLim.getValue());
        });

        JSlider energyGap = new JSlider(0, 500);
        energyGap.setValue(Cell.energySptitDeathGap);
        energyGap.setBorder(BorderFactory.createTitledBorder("Зона размножения " + energyGap.getValue()));
        energyGap.addChangeListener(e -> {
            Cell.energySptitDeathGap = energyGap.getValue();
            ((TitledBorder) energyGap.getBorder()).setTitle("Зона размножения " + energyGap.getValue());
        });

        JSlider energyStep = new JSlider(2, 100);
        energyStep.setValue(Cell.energyStep);
        energyStep.setBorder(BorderFactory.createTitledBorder("Расход энергии " + energyStep.getValue()));
        energyStep.addChangeListener(e -> {
            Cell.energyStep = energyStep.getValue();
            ((TitledBorder) energyStep.getBorder()).setTitle("Расход энергии " + energyStep.getValue());
        });


        JSlider attackForce = new JSlider(0, 1000);
        attackForce.setValue(Cell.attackForce);
        attackForce.setBorder(BorderFactory.createTitledBorder("Сила атаки " + attackForce.getValue()));
        attackForce.addChangeListener(e -> {
            Cell.attackForce = attackForce.getValue();
            ((TitledBorder) attackForce.getBorder()).setTitle("Сила атаки " + attackForce.getValue());
        });

        JPanel size = new JPanel(new GridBagLayout());
        size.setBorder(BorderFactory.createTitledBorder("Соотношение сторон"));
        width = new JTextField(Cells.getWidth());
        width.setText(String.valueOf(Cells.getWidth()));
        height = new JTextField(Cells.getHeight());
        height.setText(String.valueOf(Cells.getHeight()));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        c.ipadx = 30;
        size.add(width, c);
        c.gridx = 1;
        size.add(height, c);
        c.gridy = 1;
        c.gridx = 1;
        c.ipadx = 0;
        size.add(reset, c);
        c.gridx = 0;
        size.add(start, c);

        info = new JLabel();
        info.setBorder(BorderFactory.createTitledBorder("Статистика:"));
        info2 = new JLabel("Выберите клетку",SwingConstants.CENTER);
        info2.setBorder(BorderFactory.createTitledBorder("Ген клетки:"));
        info2.setPreferredSize(new Dimension(200,120));


        instruments.add(size);
        instruments.add(repaint);
        instruments.add(step);
        instruments.add(light);
        instruments.add(mut);
        instruments.add(peace);
        instruments.add(energyLim);
        instruments.add(energyGap);
        instruments.add(energyStep);
        instruments.add(attackForce);
        instruments.add(info);

        side.add(instruments, BorderLayout.CENTER);
        side.add(info2, BorderLayout.SOUTH);


        add(side, BorderLayout.EAST);
        setPreferredSize(new Dimension(1000, 800));
        pack();

        setVisible(true);
    }

    private void mouseEvent(MouseEvent e) {
        int x = (int) (e.getX() / paint.getSizeX());
        int y = (int) (e.getY() / paint.getSizeY());
        Cell c = Cells.getCell(x, y);
        if (c != null) {
            StringBuilder s = new StringBuilder();
            s.append("<html><center><body color=#000000><font size=100 color=\"");
            s.append(String.format("#%02x%02x%02x\">", c.color.getRed(), c.color.getGreen(), c.color.getBlue()));
            s.append('\u25a0');
            s.append("</font> Поколение ").append(c.generation).append("<br>");
            s.append(c.mut1).append(" ").append(c.mut2).append(" ").append(c.mut3).append("<br>");
            for (int i = 0; i < c.acts.length; i++) {
                switch (c.acts[i]) {
                    case 0:
                        s.append("grow ");
                        break;
                    case 1:
                        s.append("look ");
                        break;
                    case 2:
                        s.append("rot+ ");
                        break;
                    case 3:
                        s.append("rot- ");
                        break;
                    case 4:
                        s.append("move ");
                        break;
                    case 5:
                        s.append("eat ");
                }
            }
            s.append("</html>");
            info2.setText(s.toString());
        }
    }

    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        Cells.init(500, 500);
        frame = new Frame();
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(ms);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                long start = System.currentTimeMillis();
                lock.lock();
                try {
                    Cells.DoTick();
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(1);
                }
                lock.unlock();
                info.setText(String.format("<html> Итерация: %d<br>Количество клеток: %d<br>Время итерации (мс): %d<html>", count++, Cells.queue.size(), System.currentTimeMillis() - start));

            }
        }).start();

        while (true) {
            try {
                Thread.sleep(ms2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            frame.getContentPane().repaint();
        }


    }
}
