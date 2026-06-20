

public class PlayfairCipher {
    private char[][] matrix = new char[5][5];

    public PlayfairCipher(String key) {
        createMatrix(key);
    }

    // Chuẩn hóa chuỗi: Bỏ dấu tiếng Việt, viết thường, chuyển J thành I, lọc ký tự a-z
    public static String prepareText(String text) {
        text = text.toLowerCase()
                .replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a")
                .replaceAll("[èéẹẻẽêềếệểễ]", "e")
                .replaceAll("[ìíịỉĩ]", "i")
                .replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o")
                .replaceAll("[ùúụủũưừứựửữ]", "u")
                .replaceAll("[ỳýỵỷỹ]", "y")
                .replaceAll("đ", "d")
                .replaceAll("j", "i")
                .replaceAll("[^a-z]", ""); // Xóa khoảng trắng và ký tự đặc biệt
        return text;
    }

    private void createMatrix(String key) {
        String preparedKey = prepareText(key) + "abcdefghiklmnopqrstuvwxyz"; // loại j
        StringBuilder sb = new StringBuilder();
        for (char c : preparedKey.toCharArray()) {
            if (sb.indexOf(String.valueOf(c)) == -1) {
                sb.append(c);
            }
        }
        int idx = 0;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                matrix[i][j] = sb.charAt(idx++);
            }
        }
    }

    private int[] getPosition(char c) {
        if (c == 'j') c = 'i';
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (matrix[i][j] == c) return new int[]{i, j};
            }
        }
        return null;
    }

    public String encrypt(String plaintext) {
        String text = prepareText(plaintext);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            sb.append(text.charAt(i));
            if (i < text.length() - 1 && text.charAt(i) == text.charAt(i + 1)) {
                sb.append('x'); // Chèn x nếu 2 ký tự trùng nhau đứng cạnh
            }
        }
        if (sb.length() % 2 != 0) sb.append('x'); // Chèn x nếu độ dài lẻ

        return process(sb.toString(), 1);
    }

    public String decrypt(String ciphertext) {
        return process(ciphertext, -1);
    }

    private String process(String text, int direction) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); i += 2) {
            int[] p1 = getPosition(text.charAt(i));
            int[] p2 = getPosition(text.charAt(i + 1));
            if (p1 == null || p2 == null) continue;

            if (p1[0] == p2[0]) { // Cùng hàng
                result.append(matrix[p1[0]][(p1[1] + direction + 5) % 5]);
                result.append(matrix[p2[0]][(p2[1] + direction + 5) % 5]);
            } else if (p1[1] == p2[1]) { // Cùng cột
                result.append(matrix[(p1[0] + direction + 5) % 5][p1[1]]);
                result.append(matrix[(p2[0] + direction + 5) % 5][p2[1]]);
            } else { // Tạo thành hình chữ nhật
                result.append(matrix[p1[0]][p2[1]]);
                result.append(matrix[p2[0]][p1[1]]);
            }
        }
        return result.toString();
    }
}