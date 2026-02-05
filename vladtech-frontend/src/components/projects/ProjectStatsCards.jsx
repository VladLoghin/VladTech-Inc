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

const ProjectStatsCards = ({ stats, onStatClick, viewMode = 'status', onViewModeChange }) => {
  const { t } = useTranslation();

  if (!stats) {
    return null;
  }

  return (
    <div>
      {/* Define items based on view mode */}
      {(() => {
        let items = [];
        
        if (viewMode === 'status') {
          items = [
            { label: t("admin.stats.total"), value: stats.activeCount || stats.total, filterType: "total" },
            { label: t("admin.stats.pending"), value: stats.pendingCount, filterType: "status", filterValue: "PENDING" },
            { label: t("admin.stats.inProgress"), value: stats.inProgressCount, filterType: "status", filterValue: "IN_PROGRESS" },
            { label: t("admin.stats.completed"), value: stats.completedCount, filterType: "status", filterValue: "COMPLETED" },
            { label: t("admin.stats.overdue"), value: stats.overdueCount, isError: true, filterType: "overdue" },
          ];
        } else if (viewMode === 'priority') {
          items = [
            { label: "Total", value: stats.total, filterType: "total" },
            { label: "Low", value: stats.lowCount, filterType: "priority", filterValue: "LOW" },
            { label: "Medium", value: stats.mediumCount, filterType: "priority", filterValue: "MEDIUM" },
            { label: "High", value: stats.highCount, filterType: "priority", filterValue: "HIGH" },
            { label: "Urgent", value: stats.urgentCount, filterType: "priority", filterValue: "URGENT", isError: true },
          ];
        } else if (viewMode === 'projectType') {
          items = [
            { label: "Total", value: stats.total, filterType: "total" },
            { label: "Appointment", value: stats.appointmentCount, filterType: "projectType", filterValue: "APPOINTMENT" },
            { label: "Scheduled", value: stats.scheduledCount, filterType: "projectType", filterValue: "SCHEDULED" },
          ];
        }

        // Data for charts - changes based on view mode
        let activeData = [];
        let totalCount = 0;
        
        if (viewMode === 'status') {
          totalCount = stats.activeCount || stats.total || 0;
          activeData = [
            { 
              name: t("admin.stats.pending"), 
              value: stats.pendingCount || 0, 
              color: "#f59e0b",
              percentage: totalCount > 0 ? Math.round(((stats.pendingCount || 0) / totalCount) * 100) : 0
            },
            { 
              name: t("admin.stats.inProgress"), 
              value: stats.inProgressCount || 0, 
              color: "#3b82f6",
              percentage: totalCount > 0 ? Math.round(((stats.inProgressCount || 0) / totalCount) * 100) : 0
            },
            { 
              name: t("admin.stats.completed"), 
              value: stats.completedCount || 0, 
              color: "#10b981",
              percentage: totalCount > 0 ? Math.round(((stats.completedCount || 0) / totalCount) * 100) : 0
            },
          ];
        } else if (viewMode === 'priority') {
          totalCount = stats.total || 0;
          activeData = [
            { 
              name: "Low", 
              value: stats.lowCount || 0, 
              color: "#10b981",
              percentage: totalCount > 0 ? Math.round(((stats.lowCount || 0) / totalCount) * 100) : 0
            },
            { 
              name: "Medium", 
              value: stats.mediumCount || 0, 
              color: "#f59e0b",
              percentage: totalCount > 0 ? Math.round(((stats.mediumCount || 0) / totalCount) * 100) : 0
            },
            { 
              name: "High", 
              value: stats.highCount || 0, 
              color: "#ef4444",
              percentage: totalCount > 0 ? Math.round(((stats.highCount || 0) / totalCount) * 100) : 0
            },
            { 
              name: "Urgent", 
              value: stats.urgentCount || 0, 
              color: "#991b1b",
              percentage: totalCount > 0 ? Math.round(((stats.urgentCount || 0) / totalCount) * 100) : 0
            },
          ];
        } else if (viewMode === 'projectType') {
          totalCount = stats.total || 0;
          activeData = [
            { 
              name: "Appointment", 
              value: stats.appointmentCount || 0, 
              color: "#8b5cf6",
              percentage: totalCount > 0 ? Math.round(((stats.appointmentCount || 0) / totalCount) * 100) : 0
            },
            { 
              name: "Scheduled", 
              value: stats.scheduledCount || 0, 
              color: "#3b82f6",
              percentage: totalCount > 0 ? Math.round(((stats.scheduledCount || 0) / totalCount) * 100) : 0
            },
          ];
        }

        // Chart titles based on view mode
        const chartTitle = viewMode === 'status' 
          ? t("admin.stats.activeProjectsDistribution")
          : viewMode === 'priority'
          ? "Priority Distribution"
          : "Project Type Distribution";

        const chartSubtitle = viewMode === 'status'
          ? t("admin.stats.statusBreakdown")
          : viewMode === 'priority'
          ? "Priority Breakdown"
          : "Type Breakdown";

        return (
          <div className="w-full bg-white border-2 border-black rounded-lg p-6">
            {/* View Mode Tabs */}
            <div className="flex items-center gap-2 mb-6">
              <span className="text-sm font-semibold text-gray-600">View:</span>
              <div className="flex border-2 border-black rounded-lg overflow-hidden">
                <button
                  onClick={() => onViewModeChange('status')}
                  className={`px-4 py-2 font-semibold transition-all ${
                    viewMode === 'status' 
                      ? 'bg-black text-white' 
                      : 'bg-white text-black hover:bg-gray-100'
                  }`}
                >
                  Status
                </button>
                <button
                  onClick={() => onViewModeChange('priority')}
                  className={`px-4 py-2 font-semibold transition-all border-l-2 border-black ${
                    viewMode === 'priority' 
                      ? 'bg-black text-white' 
                      : 'bg-white text-black hover:bg-gray-100'
                  }`}
                >
                  Priority
                </button>
                <button
                  onClick={() => onViewModeChange('projectType')}
                  className={`px-4 py-2 font-semibold transition-all border-l-2 border-black ${
                    viewMode === 'projectType' 
                      ? 'bg-black text-white' 
                      : 'bg-white text-black hover:bg-gray-100'
                  }`}
                >
                  Project Type
                </button>
              </div>
            </div>

            {/* Stats Row */}
      <div className="grid grid-cols-2 gap-y-6 md:flex md:flex-row md:items-center md:justify-start md:gap-0 mb-8">
        {items.map((item, index) => (
          <button
            key={index}
            onClick={() => onStatClick?.(item.filterType, item.filterValue)}
            className="flex flex-col md:px-8 md:first:pl-0 md:border-r-2 md:last:border-r-0 border-gray-100 min-w-0 md:min-w-[120px] first:col-span-2 md:first:col-span-1 text-left cursor-pointer hover:bg-gray-50 active:bg-gray-100 transition-colors rounded-lg p-2 -m-2"
            aria-label={`Filter by ${item.label}`}
          >
            <span className="text-xs font-bold text-gray-400 uppercase tracking-widest mb-1">
              {item.label}
            </span>
            <span className={`text-4xl font-black leading-none ${item.isError ? 'text-red-500' : 'text-gray-900'}`}>
              {item.value}
            </span>
          </button>
        ))}
      </div>

      {/* Charts Row - Hidden on mobile, visible on medium+ screens */}
      <div className="hidden md:grid grid-cols-1 lg:grid-cols-2 gap-6 pt-6 border-t-2 border-gray-100">
        {/* Circle Chart */}
        <div>
          <h3 className="text-sm font-bold text-gray-600 uppercase tracking-wider mb-4">{chartTitle}</h3>
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
          <h3 className="text-sm font-bold text-gray-600 uppercase tracking-wider mb-4">{chartSubtitle}</h3>
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
      })()}
    </div>
  );
};

export default ProjectStatsCards;
