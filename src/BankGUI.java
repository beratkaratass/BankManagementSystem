import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.List;

public class BankGUI {

    private static final Color BG = new Color(0xF4, 0xF6, 0xFA);
    private static final Color PANEL = Color.WHITE;
    private static final Color PRIMARY = new Color(0x1F, 0x6F, 0xEB);
    private static final Color PRIMARY_DARK = new Color(0x15, 0x57, 0xBE);
    private static final Color ACCENT = new Color(0x10, 0xB9, 0x81);
    private static final Color TEXT = new Color(0x1F, 0x29, 0x37);
    private static final Color MUTED = new Color(0x6B, 0x72, 0x80);
    private static final Color BORDER = new Color(0xE5, 0xE7, 0xEB);

    private JFrame frame;
    private JTextField idField;
    private JTextArea logArea;
    private DefaultListModel<String> queueModel;
    private JList<String> queueList;
    private JLabel queueCountLabel;
    private JLabel registeredCountLabel;
    private JLabel nowServingLabel;
    private JLabel statusLabel;

    private CustomerBST bst;
    private CustomerQueue queue;
    private int ticketCounter = 0;

    private static final String DATA_FILE = "Musteriler.txt";

    public BankGUI() {
        bst = new CustomerBST();
        queue = new CustomerQueue();
        loadData();
        buildUI();
    }

