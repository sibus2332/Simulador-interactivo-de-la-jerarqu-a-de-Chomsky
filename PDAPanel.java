import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;


public class PDAPanel extends SimPanel {
    
    List<String[]> rules = new ArrayList<>();
    StringBuilder st = new StringBuilder();   // el tope es el último carácter
    String cur = "q0", w = "", last = "";
    int idx;
    final JComboBox<String> preset = new JComboBox<>(new String[]{
            "aⁿbⁿ (n ≥ 0)", "w c wᴿ, w ∈ {a,b}*", "Paréntesis balanceados"});
    final JTextArea rulesView = new JTextArea();

    public PDAPanel() {
        JPanel west = new JPanel(new BorderLayout(4, 4));
        west.setBorder(BorderFactory.createTitledBorder("Lenguaje y reglas δ"));
        west.setPreferredSize(new Dimension(290, 0));
        rulesView.setEditable(false);
        rulesView.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        west.add(preset, BorderLayout.NORTH);
        west.add(new JScrollPane(rulesView), BorderLayout.CENTER);
        west.add(new JLabel("<html>δ(estado, entrada, tope) → (estado', apilar)<br>ε = sin consumir. '$' = fondo de pila.<br>Acepta: estado qf y entrada consumida.</html>"), BorderLayout.SOUTH);
        preset.addActionListener(e -> configure());
        configure();
        build(canvas(), west);
    }

    private void r(String q, String a, String x, String q2, String push) { rules.add(new String[]{q, a, x, q2, push}); }

    private void configure() {
        rules.clear();
        int p = preset.getSelectedIndex();
        if (p == 0) {
            r("q0", "a", "$", "q0", "a$"); r("q0", "a", "a", "q0", "aa");
            r("q0", "b", "a", "q1", ""); r("q1", "b", "a", "q1", "");
            r("q1", "", "$", "qf", "$"); r("q0", "", "$", "qf", "$");
            examples("aaabbb | acepta", "ab | acepta", "ε | acepta", "aabbb | rechaza", "aab | rechaza", "abab | rechaza");
        } else if (p == 1) {
            for (String c : new String[]{"a", "b"}) {
                for (String x : new String[]{"$", "a", "b"}) r("q0", c, x, "q0", c + (x.equals("$") ? "$" : x.equals("a") ? "a" : "b"));
            }
            for (String x : new String[]{"$", "a", "b"}) r("q0", "c", x, "q1", x);
            r("q1", "a", "a", "q1", ""); r("q1", "b", "b", "q1", ""); r("q1", "", "$", "qf", "$");
            examples("abcba | acepta", "c | acepta", "abbcbba | acepta", "abcab | rechaza", "ab | rechaza");
        } else {
            r("q0", "(", "$", "q0", "($"); r("q0", "(", "(", "q0", "((");
            r("q0", ")", "(", "q0", ""); r("q0", "", "$", "qf", "$");
            examples("(()()) | acepta", "()() | acepta", "ε | acepta", "(() | rechaza", "())( | rechaza");
        }
        StringBuilder sb = new StringBuilder();
        for (String[] t : rules) sb.append(fmt(t)).append('\n');
        rulesView.setText(sb.toString());
    }

    private String fmt(String[] t) {
        return "δ(" + t[0] + ", " + (t[1].isEmpty() ? "ε" : t[1]) + ", " + t[2] + ") → (" + t[3] + ", " + (t[4].isEmpty() ? "ε" : t[4]) + ")";
    }

    @Override protected void load(String s) {
        w = s; idx = 0; cur = "q0"; last = "";
        st.setLength(0); st.append('$');
        log("Configuración inicial: estado q0, pila = [$], entrada = " + (s.isEmpty() ? "ε" : s));
    }

    @Override protected boolean step() {
        String top = st.charAt(st.length() - 1) + "";
        String c = idx < w.length() ? w.charAt(idx) + "" : null;
        String[] hit = null;
        for (String[] t : rules) if (t[0].equals(cur) && t[2].equals(top) && c != null && t[1].equals(c)) { hit = t; break; }
        if (hit == null) for (String[] t : rules) if (t[0].equals(cur) && t[2].equals(top) && t[1].isEmpty()) { hit = t; break; }
        if (hit == null) {
            end(false, "no hay transición para (" + cur + ", " + (c == null ? "fin de entrada" : c) + ", tope " + top + ")");
            return false;
        }
        st.setLength(st.length() - 1);                                  // pop
        for (int i = hit[4].length() - 1; i >= 0; i--) st.append(hit[4].charAt(i)); // push
        if (!hit[1].isEmpty()) idx++;
        cur = hit[3];
        last = fmt(hit);
        log(last + "   pila = " + stackStr());
        if (cur.equals("qf") && idx == w.length()) { end(true, "entrada consumida y estado final qf alcanzado"); return false; }
        return true;
    }

    private String stackStr() { return "[" + st.toString() + "]  (tope a la derecha)"; }

    private JComponent canvas() {
        return new JPanel() {
            @Override protected void paintComponent(Graphics g0) {
                super.paintComponent(g0);
                Graphics2D g = (Graphics2D) g0;
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int W = getWidth(), H = getHeight();
                g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
                g.drawString("Cinta de entrada (solo lectura, una dirección)", 20, 22);
                String[] cells = new String[w.length()];
                for (int i = 0; i < cells.length; i++) cells[i] = w.charAt(i) + "";
                tape(g, cells, idx, idx, false, 20, 44, W);
                g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
                g.setColor(cur.equals("qf") ? new Color(0, 130, 0) : Color.BLACK);
                g.drawString("Estado: " + cur, 40, 150);
                g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
                g.setColor(Color.DARK_GRAY);
                g.drawString("Última regla:", 40, 185);
                g.drawString(last.isEmpty() ? "—" : last, 40, 205);
                // pila (LIFO)
                int bw = 70, bh = 30, bx = W / 2 + 40, base = H - 30;
                g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
                g.setColor(Color.BLACK);
                g.drawString("PILA (LIFO)", bx, 100);
                int maxVisible = Math.max(1, (base - 130) / bh);
                int from = Math.max(0, st.length() - maxVisible);
                for (int i = from; i < st.length(); i++) {
                    int y = base - (i - from + 1) * bh;
                    boolean isTop = i == st.length() - 1;
                    g.setColor(isTop ? new Color(255, 225, 90) : st.charAt(i) == '$' ? new Color(200, 200, 235) : new Color(190, 225, 255));
                    g.fillRect(bx, y, bw, bh);
                    g.setColor(Color.DARK_GRAY);
                    g.drawRect(bx, y, bw, bh);
                    g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 18));
                    center(g, st.charAt(i) + "", bx + bw / 2.0, y + bh / 2.0);
                    if (isTop) { g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12)); g.setColor(new Color(200, 30, 30)); g.drawString("← tope", bx + bw + 8, y + 19); }
                }
                g.setColor(Color.BLACK);
                g.drawLine(bx - 6, base, bx + bw + 6, base);
            }
        };
    }
}
