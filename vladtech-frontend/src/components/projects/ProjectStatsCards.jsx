import { useTranslation } from "react-i18next";
import {
  PieChart,
  Pie,
  Cell,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  Label,
} from "recharts";

// Custom tooltip component - defined outside to avoid recreation on each render
const CustomTooltip = ({ active, payload }) => {
  if (active && payload && payload.length) {
    const data = payload[0].payload;
    return (
      <div className="bg-white border-2 border-black rounded-lg px-3 py-2 shadow-lg">
        <p className="font-semibold text-sm">{data.name}</p>
        <p className="text-lg font-bold" style={{ color: data.color }}>
          {data.value} ({data.percentage}%)
        </p>
      </div>
    );
  }
  return null;
};

// Custom label renderer for donut chart
const renderLabel = ({ cx, cy, midAngle, innerRadius, outerRadius, payload }) => {
  if (payload.percentage < 5) return null;
  const RADIAN = Math.PI / 180;
  const radius = innerRadius + (outerRadius - innerRadius) * 0.5;
  const x = cx + radius * Math.cos(-midAngle * RADIAN);
  const y = cy + radius * Math.sin(-midAngle * RADIAN);

  return (
    <text
      x={x}
      y={y}
      fill="white"
      textAnchor="middle"
      dominantBaseline="central"
      className="text-sm font-bold"
    >
      {`${payload.percentage}%`}
    </text>
  );
};

const ProjectStatsCards = ({ stats }) => {
  const { t } = useTranslation();

  if (!stats) {
    return null;
  }

  const items = [
    { label: t("admin.stats.total"), value: stats.activeCount },
    { label: t("admin.stats.pending"), value: stats.pendingCount },
    { label: t("admin.stats.inProgress"), value: stats.inProgressCount },
    { label: t("admin.stats.completed"), value: stats.completedCount },
    { label: t("admin.stats.overdue"), value: stats.overdueCount, isError: true },
  ];

  // Data for active projects (excluding archived)
  // Percentages are calculated based on ACTIVE projects (ignoring archived)
  const activeData = [
    { 
      name: t("admin.stats.pending"), 
      value: stats.pendingCount, 
      color: "#f59e0b",
      percentage: stats.activeCount > 0 ? Math.round((stats.pendingCount / stats.activeCount) * 100) : 0
    },
    { 
      name: t("admin.stats.inProgress"), 
      value: stats.inProgressCount, 
      color: "#3b82f6",
      percentage: stats.activeCount > 0 ? Math.round((stats.inProgressCount / stats.activeCount) * 100) : 0
    },
    { 
      name: t("admin.stats.completed"), 
      value: stats.completedCount, 
      color: "#10b981",
      percentage: stats.activeCount > 0 ? Math.round((stats.completedCount / stats.activeCount) * 100) : 0
    },
  ];

  return (
    <div className="w-full bg-white border-2 border-black rounded-lg p-6">
      {/* Stats Row */}
      <div className="grid grid-cols-2 gap-y-6 md:flex md:flex-row md:items-center md:justify-start md:gap-0 mb-8">
        {items.map((item, index) => (
          <div 
            key={index} 
            className="flex flex-col md:px-8 md:first:pl-0 md:border-r-2 md:last:border-r-0 border-gray-100 min-w-0 md:min-w-[120px] first:col-span-2 md:first:col-span-1"
          >
            <span className="text-xs font-bold text-gray-400 uppercase tracking-widest mb-1">
              {item.label}
            </span>
            <span className={`text-4xl font-black leading-none ${item.isError ? 'text-red-500' : 'text-gray-900'}`}>
              {item.value}
            </span>
          </div>
        ))}
      </div>

      {/* Charts Row - Hidden on mobile, visible on medium+ screens */}
      <div className="hidden md:grid grid-cols-1 lg:grid-cols-2 gap-6 pt-6 border-t-2 border-gray-100">
        {/* Circle Chart */}
        <div>
          <h3 className="text-sm font-bold text-gray-600 uppercase tracking-wider mb-4">{t("admin.stats.activeProjectsDistribution")}</h3>
          <ResponsiveContainer width="100%" height={250}>
            <PieChart>
              <Pie
                data={activeData}
                cx="50%"
                cy="50%"
                innerRadius={50}
                outerRadius={90}
                paddingAngle={3}
                dataKey="value"
                label={renderLabel}
                labelLine={false}
              >
                {activeData.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={entry.color} />
                ))}
              </Pie>
              <Tooltip content={<CustomTooltip />} />
            </PieChart>
          </ResponsiveContainer>
          <div className="flex justify-center gap-4 mt-2">
            {activeData.map((entry, index) => (
              <div key={index} className="flex items-center gap-1.5">
                <div className="w-3 h-3 rounded-full" style={{ backgroundColor: entry.color }} />
                <span className="text-xs font-medium">{entry.name}: {entry.value} ({entry.percentage}%)</span>
              </div>
            ))}
          </div>
        </div>

        {/* Bar Chart */}
        <div>
          <h3 className="text-sm font-bold text-gray-600 uppercase tracking-wider mb-4">{t("admin.stats.statusBreakdown")}</h3>
          <ResponsiveContainer width="100%" height={250}>
            <BarChart data={activeData}>
              <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
              <XAxis dataKey="name" tick={{ fontSize: 12 }} />
              <YAxis tick={{ fontSize: 12 }} />
              <Tooltip content={<CustomTooltip />} />
              <Bar dataKey="value" radius={[6, 6, 0, 0]}>
                {activeData.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={entry.color} />
                ))}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>
    </div>
  );
};

export default ProjectStatsCards;
