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
} from "recharts";

const ProjectStatsCharts = ({ stats }) => {
  const { t } = useTranslation();

  if (!stats) {
    return null;
  }

  // Data for active projects (excluding completed)
  const activeData = [
    { name: t("admin.stats.pending"), value: stats.pendingCount, color: "#f59e0b" },
    { name: t("admin.stats.inProgress"), value: stats.inProgressCount, color: "#3b82f6" },
    { name: t("admin.stats.overdue"), value: stats.overdueCount, color: "#ef4444" },
  ];

  return (
    <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
      {/* Circle Chart */}
      <div className="bg-white border-2 border-black rounded-lg p-6">
        <h3 className="text-lg font-bold mb-4">Active Projects Distribution</h3>
        <ResponsiveContainer width="100%" height={300}>
          <PieChart>
            <Pie
              data={activeData}
              cx="50%"
              cy="50%"
              innerRadius={60}
              outerRadius={100}
              paddingAngle={5}
              dataKey="value"
            >
              {activeData.map((entry, index) => (
                <Cell key={`cell-${index}`} fill={entry.color} />
              ))}
            </Pie>
            <Tooltip />
          </PieChart>
        </ResponsiveContainer>
        <div className="flex justify-center gap-4 mt-4">
          {activeData.map((entry, index) => (
            <div key={index} className="flex items-center gap-2">
              <div className="w-3 h-3 rounded-full" style={{ backgroundColor: entry.color }} />
              <span className="text-sm">{entry.name}: {entry.value}</span>
            </div>
          ))}
        </div>
      </div>

      {/* Bar Chart */}
      <div className="bg-white border-2 border-black rounded-lg p-6">
        <h3 className="text-lg font-bold mb-4">Project Status Breakdown</h3>
        <ResponsiveContainer width="100%" height={300}>
          <BarChart data={activeData}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="name" />
            <YAxis />
            <Tooltip />
            <Bar dataKey="value" fill="#000000">
              {activeData.map((entry, index) => (
                <Cell key={`cell-${index}`} fill={entry.color} />
              ))}
            </Bar>
          </BarChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
};

export default ProjectStatsCharts;
