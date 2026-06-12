import csv
import json
from collections import Counter
from datetime import datetime
from pathlib import Path


DATASET = Path("data/sample/yagi_complex_random.csv")
OUTPUT = Path("data/sample/yagi_complex_random_statistics.md")
START_DATE = "2024-09-01"
END_DATE = "2024-09-30"


def percent(value: int, total: int) -> str:
    return f"{value * 100 / total:.1f}%" if total else "0.0%"


def markdown_table(headers, rows) -> str:
    result = [
        "| " + " | ".join(headers) + " |",
        "| " + " | ".join("---" for _ in headers) + " |",
    ]
    result.extend("| " + " | ".join(str(value) for value in row) + " |" for row in rows)
    return "\n".join(result)


def category_counts(config_path: Path, texts: list[str]):
    with config_path.open(encoding="utf-8") as config_file:
        categories = json.load(config_file)["categories"]

    counts = []
    for category in categories:
        count = sum(
            any(keyword.lower() in text for keyword in category["keywords"])
            for text in texts
        )
        counts.append((category["nameVi"], count))
    return sorted(counts, key=lambda item: item[1], reverse=True)


def main() -> None:
    with DATASET.open(encoding="utf-8", newline="") as dataset_file:
        rows = list(csv.DictReader(dataset_file))

    total = len(rows)
    texts = [row["content"].lower() for row in rows]
    timestamps = [
        datetime.strptime(row["timestamp"], "%Y-%m-%d %H:%M:%S")
        for row in rows
    ]
    in_range = [
        row for row in rows
        if START_DATE <= row["timestamp"][:10] <= END_DATE
    ]
    platforms = Counter(row["platform"] for row in rows)
    locations = Counter(row["location"] for row in rows)
    authors = Counter(row["author"] for row in rows)
    dates = Counter(row["timestamp"][:10] for row in rows)
    likes = [int(row["likes"]) for row in rows]
    shares = [int(row["shares"]) for row in rows]
    comments = [int(row["comments"]) for row in rows]

    damage = category_counts(Path("config/damage-categories.json"), texts)
    relief = category_counts(Path("config/relief-categories.json"), texts)

    difficult_cases = [
        ("Cảm xúc pha trộn", sum("rất tận tình nhưng thực phẩm đến chậm" in text for text in texts)),
        ("Mỉa mai", sum("quá tuyệt vời... chờ ba ngày" in text for text in texts)),
        ("Phủ định/đánh giá dè dặt", sum("không tệ như tin đồn" in text for text in texts)),
        ("Có URL", sum("http" in text for text in texts)),
        ("Có hashtag", sum("#" in text for text in texts)),
        ("Có HTML", sum("<p>" in text or "</p>" in text for text in texts)),
        ("Có emoji", sum(any(symbol in text for symbol in ["😢", "❤️"]) for text in texts)),
        ("Nội dung quảng cáo gây nhiễu", sum("quảng cáo" in text for text in texts)),
    ]

    top_engagement = sorted(
        rows,
        key=lambda row: int(row["likes"]) + int(row["shares"]) + int(row["comments"]),
        reverse=True,
    )[:10]

    report = [
        "# Thống kê bộ dữ liệu `yagi_complex_random.csv`",
        "",
        "## Tổng quan",
        "",
        markdown_table(
            ["Chỉ số", "Giá trị"],
            [
                ("Tổng số bài đăng", total),
                ("Số tác giả khác nhau", len(authors)),
                ("Số nền tảng", len(platforms)),
                ("Số địa điểm", len(locations)),
                ("Bài nằm trong khoảng cấu hình", f"{len(in_range)} ({percent(len(in_range), total)})"),
                ("Bài ngoài khoảng cấu hình", f"{total - len(in_range)} ({percent(total - len(in_range), total)})"),
                ("Thời điểm sớm nhất", min(timestamps).strftime("%d/%m/%Y %H:%M")),
                ("Thời điểm muộn nhất", max(timestamps).strftime("%d/%m/%Y %H:%M")),
            ],
        ),
        "",
        "## Phân bố theo nền tảng",
        "",
        markdown_table(
            ["Nền tảng", "Số bài", "Tỷ lệ"],
            [(name, count, percent(count, total)) for name, count in platforms.most_common()],
        ),
        "",
        "## Phân bố theo địa điểm",
        "",
        markdown_table(
            ["Địa điểm", "Số bài", "Tỷ lệ"],
            [(name, count, percent(count, total)) for name, count in locations.most_common()],
        ),
        "",
        "## Tác giả xuất hiện nhiều nhất",
        "",
        markdown_table(
            ["Tác giả", "Số bài", "Tỷ lệ"],
            [(name, count, percent(count, total)) for name, count in authors.most_common(10)],
        ),
        "",
        "## Ngày có nhiều bài đăng nhất",
        "",
        markdown_table(
            ["Ngày", "Số bài"],
            [(date, count) for date, count in dates.most_common(10)],
        ),
        "",
        "## Thống kê tương tác",
        "",
        markdown_table(
            ["Chỉ số", "Tổng", "Trung bình", "Lớn nhất"],
            [
                ("Lượt thích", sum(likes), f"{sum(likes) / total:.1f}", max(likes)),
                ("Lượt chia sẻ", sum(shares), f"{sum(shares) / total:.1f}", max(shares)),
                ("Bình luận", sum(comments), f"{sum(comments) / total:.1f}", max(comments)),
            ],
        ),
        "",
        "## Độ phủ nhóm thiệt hại",
        "",
        "> Một bài đăng có thể thuộc nhiều nhóm, vì vậy tổng tỷ lệ có thể lớn hơn 100%.",
        "",
        markdown_table(
            ["Nhóm thiệt hại", "Số bài khớp", "Tỷ lệ"],
            [(name, count, percent(count, total)) for name, count in damage],
        ),
        "",
        "## Độ phủ nhóm cứu trợ",
        "",
        "> Một bài đăng có thể thuộc nhiều nhóm, vì vậy tổng tỷ lệ có thể lớn hơn 100%.",
        "",
        markdown_table(
            ["Nhóm cứu trợ", "Số bài khớp", "Tỷ lệ"],
            [(name, count, percent(count, total)) for name, count in relief],
        ),
        "",
        "## Các trường hợp nội dung khó",
        "",
        markdown_table(
            ["Loại trường hợp", "Số bài", "Tỷ lệ"],
            [(name, count, percent(count, total)) for name, count in difficult_cases],
        ),
        "",
        "## Bài đăng có tổng tương tác cao nhất",
        "",
        markdown_table(
            ["ID", "Nền tảng", "Tác giả", "Địa điểm", "Tổng tương tác"],
            [
                (
                    row["id"],
                    row["platform"],
                    row["author"],
                    row["location"],
                    int(row["likes"]) + int(row["shares"]) + int(row["comments"]),
                )
                for row in top_engagement
            ],
        ),
        "",
        "## Lưu ý khi đánh giá kết quả phân tích",
        "",
        "- Tập dữ liệu được sinh tổng hợp để kiểm thử, không đại diện cho dữ liệu mạng xã hội thực tế.",
        "- Tần suất đề cập không đồng nghĩa với mức độ thiệt hại thực tế.",
        "- Các câu phủ định, mỉa mai và cảm xúc pha trộn được đưa vào để kiểm tra giới hạn của mô hình sentiment.",
        "- Các tên tác giả là tên tổng hợp giống tên người thật, không đại diện cho cá nhân cụ thể.",
        "",
    ]

    OUTPUT.write_text("\n".join(report), encoding="utf-8")
    print(f"Generated statistics at {OUTPUT}")


if __name__ == "__main__":
    main()
