import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import javax.swing.*;

public class DFAPanel extends SimPanel {
    int n, cur, idx;
    String alpha = "01", w = "";
    int[][] d;         
    boolean[] acc;
    final JComboBox<String> preset = new JComboBox<>(new String[]{
            "Cantidad par de 1s", "Termina en 01", "Contiene la subcadena (parámetro)"});
    final JTextField pat = new JTextField("abb", 8);
    final JLabel desc = new JLabel();

    public DFAPanel() {
        JPanel west = new JPanel();
        west.setLayout(new BoxLayout(west, BoxLayout.Y_AXIS));
        west.setBorder(BorderFactory.createTitledBorder("Lenguaje regular"));
        west.setPreferredSize(new Dimension(230, 0));
        preset.setMaximumSize(new Dimension(220, 28));
        pat.setMaximumSize(new Dimension(220, 28));
        JButton ap = new JButton("Aplicar parámetro");
        ap.addActionListener(e -> { configure(); });
        preset.addActionListener(e -> configure());
        west.add(preset);
        west.add(Box.createVerticalStrut(8));
        west.add(new JLabel("Subcadena (solo opción 3):"));
        west.add(pat);
        west.add(ap);
        west.add(Box.createVerticalStrut(10));
        west.add(desc);
        configure();
        build(canvas(), west);
    }

    private void configure() {
        int p = preset.getSelectedIndex();
        if (p == 0) {
            alpha = "01"; n = 2;
            d = new int[][]{{0, 1}, {1, 0}};
            acc = new boolean[]{true, false};
            desc.setText("<html>L = { w ∈ {0,1}* : |w|₁ es par }<br>M = (Q, Σ, δ, q0, F)<br>q0 = par, q1 = impar</html>");
            examples("1010 | acepta (dos 1s)", "111 | rechaza", "ε | acepta", "0000 | acepta", "10101 | rechaza");
        } else if (p == 1) {
            alpha = "01"; n = 3;
            d = new int[][]{{1, 0}, {1, 2}, {1, 0}};
            acc = new boolean[]{false, false, true};
            desc.setText("<html>L = { w ∈ {0,1}* : w termina en 01 }<br>q0 = inicio, q1 = último 0,<br>q2 = últimos símbolos 01</html>");
            examples("1101 | acepta", "01 | acepta", "10 | rechaza", "0100 | rechaza", "ε | rechaza");
        } else {
            String s = pat.getText().trim();
            if (s.isEmpty()) { s = "ab"; pat.setText(s); }
            StringBuilder a = new StringBuilder();
            for (char c : s.toCharArray()) if (a.indexOf(c + "") < 0) a.append(c);
            if (a.length() < 2) a.append(a.charAt(0) == 'x' ? 'y' : 'x');
            alpha = a.toString();
            int m = s.length();
            n = m + 1;
            d = new int[n][alpha.length()];
            acc = new boolean[n];
            acc[m] = true;
            for (int k = 0; k <= m; k++)
                for (int c = 0; c < alpha.length(); c++) {
                    if (k == m) { d[k][c] = m; continue; }
                    String t = s.substring(0, k) + alpha.charAt(c);
                    int l = Math.min(m, t.length());
                    while (l > 0 && !t.endsWith(s.substring(0, l))) l--;
                    d[k][c] = l;
                }
            desc.setText("<html>L = { w ∈ {" + alpha + "}* : w contiene \"" + s + "\" }<br>Autómata tipo KMP, " + n + " estados.</html>");
            char o = alpha.charAt(0);
            examples(s + " | acepta", o + s + o + " | acepta", (m > 1 ? s.substring(0, m - 1) : "ε") + " | rechaza");
        }
    }

    @Override protected void load(String s) {
        w = s; cur = 0; idx = 0;
        log("Estado inicial: q0. Entrada: " + (s.isEmpty() ? "ε" : s));
    }

    @Override protected boolean step() {
        if (idx >= w.length()) { end(acc[cur], "la cadena se consumió en q" + cur + (acc[cur] ? " (estado final)" : " (no es final)")); return false; }
        char c = w.charAt(idx);
        int ci = alpha.indexOf(c);
        if (ci < 0) { end(false, "el símbolo '" + c + "' no pertenece al alfabeto {" + alpha + "}"); return false; }
        int nx = d[cur][ci];
        log("δ(q" + cur + ", " + c + ") = q" + nx);
        cur = nx; idx++;
        status.setText("Leyendo posición " + idx + "/" + w.length() + " — estado actual: q" + cur);
        if (idx == w.length()) { end(acc[cur], "la cadena se consumió en q" + cur + (acc[cur] ? " (estado final)" : " (no es final)")); return false; }
        return true;
    }

