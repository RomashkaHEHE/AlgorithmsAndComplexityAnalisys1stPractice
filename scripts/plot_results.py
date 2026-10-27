"""Build tables, figures and a report from actual Java measurements."""

import csv
import math
import statistics
from collections import defaultdict
from pathlib import Path

import matplotlib

matplotlib.use("Agg")
import matplotlib.pyplot as plt


ROOT = Path(__file__).resolve().parents[1]
RESULTS = ROOT / "results"
FIGURES = RESULTS / "figures"
LABELS = {
    "RANDOM": "Случайные int",
    "REVERSED": "Обратный порядок",
    "SORTED": "Уже отсортированный",
    "FEW_UNIQUE": "Случайные значения 0–99",
    "ALL_EQUAL": "Все элементы равны",
    "NEARLY_SORTED": "Почти отсортированный (1% обменов)",
}
COLORS = {"BubbleSort": "#c94c4c", "QuickSort": "#167c80"}


def write_csv(path, records):
    with path.open("w", encoding="utf-8", newline="") as stream:
        writer = csv.DictWriter(stream, fieldnames=list(records[0]))
        writer.writeheader()
        writer.writerows(records)


def main():
    FIGURES.mkdir(parents=True, exist_ok=True)
    groups = defaultdict(list)
    with (RESULTS / "raw.csv").open(encoding="utf-8", newline="") as stream:
        reader = csv.DictReader(stream)
        seen = set()
        for record in reader:
            key = (record["scenario"], int(record["n"]), record["algorithm"])
            identity = (*key, int(record["sample"]))
            if identity in seen:
                raise ValueError(f"Duplicate sample: {identity}")
            seen.add(identity)
            elapsed = int(record["elapsed_ns"])
            if elapsed <= 0:
                raise ValueError("Non-positive timing")
            groups[key].append(elapsed)
    if not groups:
        raise ValueError("No measurements")
    sizes = sorted({key[1] for key in groups})
    sample_counts = {len(values) for values in groups.values()}
    if len(sample_counts) != 1 or min(sample_counts) < 5:
        raise ValueError("At least five samples and equal group sizes are required")
    summary = []
    lookup = {}
    growth = []
    for scenario in LABELS:
        for algorithm in COLORS:
            previous = None
            for size in sizes:
                values = groups[(scenario, size, algorithm)]
                q1, _, q3 = statistics.quantiles(values, n=4, method="inclusive")
                median = statistics.median(values)
                record = {
                    "scenario": scenario, "n": size, "algorithm": algorithm,
                    "samples": len(values), "median_ns": median,
                    "q1_ns": q1, "q3_ns": q3,
                    "min_ns": min(values), "max_ns": max(values),
                    "mean_ns": statistics.mean(values),
                    "stdev_ns": statistics.stdev(values),
                }
                summary.append(record)
                lookup[(scenario, size, algorithm)] = record
                if previous is not None:
                    growth.append({
                        "scenario": scenario, "algorithm": algorithm,
                        "n_from": previous[0], "n_to": size,
                        "time_ratio": median / previous[1],
                        "linear_reference": size / previous[0],
                        "quadratic_reference": (size / previous[0]) ** 2,
                        "n_log_n_reference": size * math.log2(size)
                            / (previous[0] * math.log2(previous[0])),
                    })
                previous = (size, median)
    write_csv(RESULTS / "summary.csv", summary)
    write_csv(RESULTS / "growth.csv", growth)

    plt.rcParams.update({"font.family": "DejaVu Sans", "font.size": 10})
    figure, axes = plt.subplots(3, 2, figsize=(13, 12), constrained_layout=True)
    for axis, (scenario, title) in zip(axes.flat, LABELS.items()):
        for algorithm, color in COLORS.items():
            rows = [lookup[(scenario, size, algorithm)] for size in sizes]
            axis.plot(sizes, [r["median_ns"] / 1e6 for r in rows],
                      "o-", label=algorithm, color=color, markersize=4)
            axis.fill_between(sizes, [r["q1_ns"] / 1e6 for r in rows],
                              [r["q3_ns"] / 1e6 for r in rows], color=color, alpha=0.15)
        axis.set(xscale="log", yscale="log", title=title,
                 xlabel="Размер массива N", ylabel="Время сортировки, мс")
        axis.set_xticks(sizes, [str(size) for size in sizes], rotation=35)
        axis.grid(True, which="both", alpha=0.2)
        axis.legend()
    figure.suptitle("BubbleSort и QuickSort: медиана времени, полоса — Q1–Q3", fontsize=14)
    figure.savefig(FIGURES / "timings.png", dpi=160)
    plt.close(figure)

    figure, axes = plt.subplots(1, 3, figsize=(15, 5), constrained_layout=True)
    specifications = [
        ("BubbleSort", ["RANDOM", "REVERSED", "FEW_UNIQUE", "NEARLY_SORTED"],
         lambda n: n * n, "BubbleSort: T(N) / N²", "нс / N²"),
        ("BubbleSort", ["SORTED", "ALL_EQUAL"],
         lambda n: n, "BubbleSort: T(N) / N", "нс / N"),
        ("QuickSort", list(LABELS), lambda n: n * math.log2(n),
         "QuickSort: T(N) / (N log₂ N)", "нс / (N log₂ N)"),
    ]
    for axis, (algorithm, scenarios, divisor, title, ylabel) in zip(axes, specifications):
        for scenario in scenarios:
            axis.plot(sizes, [lookup[(scenario, n, algorithm)]["median_ns"] / divisor(n)
                             for n in sizes], "o-", label=LABELS[scenario], markersize=3)
        axis.set(xscale="log", xlabel="Размер массива N", ylabel=ylabel, title=title)
        axis.grid(True, alpha=0.2)
        axis.legend(fontsize=7)
    figure.suptitle("Нормировка времени: горизонтальный участок согласуется с моделью роста", fontsize=12)
    figure.savefig(FIGURES / "normalized.png", dpi=160)
    plt.close(figure)

    maximum = sizes[-1]
    lines = [
        "# Результаты измерений", "",
        "Файл построен автоматически по `raw.csv`; времена не являются теоретическими оценками.", "",
        f"Размеры: {', '.join(map(str, sizes))}. Повторений на точку: {next(iter(sample_counts))}.",
        "Единица исходных измерений — наносекунда. В таблице ниже — миллисекунды.", "",
        "![Время сортировки](figures/timings.png)", "",
        "![Нормированное время](figures/normalized.png)", "",
        f"## Сравнение при N = {maximum}", "",
        "| Вход | BubbleSort, медиана мс | QuickSort, медиана мс | Bubble / Quick | Рост Bubble при удвоении N | Рост Quick при удвоении N |",
        "|---|---:|---:|---:|---:|---:|",
    ]
    for scenario, label in LABELS.items():
        bubble = lookup[(scenario, maximum, "BubbleSort")]["median_ns"]
        quick = lookup[(scenario, maximum, "QuickSort")]["median_ns"]
        if len(sizes) > 1:
            previous = sizes[-2]
            bubble_growth = bubble / lookup[(scenario, previous, "BubbleSort")]["median_ns"]
            quick_growth = quick / lookup[(scenario, previous, "QuickSort")]["median_ns"]
            ratios = f"{bubble_growth:.2f} | {quick_growth:.2f}"
        else:
            ratios = "— | —"
        lines.append(f"| {label} | {bubble / 1e6:.6f} | {quick / 1e6:.6f} | {bubble / quick:.2f} | {ratios} |")
    lines += [
        "", "## Как читать результаты", "",
        "- Для квадратичного времени ориентир при удвоении N — 4, для линейного — 2.",
        "- Для N log₂ N ориентир — 2 · log₂(2N) / log₂(N), немного больше 2.",
        "- У BubbleSort есть ранний выход: отсортированный массив и равные элементы дают один проход.",
        "- Почти отсортированный вход здесь получен дальними случайными обменами. Он не гарантирует линейного времени BubbleSort.",
        "- QuickSort выбирает средний по индексу элемент. Обратный порядок не является автоматически его худшим случаем.",
        "- Равные элементы в этой двухсторонней реализации QuickSort всё равно обмениваются и делятся на подзадачи.",
        "- Измерение не доказывает асимптотику и не исключает O(N²) на специально подобранном входе QuickSort.",
        "- На малых временах заметны JIT, таймер, кэш и фоновые процессы. Используются медиана и межквартильный диапазон; выбросы не удалялись.",
        "- Это учебный эксперимент в одном процессе JVM, а не статистическое исследование с независимыми JVM-fork и JMH.",
        "", "## Воспроизводимость", "",
        "`raw.csv` — каждый замер; `summary.csv` — статистика; `growth.csv` — отношения времени при росте N.",
        "Среда и параметры записаны в `environment.txt`. Генерация, копирование массива и проверка результата не входят в измеряемый участок.",
        "Для обоих алгоритмов в каждом повторении использован одинаковый вход; случайные входы между повторениями меняются по фиксированным seed.",
        "Перезапуск заменяет результаты. На другой машине точные времена будут отличаться.", "",
    ]
    (RESULTS / "REPORT.md").write_text("\n".join(lines), encoding="utf-8")
    print(f"Report ready: {len(seen)} timings, {len(summary)} summary rows, 2 figures")


if __name__ == "__main__":
    main()
