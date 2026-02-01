import { useMemo } from "react";
import { useTranslation } from "react-i18next";

const daysBetween = (from, to) => {
  const ms = to.getTime() - from.getTime();
  return Math.floor(ms / (1000 * 60 * 60 * 24));
};

const safeDate = (dateStr) => {
  if (!dateStr) return null;
  const d = new Date(dateStr);
  return Number.isNaN(d.getTime()) ? null : d;
};

const pct = (part, total) => {
  if (!total) return 0;
  return Math.round((part / total) * 100);
};

const Segment = ({ label, value, total, className }) => {
  const widthPct = total ? (value / total) * 100 : 0;

  return (
    <div className="flex flex-col gap-1">
      <div className="flex items-center justify-between text-xs font-semibold text-black/70">
        <span>{label}</span>
        <span>
          {value} ({pct(value, total)}%)
        </span>
      </div>
      <div className="h-3 w-full rounded-full bg-gray-200 overflow-hidden border border-black/10">
        <div className={`h-full ${className}`} style={{ width: `${widthPct}%` }} />
      </div>
    </div>
  );
};

const EmployeeProjectStatsCards = ({ projects, dueSoonDays = 7 }) => {
  const { t } = useTranslation();

  const stats = useMemo(() => {
    const now = new Date();
    const total = projects?.length || 0;

    let pending = 0;
    let inProgress = 0;
    let completed = 0;

    let overdue = 0;
    let dueSoon = 0;

    let activeCount = 0;
    let archivedCount = 0;

    (projects || []).forEach((p) => {
      const status = (p.status || "PENDING").toUpperCase();
      if (status === "COMPLETED") completed += 1;
      else if (status === "IN_PROGRESS") inProgress += 1;
      else pending += 1;

      const state = (p.state || "ACTIVE").toUpperCase();
      if (state === "COMPLETE") archivedCount += 1;
      else activeCount += 1;

      const due = safeDate(p.dueDate);
      if (!due) return;

      const days = daysBetween(now, due);

      if (days < 0 && state !== "COMPLETE") overdue += 1;
      if (days >= 0 && days <= dueSoonDays && state !== "COMPLETE") dueSoon += 1;
    });

    return {
      total,
      pending,
      inProgress,
      completed,
      overdue,
      dueSoon,
      activeCount,
      archivedCount,
    };
  }, [projects, dueSoonDays]);

  return (
    <div className="border-2 border-black rounded-xl bg-white p-6 shadow-md">
      <div className="flex items-start justify-between gap-4 flex-wrap">
        <div>
          <h2 className="text-2xl font-bold">
            {t("employee.dashboard.title", { defaultValue: "My Dashboard" })}
          </h2>
          <p className="text-sm text-black/60">
            {t("employee.dashboard.subtitle", {
              defaultValue: "Quick overview of your assigned projects.",
            })}
          </p>
        </div>

        <div className="flex gap-3 flex-wrap">
          <div className="px-4 py-3 rounded-xl border border-black/10 bg-gray-50 min-w-[140px]">
            <p className="text-xs font-bold text-black/60 uppercase">
              {t("employee.dashboard.total", { defaultValue: "Total" })}
            </p>
            <p className="text-2xl font-bold">{stats.total}</p>
          </div>

          <div className="px-4 py-3 rounded-xl border border-black/10 bg-gray-50 min-w-[140px]">
            <p className="text-xs font-bold text-black/60 uppercase">
              {t("employee.dashboard.dueSoon", { defaultValue: "Due Soon" })}
            </p>
            <p className="text-2xl font-bold">{stats.dueSoon}</p>
            <p className="text-xs text-black/50">
              {t("employee.dashboard.next7Days", { defaultValue: "Next 7 days" })}
            </p>
          </div>

          <div className="px-4 py-3 rounded-xl border border-black/10 bg-gray-50 min-w-[140px]">
            <p className="text-xs font-bold text-black/60 uppercase">
              {t("employee.dashboard.overdue", { defaultValue: "Overdue" })}
            </p>
            <p className="text-2xl font-bold">{stats.overdue}</p>
          </div>
        </div>
      </div>

      <div className="mt-6 grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="border border-black/10 rounded-xl p-4 bg-gray-50">
          <p className="text-sm font-bold mb-3">
            {t("employee.dashboard.statusBreakdown", { defaultValue: "Status breakdown" })}
          </p>

          <div className="space-y-3">
            <Segment
              label={t("project.pending", { defaultValue: "Pending" })}
              value={stats.pending}
              total={stats.total}
              className="bg-yellow-400"
            />
            <Segment
              label={t("project.inProgress", { defaultValue: "In Progress" })}
              value={stats.inProgress}
              total={stats.total}
              className="bg-blue-500"
            />
            <Segment
              label={t("project.completed", { defaultValue: "Completed" })}
              value={stats.completed}
              total={stats.total}
              className="bg-green-500"
            />
          </div>
        </div>

        <div className="border border-black/10 rounded-xl p-4 bg-gray-50">
          <p className="text-sm font-bold mb-3">
            {t("employee.dashboard.activity", { defaultValue: "Activity" })}
          </p>

          <div className="space-y-3">
            <Segment
              label={t("employee.dashboard.active", { defaultValue: "Active" })}
              value={stats.activeCount}
              total={stats.total}
              className="bg-black"
            />
            <Segment
              label={t("employee.dashboard.archived", { defaultValue: "Archived" })}
              value={stats.archivedCount}
              total={stats.total}
              className="bg-gray-500"
            />
          </div>
        </div>
      </div>
    </div>
  );
};

export default EmployeeProjectStatsCards;