    private JComponent canvas() {
        return new JPanel() {
            @Override protected void paintComponent(Graphics g0) {
                super.paintComponent(g0);
                Graphics2D g = (Graphics2D) g0;
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int W = getWidth(), H = getHeight();
                g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
                g.drawString("Cinta de entrada (gris = consumido, rojo ▼ = símbolo siguiente)", 20, 22);
                String[] cells = new String[w.length()];
                for (int i = 0; i < cells.length; i++) cells[i] = w.charAt(i) + "";
                tape(g, cells, idx, idx, false, 20, 44, W);
                double cx = W / 2.0, cy = (H + 90) / 2.0;
                double R = n == 1 ? 0 : Math.max(60, Math.min(W / 2.0 - 90, (H - 160) / 2.0));
                double[] px = new double[n], py = new double[n];
                for (int i = 0; i < n; i++) {
                    double a = Math.PI + 2 * Math.PI * i / n;
                    px[i] = cx + R * Math.cos(a);
                    py[i] = cy + R * Math.sin(a);
                }
                double r = 24;
                Map<String, StringBuilder> edges = new TreeMap<>();
                for (int i = 0; i < n; i++)
                    for (int c = 0; c < alpha.length(); c++)
                        edges.computeIfAbsent(i + ">" + d[i][c], k -> new StringBuilder()).append(alpha.charAt(c)).append(',');
                g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
                for (Map.Entry<String, StringBuilder> e : edges.entrySet()) {
                    String[] ij = e.getKey().split(">");
                    int i = Integer.parseInt(ij[0]), j = Integer.parseInt(ij[1]);
                    String lab = e.getValue().substring(0, e.getValue().length() - 1);
                    g.setColor(Color.DARK_GRAY);
                    if (i == j) {
                        double ux = R == 0 ? 0 : (px[i] - cx) / R, uy = R == 0 ? -1 : (py[i] - cy) / R;
                        g.draw(new Ellipse2D.Double(px[i] + ux * (r + 8) - 11, py[i] + uy * (r + 8) - 11, 22, 22));
                        g.setColor(new Color(0, 70, 160));
                        center(g, lab, px[i] + ux * (r + 32), py[i] + uy * (r + 32));
                    } else {
                        double dx = px[j] - px[i], dy = py[j] - py[i], len = Math.hypot(dx, dy);
                        double qx = (px[i] + px[j]) / 2 - dy / len * 30, qy = (py[i] + py[j]) / 2 + dx / len * 30;
                        double a0 = Math.atan2(qy - py[i], qx - px[i]), a1 = Math.atan2(py[j] - qy, px[j] - qx);
                        double sx = px[i] + r * Math.cos(a0), sy = py[i] + r * Math.sin(a0);
                        double ex = px[j] - r * Math.cos(a1), ey = py[j] - r * Math.sin(a1);
                        g.draw(new QuadCurve2D.Double(sx, sy, qx, qy, ex, ey));
                        head(g, qx, qy, ex, ey);
                        g.setColor(new Color(0, 70, 160));
                        center(g, lab, 0.25 * sx + 0.5 * qx + 0.25 * ex, 0.25 * sy + 0.5 * qy + 0.25 * ey - 8);
                    }
                }
                for (int i = 0; i < n; i++) {
                    Ellipse2D.Double c = new Ellipse2D.Double(px[i] - r, py[i] - r, 2 * r, 2 * r);
                    g.setColor(i == cur ? new Color(255, 215, 70) : Color.WHITE);
                    g.fill(c);
                    g.setColor(Color.BLACK);
                    g.setStroke(new BasicStroke(i == cur ? 3f : 1.5f));
                    g.draw(c);
                    g.setStroke(new BasicStroke(1.5f));
                    if (acc[i]) g.draw(new Ellipse2D.Double(px[i] - r + 5, py[i] - r + 5, 2 * r - 10, 2 * r - 10));
                    center(g, "q" + i, px[i], py[i]);
                }
                arrow(g, px[0] - r - 32, py[0], px[0] - r, py[0]);
                g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
                g.drawString("Estado actual: q" + cur + (acc[cur] ? "  (final)" : ""), 20, H - 12);
            }
        };
    }
}
