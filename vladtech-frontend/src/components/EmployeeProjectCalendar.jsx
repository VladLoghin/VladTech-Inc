import FullCalendar from "@fullcalendar/react";
import dayGridPlugin from "@fullcalendar/daygrid";
import interactionPlugin from "@fullcalendar/interaction";

const EmployeeProjectCalendar = ({ projects = [], onDateSelect }) => {
  const projectDays = new Set();

  // mark all days between startDate and dueDate (inclusive)
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

  const handleDateClick = (info) => {
    onDateSelect(info.dateStr); // "YYYY-MM-DD"
  };

  return (
    <div className="border-2 border-black rounded-xl p-4 shadow-md bg-white">
      <h2 className="text-2xl font-bold mb-4">My Project Calendar</h2>

      <FullCalendar
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
            // small dot indicator (no bars/lines)
            const dot = document.createElement("div");
            dot.style.width = "8px";
            dot.style.height = "8px";
            dot.style.borderRadius = "9999px";
            dot.style.background = "#facc15"; // yellow-400
            dot.style.marginTop = "6px";

            arg.el.querySelector(".fc-daygrid-day-number")?.after(dot);
          }
        }}
      />
    </div>
  );
};

export default EmployeeProjectCalendar;
