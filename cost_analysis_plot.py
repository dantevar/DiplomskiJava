import pandas as pd
import numpy as np
import matplotlib
matplotlib.use('Agg')  # Non-interactive backend
import matplotlib.pyplot as plt
from scipy.optimize import curve_fit

# Učitaj CSV
df = pd.read_csv('cost_analysis_results.csv')

print("Učitani podaci:")
print(df.to_string())

# Podaci
n = df['N'].values
avg_cost = df['AvgCost'].values
stdev = df['Stdev'].values

# Definiraj fit funkcije
def asymptotic(x, C, a):
    return C - a / x

def log_fit(x, a, b):
    return a * np.log(x) + b

def sqrt_fit(x, a, b):
    return a * np.sqrt(x) + b

# Fitiranje asimptotske funkcije za avg
popt_asym, _ = curve_fit(asymptotic, n, avg_cost, p0=[1.8, 1.0])
C_asym, a_asym = popt_asym
print(f"\nAsimptotski fit za Avg: cost(n) = {C_asym:.4f} - {a_asym:.4f}/n")

# Fitiranje log funkcije za avg
popt_log, _ = curve_fit(log_fit, n, avg_cost)
a_log, b_log = popt_log
print(f"Log fit za Avg: cost(n) = {a_log:.4f}*ln(n) + {b_log:.4f}")

# Fitiranje sqrt funkcije za stdev
popt_stdev, _ = curve_fit(sqrt_fit, n, stdev, p0=[0.1, 0.1])
a_stdev, b_stdev = popt_stdev
print(f"Sqrt fit za Stdev: stdev(n) = {a_stdev:.4f}*sqrt(n) + {b_stdev:.4f}")

# Generiraj N vrijednosti do 40
n_extended = np.linspace(4, 40, 100)

# Izračunaj predikcije
avg_pred_asym = asymptotic(n_extended, C_asym, a_asym)
stdev_pred = sqrt_fit(n_extended, a_stdev, b_stdev)

# Kreiraj graf
fig, ax1 = plt.subplots(figsize=(12, 7))

# Primarna Y os - Avg Cost
color_avg = '#2E86AB'
ax1.set_xlabel('N (broj čvorova)', fontsize=12)
ax1.set_ylabel('Prosječna cijena (Avg Cost)', color=color_avg, fontsize=12)

# Empirijski podaci - avg
ax1.scatter(n, avg_cost, color=color_avg, s=80, zorder=5, label='Avg Cost (podaci)', edgecolors='black', linewidth=0.5)

# Fitted funkcija - avg (do N=40)
ax1.plot(n_extended, avg_pred_asym, color=color_avg, linestyle='--', linewidth=2, 
         label=f'Fit: C - a/n = {C_asym:.3f} - {a_asym:.3f}/n')

# Horizontalna linija za asimptotsku vrijednost
ax1.axhline(y=C_asym, color=color_avg, linestyle=':', alpha=0.7, 
            label=f'Asimptota C = {C_asym:.3f}')

ax1.tick_params(axis='y', labelcolor=color_avg)
ax1.set_ylim(1.4, 1.9)

# Sekundarna Y os - Stdev
ax2 = ax1.twinx()
color_stdev = '#E94F37'
ax2.set_ylabel('Standardna devijacija (Stdev)', color=color_stdev, fontsize=12)

# Empirijski podaci - stdev
ax2.scatter(n, stdev, color=color_stdev, s=80, zorder=5, marker='s', 
            label='Stdev (podaci)', edgecolors='black', linewidth=0.5)

# Fitted funkcija - stdev (do N=40)
ax2.plot(n_extended, stdev_pred, color=color_stdev, linestyle='--', linewidth=2,
         label=f'Fit: a√n + b = {a_stdev:.3f}√n + {b_stdev:.3f}')

ax2.tick_params(axis='y', labelcolor=color_stdev)
ax2.set_ylim(0.2, 0.55)

# Naslov
plt.title('MCW Optimalna Cijena i Varijabilnost vs Broj Čvorova\n(1000 instanci po N)', 
          fontsize=14, fontweight='bold')

# Kombinirana legenda
lines1, labels1 = ax1.get_legend_handles_labels()
lines2, labels2 = ax2.get_legend_handles_labels()
ax1.legend(lines1 + lines2, labels1 + labels2, loc='upper left', fontsize=10)

# Grid
ax1.grid(True, alpha=0.3)
ax1.set_xlim(3, 42)

# Označi ekstrapoliciju
ax1.axvline(x=23, color='gray', linestyle=':', alpha=0.5)
ax1.text(23.5, 1.45, '← Podaci | Ekstrapolacija →', fontsize=9, color='gray')

plt.tight_layout()
plt.savefig('cost_analysis_graph.png', dpi=150, bbox_inches='tight')
# plt.show()  # Commented out - using non-interactive backend

print(f"\n📊 Graf spremljen kao: cost_analysis_graph_exp.png")

# Predikcije
print("\n" + "="*50)
print("PREDIKCIJE ZA VEĆE N:")
print("="*50)
print(f"{'N':>5} {'Pred Avg':>12} {'Pred Stdev':>12}")
print("-"*30)
for n_val in [25, 30, 35, 40, 50, 100]:
    pred_avg = asymptotic(n_val, C_asym, a_asym)
    pred_std = sqrt_fit(n_val, a_stdev, b_stdev)
    print(f"{n_val:>5} {pred_avg:>12.4f} {pred_std:>12.4f}")
