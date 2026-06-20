

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class ClientGUI extends JFrame {
    private JTextField txtKey, txtIP, txtPort;
    private JTextArea txtPlainInput, txtCiphertext, txtResult;
    private JButton btnSend;

    public ClientGUI() {
        setTitle("Playfair TCP Client - Đánh giá Hiệu năng mạng");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel Cấu hình kết nối
        JPanel pnlTop = new JPanel(new GridLayout(1, 6, 5, 5));
        pnlTop.setBorder(BorderFactory.createTitledBorder("Cấu hình mạng & Khóa"));
        pnlTop.add(new JLabel("IP Server:"));
        txtIP = new JTextField("127.0.0.1");
        pnlTop.add(txtIP);
        pnlTop.add(new JLabel("Port:"));
        txtPort = new JTextField("8888");
        pnlTop.add(txtPort);
        pnlTop.add(new JLabel("Khóa (Key):"));
        txtKey = new JTextField("network");
        pnlTop.add(txtKey);
        add(pnlTop, BorderLayout.NORTH);

        // Panel Nhập liệu và kết quả
        JPanel pnlCenter = new JPanel(new GridLayout(3, 1, 5, 5));
        
        JPanel pnlInput = new JPanel(new BorderLayout());
        pnlInput.setBorder(BorderFactory.createTitledBorder("1. Nhập văn bản rõ cần gửi (Ví dụ: xinchao các bạn xinchao)"));
        txtPlainInput = new JTextArea();
        pnlInput.add(new JScrollPane(txtPlainInput), BorderLayout.CENTER);
        
        JPanel pnlCipher = new JPanel(new BorderLayout());
        pnlCipher.setBorder(BorderFactory.createTitledBorder("2. Bản mã (Mã hóa Playfair tại Client trước khi gửi)"));
        txtCiphertext = new JTextArea();
        txtCiphertext.setEditable(false);
        txtCiphertext.setBackground(new Color(240, 240, 240));
        pnlCipher.add(new JScrollPane(txtCiphertext), BorderLayout.CENTER);

        JPanel pnlResult = new JPanel(new BorderLayout());
        pnlResult.setBorder(BorderFactory.createTitledBorder("3. Kết quả phản hồi từ Server"));
        txtResult = new JTextArea();
        txtResult.setEditable(false);
        txtResult.setBackground(new Color(230, 245, 230));
        pnlResult.add(new JScrollPane(txtResult), BorderLayout.CENTER);

        pnlCenter.add(pnlInput);
        pnlCenter.add(pnlCipher);
        pnlCenter.add(pnlResult);
        add(pnlCenter, BorderLayout.CENTER);

        // Nút hành động
        btnSend = new JButton("Mã hóa và Gửi lên Server");
        btnSend.setFont(new Font("Arial", Font.BOLD, 14));
        btnSend.setBackground(new Color(70, 130, 180));
        btnSend.setForeground(Color.WHITE);
        add(btnSend, BorderLayout.SOUTH);

        // Xử lý sự kiện nút bấm
        btnSend.addActionListener(e -> sendData());
    }

    private void sendData() {
        String ip = txtIP.getText().trim();
        int port = Integer.parseInt(txtPort.getText().trim());
        String key = txtKey.getText().trim();
        String plainText = txtPlainInput.getText().trim();

        if (key.isEmpty() || plainText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ Khóa và Văn bản!");
            return;
        }

        try {
            // Bước 1: Mã hóa tại Client
            PlayfairCipher cipher = new PlayfairCipher(key);
            String cipherText = cipher.encrypt(plainText);
            txtCiphertext.setText(cipherText);

            // Đo tổng thời gian khứ hồi (Round Trip Time - RTT) qua mạng
            long startRTT = System.nanoTime();

            // Bước 2: Gửi qua Socket TCP
            try (
                Socket socket = new Socket(ip, port);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
            ) {
                // Gửi khóa và bản mã lên server
                out.println(key);
                out.println(cipherText);

                // Nhận phản hồi
                String decryptedText = in.readLine();
                String positions = in.readLine();
                String serverProcTime = in.readLine();

                long endRTT = System.nanoTime();
                double rttMs = (endRTT - startRTT) / 1000000.0;

                // Hiển thị kết quả lên GUI
                StringBuilder sb = new StringBuilder();
                sb.append("• Bản rõ Server giải mã được: ").append(decryptedText).append("\n");
                sb.append("• Vị trí cụm 'xinchao' tìm thấy: ").append(positions).append("\n\n");
                sb.append("===== ĐÁNH GIÁ HIỆU NĂNG =====\n");
                sb.append("+ Thời gian Server giải mã & xử lý chuỗi: ").append(serverProcTime).append(" ms\n");
                sb.append("+ Tổng thời gian phản hồi mạng (RTT): ").append(String.format("%.2f", rttMs)).append(" ms\n");
                
                txtResult.setText(sb.toString());
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi kết nối tới Server: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClientGUI().setVisible(true));
    }
}