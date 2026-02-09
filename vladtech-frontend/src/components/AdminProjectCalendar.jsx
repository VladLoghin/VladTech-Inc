// src/components/AdminProjectCalendar.jsx
import FullCalendar from "@fullcalendar/react";
import dayGridPlugin from "@fullcalendar/daygrid";
import interactionPlugin from "@fullcalendar/interaction";
import enLocale from "@fullcalendar/core/locales/en-gb";
import frLocale from "@fullcalendar/core/locales/fr";
import { Maximize2, Minimize2, X } from "lucide-react";
import { useState, useMemo, useRef } from "react";
import { useTranslation } from "react-i18next";


const AdminProjectCalendar = ({ projects = [], onDateSelect, selectedDate }) => {
  const { t, i18n } = useTranslation();

  const lang = i18n.resolvedLanguage || i18n.language || "en";
  const isFr = lang.startsWith("fr");

  /* New State for Features */
  const [showCounts, setShowCounts] = useState(false);
  // Using persisted prop 'selectedDate' from parent
  const calendarRef = useRef();
  // Helper: get today's date string in YYYY-MM-DD
  const getTodayStr = () => {
    const today = new Date();
    const yyyy = today.getFullYear();
    const mm = String(today.getMonth() + 1).padStart(2, "0");
    const dd = String(today.getDate()).padStart(2, "0");
    return `${yyyy}-${mm}-${dd}`;
  };

  const handleDateClick = (info) => {
    onDateSelect(info.dateStr); // "YYYY-MM-DD"
  };

  const getEventStyle = (status) => {
    const s = (status || "").toUpperCase();
    if (s === "COMPLETED") return "#10b981"; // Green
    if (s === "IN_PROGRESS") return "#3b82f6"; // Blue
    return "#f59e0b"; // Orange (Pending)
  };

  // Pre-process projects into a map of Date -> { STATUS: count }
  const projectStatusMap = useMemo(() => {
    const map = new Map();

    projects.forEach(p => {
        if (!p.startDate) return;
        // Skip archived projects (state === 'COMPLETE')
        if (p.state && p.state.toUpperCase() === 'COMPLETE') return;
        
        // Parse dates in local timezone to avoid off-by-one errors
        const [startY, startM, startD] = p.startDate.split('-').map(Number);
        const start = new Date(startY, startM - 1, startD);
        
        const [endY, endM, endD] = (p.dueDate || p.startDate).split('-').map(Number);
        const end = new Date(endY, endM - 1, endD);
        
        const status = (p.status || "PENDING").toUpperCase();

        for (let d = new Date(start); d <= end; d.setDate(d.getDate() + 1)) {
          const yyyy = d.getFullYear();
          const mm = String(d.getMonth() + 1).padStart(2, "0");
          const dd = String(d.getDate()).padStart(2, "0");
          const key = `${yyyy}-${mm}-${dd}`;

          if (!map.has(key)) {
            map.set(key, {});
          }
          const counts = map.get(key);
          counts[status] = (counts[status] || 0) + 1;
        }
      });
      return map;
  }, [projects]);

  // Only render the selection background as an event
  const events = useMemo(() => selectedDate ? [{
    start: selectedDate,
    display: "background",
    classNames: ["fc-selected-date-event"]
  }] : [], [selectedDate]);

  // ...existing code...

  // Performance-optimized mount callback (runs once per cell)
  const handleDayCellDidMount = (arg) => {
    const yyyy = arg.date.getFullYear();
    const mm = String(arg.date.getMonth() + 1).padStart(2, "0");
    const dd = String(arg.date.getDate()).padStart(2, "0");
    const key = `${yyyy}-${mm}-${dd}`;

    if (!projectStatusMap.has(key)) return;

    const counts = projectStatusMap.get(key);
    const statuses = ["COMPLETED", "IN_PROGRESS", "PENDING", "ACTIVE", "ARCHIVED", "APPOINTMENT", "SCHEDULED"];
    const presentStatuses = statuses.filter(s => counts[s]);
    Object.keys(counts).forEach(s => {
      if (!statuses.includes(s) && !presentStatuses.includes(s)) presentStatuses.push(s);
    });

    // Create container for status indicators
    const container = document.createElement("div");
    container.className = "flex justify-center flex-wrap gap-[2px] mt-1";
    container.style.marginTop = "6px";

    presentStatuses.forEach(status => {
      const count = counts[status];
      const color = getEventStyle(status);
      
      const indicator = document.createElement("div");
      
      if (showCounts) {
        indicator.className = "flex items-center justify-center text-white text-[10px] font-bold rounded-full h-4 min-w-[16px] px-1";
        indicator.style.backgroundColor = color;
        indicator.textContent = count;
      } else {
        indicator.className = "w-2 h-2 rounded-full";
        indicator.style.backgroundColor = color;
      }
      
      container.appendChild(indicator);
    });

    // Append after the day number
    arg.el.querySelector(".fc-daygrid-day-number")?.after(container);
  };

  // Calculate a data hash to force remount when deep data changes
  const dataVersion = useMemo(() => {
    return projects.map(p => 
      `${p.projectIdentifier || p.id}:${p.status}:${p.startDate}:${p.dueDate}:${p.state}`
    ).join("|");
  }, [projects]);

  return (
    <div
      className="transition-all duration-300 bg-white shadow-md border-2 border-black rounded-xl p-4 relative"
    >
      <div className="flex items-center justify-between mb-4">
        <h2 className="text-2xl font-bold">{t("admin.projectCalendar")}</h2>
        
        <div className="flex items-center gap-4">
          <label className="flex items-center gap-2 text-sm font-medium cursor-pointer select-none">
            <span>{t("admin.showCounts") !== "admin.showCounts" ? t("admin.showCounts") : "Show Project Counts"}</span>
            <div
              onClick={() => setShowCounts(!showCounts)}
              className={`relative inline-flex h-6 w-11 items-center rounded-full transition-colors ${
                showCounts ? "bg-black" : "bg-gray-300"
              }`}
            >
              <span
                className={`inline-block h-4 w-4 transform rounded-full bg-white transition-transform ${
                  showCounts ? "translate-x-6" : "translate-x-1"
                }`}
              />
            </div>
          </label>
        </div>
      </div>

      <FullCalendar
        // Key changes on lang, showCounts, or ANY data change (via dataVersion hash)
        key={`${isFr ? "fr" : "en"}-${showCounts}-${dataVersion}`}
        ref={calendarRef}
        plugins={[dayGridPlugin, interactionPlugin]}
        initialView="dayGridMonth"
        locales={[frLocale, enLocale]}
        locale={isFr ? "fr" : "en"}
        events={events}
        dayMaxEvents={true}
        eventDisplay="block"

        headerToolbar={{
          left: "prev,next todayCustom",
          center: "title",
          right: "",
        }}
        customButtons={{
          todayCustom: {
            text: t("calendar.today") || "Today",
            click: () => {
              const todayStr = getTodayStr();
              onDateSelect(todayStr);
              // Also navigate to today
              calendarRef.current?.getApi().today();
            },
          },
        }}
        buttonText={{
          today: t("calendar.today"),
        }}

        dateClick={handleDateClick}
        height="auto"
        dayCellDidMount={handleDayCellDidMount}
      />
      
      <style>{`
        .fc-toolbar-title,
        .fc-col-header-cell-cushion {
          text-transform: capitalize;
        }
        .fc-daygrid-day {
          cursor: pointer;
          transition: background-color 0.2s;
        }
        .fc-daygrid-day:hover {
          background-color: #f3f4f6 !important; /* Tailwind gray-100 */
        }
        .fc-event {
          pointer-events: none;
        }
        /* Selection Box (Background Event) */
        .fc-bg-event.fc-selected-date-event {
          opacity: 1 !important;
          background-color: rgba(0, 0, 0, 0.05) !important; /* Black tint */
          box-shadow: inset 0 0 0 2px black !important; /* Black border */
        }
      `}</style>

      {/* Legend */}
      <div className="mt-4 flex flex-wrap gap-4 text-sm px-2 border-t pt-4">
        <div className="flex items-center gap-2">
          <div className="w-3 h-3 rounded-full bg-emerald-500"></div>
          <span>{t("project.completed") || "Completed"}</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-3 h-3 rounded-full bg-blue-500"></div>
          <span>{t("project.inProgress") || "In Progress"}</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-3 h-3 rounded-full bg-amber-500"></div>
          <span>{t("project.pending") || "Pending"}</span>
        </div>
      </div>
    </div>
  );
};

export default AdminProjectCalendar;
