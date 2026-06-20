# 1. Ép hệ thống và Terminal nhận UTF-8 chuẩn mã hóa
$OutputEncoding = [System.Text.Encoding]::UTF8
chcp 65001 | Out-Null

# 2. Biên dịch code
javac -encoding UTF-8 *.java

# 3. Bẫy lỗi: Nếu biên dịch thất bại ($? là False) thì dừng lại luôn
if (-not $?) {
    Write-Host "Biên dịch thất bại! Vui lòng kiểm tra lại code." -ForegroundColor Red
    pause
    exit
}

# 4. Nếu biên dịch thành công thì mới chạy Server
java ServerMain
pause