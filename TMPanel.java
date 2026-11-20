import javax.swing.*;
import java.awt.*;
import java.util.*;

public class TMPanel extends SimPanel {
    final boolean bounded;
    final String[] names, texts;
    final String[][] exs;
    final JComboBox<String> preset;
    final JTextArea rulesArea = new JTextArea();
    final JLabel info = new JLabel(" ");
    Map<String, String[]> delta = new HashMap<>();
    String start = "q0", accept = "qa", q = "q0";
    StringBuilder tape = new StringBuilder();
    int head, steps;
    String gamma = "";

    public TMPanel(boolean bounded) {
        this.bounded = bounded;
        if (bounded) {
            names = new String[]{"aⁿbⁿcⁿ (n ≥ 0) — no libre de contexto"};
            texts = new String[]{
                "# ALA para a^n b^n c^n. Marca a->X, b->Y, c->Z en cada pasada.\n" +
                "start q0\naccept qa\n" +
                "q0 < < R q1\n" +
                "q1 > > S qa\n" +
                "q1 a X R q2\n" +
                "q1 Y Y R q4\n" +
                "q2 a a R q2\nq2 Y Y R q2\nq2 b Y R q3\n" +
                "q3 b b R q3\nq3 Z Z R q3\nq3 c Z L q5\n" +
                "q5 Z Z L q5\nq5 b b L q5\nq5 Y Y L q5\nq5 a a L q5\nq5 X X R q1\n" +
                "q4 Y Y R q4\nq4 Z Z R q4\nq4 > > S qa\n"};
            exs = new String[][]{{"aabbcc | acepta", "abc | acepta", "aaabbbccc | acepta", "ε | acepta", "aabbc | rechaza", "abcabc | rechaza", "acb | rechaza"}};
        } else {
            names = new String[]{"aⁿbⁿ (acepta/rechaza)", "Sucesor unario (n → n+1)", "Suma unaria (a+b)", "Incremento binario"};
            texts = new String[]{
                "# Borra una 'a' al inicio y una 'b' al final en cada pasada\nstart q0\naccept qa\n" +
                "q0 _ _ S qa\nq0 a _ R q1\nq1 a a R q1\nq1 b b R q1\nq1 _ _ L q2\n" +
                "q2 b _ L q3\nq3 a a L q3\nq3 b b L q3\nq3 _ _ R q0\n",
                "# 1^n -> 1^(n+1)\nstart q0\naccept qa\nq0 1 1 R q0\nq0 _ 1 S qa\n",
                "# 1^a+1^b -> 1^(a+b): cambia '+' por 1 y borra el último 1\nstart q0\naccept qa\n" +
                "q0 1 1 R q0\nq0 + 1 R q1\nq1 1 1 R q1\nq1 _ _ L q2\nq2 1 _ S qa\n",
                "# Suma 1 a un número binario (bit más significativo a la izquierda)\nstart q0\naccept qa\n" +
                "q0 0 0 R q0\nq0 1 1 R q0\nq0 _ _ L q1\n" +
                "q1 1 0 L q1\nq1 0 1 S qa\nq1 _ 1 S qa\n"};
            exs = new String[][]{
                {"aabb | acepta", "aaabbb | acepta", "ε | acepta", "aab | rechaza", "abb | rechaza", "ba | rechaza"},
                {"111 | resultado 1111", "ε | resultado 1"},
                {"111+11 | resultado 11111", "1+1 | resultado 11", "+111 | resultado 111"},
                {"1011 | resultado 1100", "111 | resultado 1000", "0 | resultado 1"}};
        }
        preset = new JComboBox<>(names);
        JPanel west = new JPanel(new BorderLayout(4, 4));
        west.setBorder(BorderFactory.createTitledBorder(bounded ? "Reglas del ALA (editables)" : "Reglas de la MT (editables)"));
        west.setPreferredSize(new Dimension(300, 0));
        rulesArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        west.add(preset, BorderLayout.NORTH);
        west.add(new JScrollPane(rulesArea), BorderLayout.CENTER);
        west.add(new JLabel("<html>Formato: estado leído escrito L|R|S siguiente<br>Directivas: start qX / accept qX. Blanco = _<br>" +
                (bounded ? "Marcadores: &lt; = ⊢ y &gt; = ⊣" : "") + "<br>Edita y pulsa Reiniciar para probar tu propia máquina.</html>"), BorderLayout.SOUTH);
        preset.addActionListener(e -> { rulesArea.setText(texts[preset.getSelectedIndex()]); examples(exs[preset.getSelectedIndex()]); });
        rulesArea.setText(texts[0]);
        examples(exs[0]);
        build(canvas(), west);
    }

