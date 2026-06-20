
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class ServerMain {
    private static final int PORT = 8888;

    public static void main(String[] args) {
        System.out.println("Server TCP đang khởi chạy và lắng nghe trên port " + PORT + "...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Có kết nối mới từ: " + clientSocket.getRemoteSocketAddress());
                // Mỗi client xử lý trên một luồng riêng biệt
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ClientHandler implements Runnable {
    private Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            String key = in.readLine();
            String ciphertext = in.readLine();
            
            if (key == null || ciphertext == null) return;

            // Đo hiệu năng xử lý giải mã tại Server
            long startTime = System.nanoTime();
            
            PlayfairCipher cipher = new PlayfairCipher(key);
            String plaintext = cipher.decrypt(ciphertext);
            
            // Tìm cụm "xinchao" (đã được chuẩn hóa không dấu)
            String target = "xinchao";
            List<Integer> positions = new ArrayList<>();
            int index = plaintext.indexOf(target);
            while (index >= 0) {
                positions.add(index);
                index = plaintext.indexOf(target, index + 1);
            }
            
            long endTime = System.nanoTime();
            long processingTimeMs = (endTime - startTime) / 1000000; // Đổi sang mili giây

            // Gửi kết quả về Client: 
            // Dòng 1: Bản rõ đã giải mã
            // Dòng 2: Danh sách vị trí
            // Dòng 3: Thời gian xử lý tại Server (phục vụ báo cáo hiệu năng)
            out.println(plaintext);
            out.println(positions.toString());
            out.println(processingTimeMs);

        } catch (IOException e) {
            System.out.println("Lỗi kết nối client: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}