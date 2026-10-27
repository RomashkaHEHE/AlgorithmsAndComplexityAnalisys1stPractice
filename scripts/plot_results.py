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
    "# Сравнение BubbleSort и QuickSort", "",
    "Для каждого размера взято среднее время пяти запусков. Обе сортировки получают копии одного массива.",
    "Генерация, копирование и проверка результата не входят в замер. Время измеряется через System.nanoTime.", "",
    "| Вход | N | BubbleSort, мс | QuickSort, мс |",
    "|---|---:|---:|---:|",
]
for row in rows:
    title = "Случайный" if row["type"] == "random" else "Обратный"
    report.append(f"| {title} | {row['n']} | {row['bubble_ms']} | {row['quick_ms']} |")
report += [
    "", "![Случайный массив](random.png)", "", "![Обратный порядок](reversed.png)", "",
    "## Вывод", "",
    "На больших массивах QuickSort быстрее. У BubbleSort время растёт примерно квадратично: число сравнений может достигать N(N−1)/2.",
    "Среднее время QuickSort — O(N log N), худшее — O(N²). Здесь pivot берётся из середины по индексу, поэтому обратный массив не обязательно является худшим входом.",
    "На небольших размерах измерения менее показательны: на время влияют JVM и нагрузка компьютера. Специального прогрева и отдельного поиска худшего входа QuickSort в работе нет.", "",
]
(folder / "REPORT.md").write_text("\n".join(report), encoding="utf-8")
print("Graphs and report saved to results")