    private boolean parse() {
        delta.clear();
        Set<Character> sym = new TreeSet<>();
        start = "q0"; accept = "qa";
        int ln = 0;
        for (String line : rulesArea.getText().split("\n")) {
            ln++;
            line = line.replaceAll("#.*", "").trim();
            if (line.isEmpty()) continue;
            String[] t = line.split("\\s+");
            if (t.length == 2 && t[0].equals("start")) start = t[1];
            else if (t.length == 2 && t[0].equals("accept")) accept = t[1];
            else if (t.length == 5 && t[1].length() == 1 && t[2].length() == 1 && "LRS".contains(t[3])) {
                delta.put(t[0] + " " + t[1], new String[]{t[2], t[3], t[4]});
                sym.add(t[1].charAt(0)); sym.add(t[2].charAt(0));
            } else { status.setText("Error de sintaxis en la línea " + ln + ": " + line); status.setForeground(Color.RED); return false; }
        }
        StringBuilder sb = new StringBuilder();
        for (char c : sym) sb.append(c).append(' ');
        gamma = sb.toString().trim();
        return true;
    }

    @Override protected void load(String s) {
        steps = 0; head = 0; q = "q0";
        if (!parse()) { finished = true; return; }
        q = start;
        if (bounded && (s.contains("<") || s.contains(">"))) { status.setText("La entrada no puede contener '<' ni '>'"); finished = true; return; }
        tape.setLength(0);
        if (bounded) tape.append('<').append(s).append('>');
        else tape.append(s.isEmpty() ? "_" : s);
        log("Configuración inicial: estado " + q + ", cinta = " + tape + ", cabezal en la celda 0");
    }

    @Override protected boolean step() {
        String[] t = delta.get(q + " " + tape.charAt(head));
        if (t == null) { end(false, "no existe transición para (" + q + ", " + tape.charAt(head) + ")"); return false; }
        char old = tape.charAt(head), wr = t[0].charAt(0);
        if (bounded && (old == '<' || old == '>') && wr != old) { end(false, "intento de sobrescribir un delimitador de frontera"); return false; }
        tape.setCharAt(head, wr);
        log("δ(" + q + ", " + old + ") = (" + t[2] + ", " + wr + ", " + t[1] + ")   cinta: " + tape);
        head += t[1].equals("R") ? 1 : t[1].equals("L") ? -1 : 0;
        q = t[2];
        steps++;
        if (bounded && (head < 0 || head >= tape.length())) { end(false, "el cabezal intentó salirse de los delimitadores (viola la cota lineal)"); return false; }
        if (!bounded) {
            if (head < 0) { tape.insert(0, '_'); head = 0; }
            if (head >= tape.length()) tape.append('_');
        }
        if (q.equals(accept)) {
            String out = tape.toString().replace("_", "");
            end(true, "estado de aceptación " + accept + " en " + steps + " pasos. Cinta final: " + (bounded ? tape : out.isEmpty() ? "(vacía)" : out));
            return false;
        }
        if (steps >= 20000) { end(false, "límite de 20000 pasos (posible bucle infinito)"); return false; }
        return true;
    }

    private JComponent canvas() {
        return new JPanel() {
            @Override protected void paintComponent(Graphics g0) {
                super.paintComponent(g0);
                Graphics2D g = (Graphics2D) g0;
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int W = getWidth();
                g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
                g.drawString(bounded ? "Cinta acotada: ⊢ w ⊣ (tamaño fijo |w|+2, cabezal bidireccional)"
                        : "Cinta extendible (infinita a ambos lados, '_' = blanco). ▼ = cabezal de lectura/escritura", 20, 22);
                String[] cells = new String[tape.length()];
                for (int i = 0; i < cells.length; i++) {
                    char c = tape.charAt(i);
                    cells[i] = bounded && c == '<' ? "⊢" : bounded && c == '>' ? "⊣" : c + "";
                }
                tape(g, cells, head, 0, !bounded, 20, 60, W);
                g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
                boolean acc = q.equals(accept);
                g.setColor(acc ? new Color(0, 130, 0) : Color.BLACK);
                g.drawString("Estado activo: " + q + (acc ? "  (aceptación)" : ""), 40, 160);
                g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
                g.setColor(Color.DARK_GRAY);
                g.drawString("Alfabeto de la cinta Γ = { " + gamma + " }", 40, 200);
                g.drawString("Posición del cabezal: " + head + "   Pasos: " + steps, 40, 225);
                g.drawString("Estado inicial: " + start + "   Estado de aceptación: " + accept, 40, 250);
                if (bounded) g.drawString("Longitud de la cinta: " + tape.length() + " celdas (fija)", 40, 275);
            }
        };
    }
}
