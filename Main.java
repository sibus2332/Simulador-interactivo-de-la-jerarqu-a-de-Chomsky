
import java.awt.*;
import javax.swing.*;

public class Main {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            try {
                UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName()
                );
            } catch (Exception ignored) {
            }

            JFrame f = new JFrame("Simulador de la Jerarquía de Chomsky");

            CardLayout cl = new CardLayout();
            JPanel cards = new JPanel(cl);

            JPanel menu = new JPanel(new GridLayout(0, 1, 10, 10));
            menu.setBorder(BorderFactory.createEmptyBorder(20, 60, 20, 60));

            JLabel title = new JLabel(
                "Jerarquía de Chomsky — elige un módulo",
                SwingConstants.CENTER
            );

            title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
            menu.add(title);

            Object[][] mods = {
                {"3", "Nivel 3 · Lenguajes regulares — Autómata Finito (AFD)", new DFAPanel()},
                {"2", "Nivel 2 · Libres de contexto — Autómata de Pila (memoria LIFO)", new PDAPanel()},
                {"1", "Nivel 1 · Sensibles al contexto — Autómata Linealmente Acotado", new TMPanel(true)},
                {"0", "Nivel 0 · Recursivamente enumerables — Máquina de Turing", new TMPanel(false)}
            };

            for (Object[] m : mods) {

                JButton b = new JButton((String) m[1]);
                b.setFont(b.getFont().deriveFont(Font.BOLD, 16f));

                b.addActionListener(e -> cl.show(cards, (String) m[0]));
                menu.add(b);

                JPanel wrap = new JPanel(new BorderLayout());

                JButton back = new JButton("← Menú principal");

                back.addActionListener(e -> cl.show(cards, "menu"));

                JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));

                bar.add(back);
                bar.add(new JLabel((String) m[1]));

                wrap.add(bar, BorderLayout.NORTH);
                wrap.add((Component) m[2], BorderLayout.CENTER);

                cards.add(wrap, (String) m[0]);
            }

            cards.add(menu, "menu");
            cl.show(cards, "menu");

            f.setContentPane(cards);
            f.setSize(1100, 760);
            f.setLocationRelativeTo(null);
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setVisible(true);

        });
    }
}