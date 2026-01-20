import FullCalendar from "@fullcalendar/react";
import dayGridPlugin from "@fullcalendar/daygrid";
import interactionPlugin from "@fullcalendar/interaction";
import { useTranslation } from "react-i18next";
import frLocale from "@fullcalendar/core/locales/fr";
import enGbLocale from "@fullcalendar/core/locales/en-gb";

const EmployeeProjectCalendar = ({ projects = [], onDateSelect }) => {
  const { t, i18n } = useTranslation(); // CHANGE: include i18n

  const projectDays = new Set();

  projects.forEach((p) => {
    if (!p.startDate) return;
    const start = new Date(p.startDate);
    const end = new Date(p.dueDate || p.startDate);

    const d = new Date(start);
    while (d <= end) {
      const yyyy = d.getFullYear();
      const mm = String(d.getMonth() + 1).padStart(2, "0");
      const dd = String(d.getDate()).padStart(2, "0");
      projectDays.add(`${yyyy}-${mm}-${dd}`);
      d.setDate(d.getDate() + 1);
    }
  });

  const handleDateClick = (info) => onDateSelect?.(info.dateStr);

  // ADD THIS:
  const calendarLocale = i18n.language === "fr" ? frLocale : enGbLocale;

  return (
    <div className="border-2 border-black rounded-xl p-4 shadow-md bg-white">
      <h2 className="text-2xl font-bold mb-4">{t("employee.myCalendar")}</h2>

      <FullCalendar
        key={i18n.language}                 // ADD: forces calendar to re-render on language switch
        locale={calendarLocale}            // ADD: tells FullCalendar which locale to use
        plugins={[dayGridPlugin, interactionPlugin]}
        initialView="dayGridMonth"
        headerToolbar={{
          left: "prev,next today",
          center: "title",
          right: "",
        }}
        dateClick={handleDateClick}
        height="auto"
        dayCellDidMount={(arg) => {
          const yyyy = arg.date.getFullYear();
          const mm = String(arg.date.getMonth() + 1).padStart(2, "0");
          const dd = String(arg.date.getDate()).padStart(2, "0");
          const key = `${yyyy}-${mm}-${dd}`;

          if (projectDays.has(key)) {
            const dot = document.createElement("div");
            dot.style.width = "8px";
            dot.style.height = "8px";
            dot.style.borderRadius = "9999px";
            dot.style.background = "#facc15";
            dot.style.marginTop = "6px";

            arg.el.querySelector(".fc-daygrid-day-number")?.after(dot);
          }
        }}
      />
    </div>
  );
};

export default EmployeeProjectCalendar;
