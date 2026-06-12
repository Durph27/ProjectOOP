import csv
import random
from datetime import datetime, timedelta
from pathlib import Path


SEED = 20260612
ROW_COUNT = 240
OUTPUT = Path("data/sample/yagi_complex_random.csv")

NAMES = [
    "Nguyễn Minh Anh", "Trần Thu Hà", "Lê Hoàng Nam", "Phạm Ngọc Lan",
    "Hoàng Đức Long", "Vũ Thanh Hương", "Đặng Quang Huy", "Bùi Khánh Linh",
    "Đỗ Hải Yến", "Hồ Gia Bảo", "Ngô Phương Thảo", "Dương Tuấn Kiệt",
    "Lý Bảo Châu", "Mai Quốc Việt", "Tạ Minh Trang", "Cao Đức Anh",
    "Trịnh Thùy Dương", "Đinh Nhật Minh", "Võ Ngọc Mai", "Phan Anh Tuấn",
    "Nguyễn Thị Hồng", "Trần Quốc Khánh", "Lê Thanh Tâm", "Phạm Gia Hân",
    "Hoàng Minh Đức", "Vũ Quỳnh Anh", "Đặng Hải Đăng", "Bùi Ngọc Ánh",
    "Đỗ Thành Công", "Hồ Minh Châu", "Ngô Đức Thịnh", "Dương Khánh Vy",
]

LOCATIONS = [
    "Quảng Ninh", "Hạ Long", "Hải Phòng", "Hà Nội", "Yên Bái",
    "Lào Cai", "Phú Thọ", "Thái Nguyên", "Tuyên Quang", "Cao Bằng",
    "Bắc Giang", "Nam Định", "Hòa Bình", "Lạng Sơn",
]

PLATFORMS = ["facebook", "twitter", "tiktok", "youtube"]

TOPICS = [
    "Nhà tôi bị tốc mái, nước tràn vào làm đồ đạc và xe máy hỏng hết",
    "Cầu sập, đường ngập và giao thông tê liệt nên xe cứu trợ chưa thể đi qua",
    "Nhiều gia đình phải sơ tán, vẫn còn người mất tích và mắc kẹt",
    "Hoa màu mất trắng, gia súc bị cuốn trôi, hoạt động sản xuất phải đóng cửa",
    "Mất điện, mất nước và mất sóng liên tục, khu dân cư đang bị cô lập",
    "Đội ngũ y tế đã đến khám bệnh, cấp thuốc và chăm sóc sức khỏe cho người dân",
    "Thực phẩm cứu trợ gồm gạo, mì tôm và nước sạch đã được chuyển đến",
    "Nhà tạm và lều trại đã dựng xong nhưng vẫn chưa đủ chỗ ở",
    "Quỹ cứu trợ công bố tiền mặt và hỗ trợ tài chính đã được chuyển khoản",
    "Thuyền cứu hộ và ca nô đang vận chuyển người dân qua khu vực đường ngập",
    "Nước bẩn và rác thải gây lo ngại về dịch bệnh sau lũ",
    "Trường học và nhiều công trình bị nứt tường, hư hỏng nặng",
]

REACTIONS = [
    "Rất cảm ơn lực lượng hỗ trợ, mọi người làm việc tận tình và kịp thời.",
    "Việc hỗ trợ khá nhanh chóng, minh bạch và đúng người.",
    "Không thể nói là ổn khi hàng cứu trợ vẫn đến quá chậm.",
    "Nghe báo cáo là đầy đủ nhưng thực tế nhiều nơi vẫn thiếu.",
    "Tình hình nghiêm trọng, người dân rất lo lắng và thất vọng.",
    "May mắn là mọi người an toàn, tinh thần đoàn kết thật đáng quý.",
    "Không tệ như tin đồn, nhưng cũng chưa thể xem là tốt.",
    "Quá tuyệt vời... chờ ba ngày vẫn chưa thấy nước uống.",
    "Tạm thời ổn, chưa có thêm thiệt hại đáng kể.",
    "Thông tin còn mâu thuẫn, cần kiểm chứng trước khi kết luận.",
    "Có hỗ trợ nhưng phân phối chưa công bằng, một số nơi vẫn không có.",
    "Nỗ lực cứu hộ rất đáng ghi nhận dù điều kiện còn khó khăn.",
]

NOISE_PREFIXES = [
    "", "CẬP NHẬT: ", "[Tin từ người dân] ", "<p>", "Mọi người ơi!!! ",
    "Theo mình thấy thì ", "#baoYagi #cuutro ", "Video mới: ",
]

NOISE_SUFFIXES = [
    "", " #Yagi #baoso3", " Xem thêm: https://example.org/cap-nhat",
    " Ai biết thông tin chính xác xin phản hồi.", "!!!", " 😢", " ❤️",
    " </p>", " (chia sẻ lại để kiểm chứng)", "???",
]


def make_content(index: int) -> str:
    topic_count = random.choices([1, 2, 3], weights=[50, 38, 12], k=1)[0]
    parts = random.sample(TOPICS, topic_count)
    if index % 19 == 0:
        reaction = "Quá tuyệt vời... chờ ba ngày vẫn chưa thấy nước uống."
    elif index % 13 == 0:
        reaction = "Lực lượng hỗ trợ rất tận tình nhưng thực phẩm đến chậm và vẫn còn thiếu."
    elif index % 11 == 0:
        reaction = "Không tệ như tin đồn, nhưng cũng chưa thể xem là tốt."
    else:
        reaction = random.choice(REACTIONS)
    content = ". ".join(parts + [reaction])

    if index % 17 == 0:
        content += " Không phải tất cả thông tin trên mạng đều chính xác."
    if index % 23 == 0:
        content += " bao yagiiii qua khung khiep nhung mn dung hoang manggg."
    if index % 31 == 0:
        content += " Quảng cáo bán áo mưa giảm giá, nội dung này không liên quan cứu trợ."

    return random.choice(NOISE_PREFIXES) + content + random.choice(NOISE_SUFFIXES)


def make_timestamp(index: int) -> datetime:
    if index % 29 == 0:
        base = datetime(2024, 8, 28)
    elif index % 37 == 0:
        base = datetime(2024, 10, 2)
    else:
        base = datetime(2024, 9, 1)
    return base + timedelta(days=random.randint(0, 27), minutes=random.randint(0, 1439))


def main() -> None:
    random.seed(SEED)
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)

    with OUTPUT.open("w", encoding="utf-8", newline="") as output_file:
        writer = csv.writer(output_file)
        writer.writerow([
            "id", "platform", "content", "author", "timestamp", "url",
            "likes", "shares", "comments", "location",
        ])

        for index in range(1, ROW_COUNT + 1):
            platform = random.choice(PLATFORMS)
            post_id = f"complex_{index:04d}"
            timestamp = make_timestamp(index)
            writer.writerow([
                post_id,
                platform,
                make_content(index),
                random.choice(NAMES),
                timestamp.strftime("%Y-%m-%d %H:%M:%S"),
                f"https://social.example/{platform}/{post_id}",
                random.randint(0, 8000),
                random.randint(0, 1800),
                random.randint(0, 950),
                random.choice(LOCATIONS),
            ])

    print(f"Generated {ROW_COUNT} rows at {OUTPUT}")


if __name__ == "__main__":
    main()