    private void loadData() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            String[] defaults = {
                "2203016,Berat,Karataş",
                "2364656,Emirhan,Sönmez",
                "1905885,Melisa,Okay",
                "5077437,Kadir,Yay"
            };
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                for (String row : defaults) {
                    bw.write(row);
                    bw.newLine();
                    String[] p = row.split(",");
                    bst.insert(new Customer(p[0], p[1], p[2]));
                }
            } catch (IOException e) {
                System.err.println("File create error: " + e.getMessage());
            }
        } else {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] p = line.split(",");
                    if (p.length == 3) {
                        bst.insert(new Customer(p[0].trim(), p[1].trim(), p[2].trim()));
                    }
                }
            } catch (IOException e) {
                System.err.println("File read error: " + e.getMessage());
            }
        }
    }

    private void appendToFile(Customer c) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(DATA_FILE, true))) {
            bw.write(c.getId() + "," + c.getName() + "," + c.getSurname());
            bw.newLine();
        } catch (IOException e) {
            System.err.println("File write error: " + e.getMessage());
        }
    }

    private void buildUI() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored) {}

        frame = new JFrame("Banka Kuyruk Yönetim Sistemi");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(960, 640);
        frame.setMinimumSize(new Dimension(820, 560));
        frame.setLocationRelativeTo(null);
        frame.getContentPane().setBackground(BG);
        frame.setLayout(new BorderLayout());

        frame.add(buildHeader(), BorderLayout.NORTH);
        frame.add(buildCenter(), BorderLayout.CENTER);
        frame.add(buildStatusBar(), BorderLayout.SOUTH);

        log("Sistem hazır. " + bst.getSize() + " müşteri yüklendi.");
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRIMARY);
        header.setBorder(new EmptyBorder(18, 24, 18, 24));

        JLabel title = new JLabel("Banka Kuyruk Yönetim Sistemi");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("BST + FIFO Queue");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(255, 255, 255, 200));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(title);
        left.add(Box.createVerticalStrut(2));
        left.add(subtitle);

        nowServingLabel = new JLabel("Sıradaki: —");
        nowServingLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nowServingLabel.setForeground(Color.WHITE);
        nowServingLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        header.add(left, BorderLayout.WEST);
        header.add(nowServingLabel, BorderLayout.EAST);
        return header;
    }

    private JComponent buildCenter() {
        JPanel content = new JPanel(new BorderLayout(16, 16));
        content.setBackground(BG);
        content.setBorder(new EmptyBorder(16, 16, 16, 16));

        content.add(buildActionPanel(), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildQueuePanel(), buildLogPanel());
        split.setResizeWeight(0.42);
        split.setBorder(null);
        split.setBackground(BG);
        content.add(split, BorderLayout.CENTER);

        return content;
    }

    private JPanel buildActionPanel() {
        JPanel card = roundedCard();
        card.setLayout(new BorderLayout(12, 12));
        card.setBorder(new CompoundBorder(card.getBorder(), new EmptyBorder(16, 16, 16, 16)));

        JLabel idLabel = new JLabel("Müşteri ID");
        idLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        idLabel.setForeground(MUTED);

        idField = new JTextField();
        idField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        idField.setBorder(new CompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(8, 10, 8, 10)));
        idField.setPreferredSize(new Dimension(220, 36));

        JPanel idGroup = new JPanel();
        idGroup.setOpaque(false);
        idGroup.setLayout(new BoxLayout(idGroup, BoxLayout.Y_AXIS));
        idLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        idField.setAlignmentX(Component.LEFT_ALIGNMENT);
        idGroup.add(idLabel);
        idGroup.add(Box.createVerticalStrut(4));
        idGroup.add(idField);

        JButton takeTicket = primaryButton("Sıra Al", PRIMARY);
        JButton register = primaryButton("Yeni Kayıt", ACCENT);
        JButton callNext = primaryButton("Sıradakini Çağır", PRIMARY_DARK);
        JButton clearLog = secondaryButton("Logu Temizle");

        takeTicket.addActionListener(this::onTakeTicket);
        register.addActionListener(this::onRegister);
        callNext.addActionListener(this::onCallNext);
        clearLog.addActionListener(e -> logArea.setText(""));
        idField.addActionListener(this::onTakeTicket);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        buttons.add(takeTicket);
        buttons.add(register);
        buttons.add(callNext);
        buttons.add(clearLog);

        card.add(idGroup, BorderLayout.WEST);
        card.add(buttons, BorderLayout.EAST);
        return card;
    }

    private JPanel buildQueuePanel() {
        JPanel card = roundedCard();
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(new CompoundBorder(card.getBorder(), new EmptyBorder(14, 14, 14, 14)));

        JLabel title = new JLabel("Bekleyen Kuyruk");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(TEXT);

        queueCountLabel = new JLabel("0 kişi");
        queueCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        queueCountLabel.setForeground(MUTED);

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        head.add(title, BorderLayout.WEST);
        head.add(queueCountLabel, BorderLayout.EAST);

        queueModel = new DefaultListModel<>();
        queueList = new JList<>(queueModel);
        queueList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        queueList.setFixedCellHeight(36);
        queueList.setBackground(PANEL);
        queueList.setBorder(new EmptyBorder(4, 4, 4, 4));
        queueList.setCellRenderer(new QueueCellRenderer());

        JScrollPane sp = new JScrollPane(queueList);
        sp.setBorder(new LineBorder(BORDER, 1, true));
        sp.getViewport().setBackground(PANEL);

        card.add(head, BorderLayout.NORTH);
        card.add(sp, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildLogPanel() {
        JPanel card = roundedCard();
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(new CompoundBorder(card.getBorder(), new EmptyBorder(14, 14, 14, 14)));

        JLabel title = new JLabel("İşlem Geçmişi");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(TEXT);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("JetBrains Mono", Font.PLAIN, 13));
        if (!logArea.getFont().getFamily().equals("JetBrains Mono")) {
            logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        }
        logArea.setBackground(new Color(0xFB, 0xFC, 0xFD));
        logArea.setForeground(TEXT);
        logArea.setMargin(new Insets(8, 10, 8, 10));

        JScrollPane sp = new JScrollPane(logArea);
        sp.setBorder(new LineBorder(BORDER, 1, true));

        card.add(title, BorderLayout.NORTH);
        card.add(sp, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(0xEE, 0xF1, 0xF6));
        bar.setBorder(new EmptyBorder(8, 16, 8, 16));

        statusLabel = new JLabel("Hazır.");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(MUTED);

        registeredCountLabel = new JLabel();
        registeredCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        registeredCountLabel.setForeground(MUTED);
        updateRegisteredCount();

        bar.add(statusLabel, BorderLayout.WEST);
        bar.add(registeredCountLabel, BorderLayout.EAST);
        return bar;
    }

    private JPanel roundedCard() {
        JPanel p = new JPanel();
        p.setBackground(PANEL);
        p.setBorder(new LineBorder(BORDER, 1, true));
        return p;
    }

    private JButton primaryButton(String text, Color bg) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color base = getBackground();
                if (getModel().isPressed()) base = base.darker();
                else if (getModel().isRollover()) base = base.brighter();
                g2.setColor(base);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setBorder(new EmptyBorder(8, 16, 8, 16));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton secondaryButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(PANEL);
        b.setForeground(TEXT);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        b.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(7, 14, 7, 14)));
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void onTakeTicket(ActionEvent e) {
        String id = idField.getText().trim();
        if (id.isEmpty()) {
            warn("ID alanı boş olamaz.");
            return;
        }
        Customer c = bst.search(id);
        if (c == null) {
            warn(id + " numaralı müşteri kayıtlı değil.");
            return;
        }
        if (queue.contains(id)) {
            warn(c.getFullName() + " zaten kuyrukta.");
            return;
        }
        queue.enqueue(c);
        ticketCounter++;
        log(String.format("Sıra alındı  | #%03d | %s (%s)", ticketCounter, c.getFullName(), c.getId()));
        idField.setText("");
        refreshQueue();
        status(c.getFullName() + " kuyruğa eklendi.");
    }

    private void onRegister(ActionEvent e) {
        String id = idField.getText().trim();
        if (id.isEmpty()) {
            warn("Kayıt için önce ID girin.");
            return;
        }
        if (!id.matches("\\d+")) {
            warn("ID sadece rakam içermelidir.");
            return;
        }
        if (bst.search(id) != null) {
            warn("Bu ID zaten kayıtlı.");
            return;
        }

        JTextField nameField = new JTextField(12);
        JTextField surnameField = new JTextField(12);
        JPanel form = new JPanel(new GridLayout(2, 2, 8, 8));
        form.setBorder(new EmptyBorder(8, 8, 8, 8));
        form.add(new JLabel("Ad:"));
        form.add(nameField);
        form.add(new JLabel("Soyad:"));
        form.add(surnameField);

        int res = JOptionPane.showConfirmDialog(frame, form,
                "Yeni Müşteri Kaydı - ID " + id,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;

        String name = nameField.getText().trim();
        String surname = surnameField.getText().trim();
        if (name.isEmpty() || surname.isEmpty()) {
            warn("Ad ve soyad boş bırakılamaz.");
            return;
        }
        Customer nc = new Customer(id, name, surname);
        bst.insert(nc);
        appendToFile(nc);
        log("Yeni kayıt   | " + nc.getFullName() + " (" + id + ")");
        idField.setText("");
        updateRegisteredCount();
        status("Müşteri kaydedildi: " + nc.getFullName());
    }

    private void onCallNext(ActionEvent e) {
        if (queue.isEmpty()) {
            warn("Kuyrukta bekleyen müşteri yok.");
            nowServingLabel.setText("Sıradaki: —");
            return;
        }
        Customer c = queue.dequeue();
        log("→ Vezneye çağrıldı: " + c.getFullName() + " (" + c.getId() + ")");
        nowServingLabel.setText("Sıradaki: " + c.getFullName());
        refreshQueue();
        status("Çağrıldı: " + c.getFullName());
    }

    private void refreshQueue() {
        queueModel.clear();
        List<Customer> snap = queue.snapshot();
        for (int i = 0; i < snap.size(); i++) {
            Customer c = snap.get(i);
            queueModel.addElement((i + 1) + "|" + c.getFullName() + "|" + c.getId());
        }
        queueCountLabel.setText(queue.getSize() + " kişi");
    }

    private void updateRegisteredCount() {
        registeredCountLabel.setText("Kayıtlı müşteri: " + bst.getSize());
    }

    private void log(String msg) {
        logArea.append(msg + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private void warn(String msg) {
        log("⚠  " + msg);
        status(msg);
    }

    private void status(String msg) {
        statusLabel.setText(msg);
    }

    public void show() {
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BankGUI().show());
    }

    private static class QueueCellRenderer extends JPanel implements ListCellRenderer<String> {
        private final JLabel pos = new JLabel();
        private final JLabel name = new JLabel();
        private final JLabel id = new JLabel();

        QueueCellRenderer() {
            setLayout(new BorderLayout(10, 0));
            setBorder(new EmptyBorder(6, 10, 6, 10));
            pos.setFont(new Font("Segoe UI", Font.BOLD, 13));
            pos.setForeground(Color.WHITE);
            pos.setHorizontalAlignment(SwingConstants.CENTER);
            pos.setOpaque(true);
            pos.setBackground(PRIMARY);
            pos.setBorder(new EmptyBorder(4, 8, 4, 8));
            pos.setPreferredSize(new Dimension(36, 24));
            name.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            name.setForeground(TEXT);
            id.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            id.setForeground(MUTED);

            JPanel right = new JPanel(new BorderLayout());
            right.setOpaque(false);
            right.add(name, BorderLayout.CENTER);
            right.add(id, BorderLayout.EAST);

            add(pos, BorderLayout.WEST);
            add(right, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends String> list, String value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            String[] parts = value.split("\\|");
            pos.setText(parts[0]);
            name.setText(parts[1]);
            id.setText("ID " + parts[2]);
            if (index == 0) {
                pos.setBackground(ACCENT);
            } else {
                pos.setBackground(PRIMARY);
            }
            setBackground(isSelected ? new Color(0xE8, 0xF0, 0xFE) : PANEL);
            return this;
        }
    }
}
