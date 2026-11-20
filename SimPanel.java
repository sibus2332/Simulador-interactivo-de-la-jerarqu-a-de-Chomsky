import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.geom.*;

public abstract class SimPanel extends JPanel {
    protected final JTextField in = new JTextField(14);
    protected final JComboBox<String> ex = new JComboBox<>();
    protected final JTextArea log = new JTextArea(7, 40);
    protected final JLabel status = new JLabel("Listo");
    protected final javax.swing.Timer timer = new javax.swing.Timer(600, e -> {
        if (!doStep()) ((javax.swing.Timer) e.getSource()).stop();
    });
    protected boolean finished;

    protected abstract void load(String input);

    protected abstract boolean step();

    protected void build(JComponent canvas, JComponent west) {
        setLayout(new BorderLayout(6, 6));
        setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        top.add(new JLabel("Cadena:"));
        top.add(in);
        top.add(new JLabel("Ejemplos:"));
        top.add(ex);
        top.add(btn("Reiniciar", e -> reset()));
        top.add(btn("Paso", e -> { timer.stop(); doStep(); }));
        top.add(btn("Ejecutar", e -> { if (finished) reset(); timer.start(); }));
        top.add(btn("Pausa", e -> timer.stop()));
        JSlider sp = new JSlider(1, 10, 5);
        sp.addChangeListener(e -> timer.setDelay(1100 - sp.getValue() * 100));
        top.add(new JLabel("Velocidad:"));
        top.add(sp);
        in.addActionListener(e -> reset());
        ex.addActionListener(e -> {
            String s = (String) ex.getSelectedItem();
            if (s == null) return;
            s = s.split(" \\| ")[0].trim();
            in.setText(s.equals("ε") ? "" : s);
            reset();
        });
        log.setEditable(false);
        status.setFont(status.getFont().deriveFont(Font.BOLD, 15f));
        JPanel south = new JPanel(new BorderLayout());
        south.add(status, BorderLayout.NORTH);
        south.add(new JScrollPane(log), BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);
        add(canvas, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);
        if (west != null) add(west, BorderLayout.WEST);
        if (ex.getItemCount() > 0) { ex.setSelectedIndex(-1); ex.setSelectedIndex(0); } else reset();
    }

    
    protected void examples(String... items) {
        ex.removeAllItems();
        for (String s : items) ex.addItem(s);
        ex.setSelectedIndex(-1);
        if (items.length > 0) ex.setSelectedIndex(0);
    }

    protected void reset() {
        timer.stop();
        finished = false;
        log.setText("");
        status.setText("Listo");
        status.setForeground(Color.BLACK);
        load(in.getText().trim());
        repaint();
    }

    protected boolean doStep() {
        if (finished) return false;
        boolean c = step();
        repaint();
        return c && !finished;
    }

    protected void log(String s) {
        log.append(s + "\n");
        log.setCaretPosition(log.getDocument().getLength());
    }

    protected void end(boolean ok, String msg) {
        finished = true;
        status.setText((ok ? "✔ ACEPTADA: " : "✘ RECHAZADA: ") + msg);
        status.setForeground(ok ? new Color(0, 130, 0) : new Color(190, 0, 0));
        log(">> " + msg);
    }

    private static JButton btn(String t, ActionListener l) {
        JButton b = new JButton(t);
        b.addActionListener(l);
        return b;
    }

    static void head(Graphics2D g, double x1, double y1, double x2, double y2) {
        double a = Math.atan2(y2 - y1, x2 - x1);
        Path2D p = new Path2D.Double();
        p.moveTo(x2, y2);
        p.lineTo(x2 - 10 * Math.cos(a - 0.4), y2 - 10 * Math.sin(a - 0.4));
        p.lineTo(x2 - 10 * Math.cos(a + 0.4), y2 - 10 * Math.sin(a + 0.4));
        p.closePath();
        g.fill(p);
    }

    static void arrow(Graphics2D g, double x1, double y1, double x2, double y2) {
        g.draw(new Line2D.Double(x1, y1, x2, y2));
        head(g, x1, y1, x2, y2);
    }

    static void center(Graphics2D g, String s, double cx, double cy) {
        FontMetrics m = g.getFontMetrics();
        g.drawString(s, (float) (cx - m.stringWidth(s) / 2.0), (float) (cy + m.getAscent() / 2.0 - 2));
    }

    
    static void tape(Graphics2D g, String[] cells, int head, int doneBefore, boolean inf, int x, int y, int W) {
        int cw = 38, n = Math.max(3, (W - 2 * x) / cw);
        int first = Math.max(0, inf ? head - n / 2 : Math.min(head - n / 2, cells.length - n));
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 18));
        for (int i = 0; i < n; i++) {
            int k = first + i;
            if (!inf && k >= cells.length) break;
            String s = k < cells.length ? cells[k] : "_";
            Rectangle2D r = new Rectangle2D.Double(x + i * cw, y, cw, cw);
            g.setColor(k == head ? new Color(255, 225, 90) : k < doneBefore ? new Color(220, 220, 220) : Color.WHITE);
            g.fill(r);
            g.setColor(Color.DARK_GRAY);
            g.draw(r);
            center(g, s, x + i * cw + cw / 2.0, y + cw / 2.0);
        }
        int h = head - first;
        if (h >= 0 && h < n) {
            double cx = x + h * cw + cw / 2.0;
            g.setColor(new Color(200, 30, 30));
            Path2D p = new Path2D.Double();
            p.moveTo(cx, y - 2);
            p.lineTo(cx - 8, y - 14);
            p.lineTo(cx + 8, y - 14);
            p.closePath();
            g.fill(p);
        }
        g.setColor(Color.BLACK);
    }
}
