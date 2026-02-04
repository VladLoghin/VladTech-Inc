import FullCalendar from "@fullcalendar/react";
import dayGridPlugin from "@fullcalendar/daygrid";
import interactionPlugin from "@fullcalendar/interaction";
import { useTranslation } from "react-i18next";
import { useState } from "react";
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

  // ADD THIS:
  const calendarLocale = i18n.language === "fr" ? frLocale : enGbLocale;

  const [selectedDate, setSelectedDate] = useState(null);

  const handleDateClick = (info) => {
    setSelectedDate(info.dateStr);
    onDateSelect?.(info.dateStr);
  };

  const events = selectedDate ? [{
    start: selectedDate,
    display: "background",
    classNames: ["fc-selected-date-event"]
  }] : [];

  return (
    <div className="border-2 border-black rounded-xl p-4 shadow-md bg-white">
      <h2 className="text-2xl font-bold mb-4">{t("employee.myCalendar")}</h2>

      <FullCalendar
        key={`${i18n.language}-${selectedDate}`} // ADD: forces calendar to re-render on language switch OR selection
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
        events={events} // Inject background event for selection
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
      <style>{`
        .fc-daygrid-day {
          cursor: pointer;
          transition: background-color 0.2s;
        }
        .fc-daygrid-day:hover {
          background-color: #f3f4f6 !important; /* Tailwind gray-100 */
        }
        /* Selection Box (Background Event) */
        .fc-bg-event.fc-selected-date-event {
          opacity: 1 !important;
          background-color: rgba(0, 0, 0, 0.05) !important; /* Black tint */
          box-shadow: inset 0 0 0 2px black !important; /* 2px Black border */
        }
      `}</style>
    </div>
  );
};

export default EmployeeProjectCalendar;
