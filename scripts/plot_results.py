import csv
from pathlib import Path

import matplotlib.pyplot as plt


folder = Path(__file__).resolve().parents[1] / "results"
with (folder / "results.csv").open() as file:
    rows = list(csv.DictReader(file))

for kind, title in [("random", "Случайный массив"), ("reversed", "Обратный порядок")]:
    data = [row for row in rows if row["type"] == kind]
    n = [int(row["n"]) for row in data]
    bubble = [float(row["bubble_ms"]) for row in data]
    quick = [float(row["quick_ms"]) for row in data]
    plt.figure(figsize=(8, 5))
    plt.plot(n, bubble, "o-", label="BubbleSort")
    plt.plot(n, quick, "o-", label="QuickSort")
    plt.title(title)
    plt.xlabel("Количество элементов N")
    plt.ylabel("Среднее время, мс")
    plt.grid()
    plt.legend()
    plt.tight_layout()
    plt.savefig(folder / (kind + ".png"), dpi=140)
    plt.close()

report = [
    "| Вход | N | BubbleSort, мс | QuickSort, мс |",
    "|---|---:|---:|---:|",
]
for row in rows:
    title = "Случайный" if row["type"] == "random" else "Обратный"
    report.append(f"| {title} | {row['n']} | {row['bubble_ms']} | {row['quick_ms']} |")
report += [
    "", "![Случайный массив](random.png)", "", "![Обратный порядок](reversed.png)", "",
]
(folder / "REPORT.md").write_text("\n".join(report), encoding="utf-8")
