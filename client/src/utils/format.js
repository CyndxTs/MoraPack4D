export const formatCompactNumber = (num) => {
  const n = Number(num) || 0;
  if (n >= 1_000_000) return `${(n / 1_000_000).toFixed(1)}M`;
  if (n >= 1_000) return `${(n / 1_000).toFixed(1)}K`;
  return n.toLocaleString();
};

export const formatPercent = (ratio, decimals = 1) =>
  ratio == null || Number.isNaN(ratio) ? "-" : `${(ratio * 100).toFixed(decimals)}%`;

export const parseNumber = (v) => (v === "" || v === null || v === undefined ? null : Number(v));
