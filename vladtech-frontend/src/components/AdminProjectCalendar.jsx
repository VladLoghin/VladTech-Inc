import FullCalendar from "@fullcalendar/react";
import dayGridPlugin from "@fullcalendar/daygrid";
import interactionPlugin from "@fullcalendar/interaction";

const AdminProjectCalendar = ({ onDateSelect }) => {
  const handleDateClick = (info) => {
    const d = info.date;
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, "0");
    const day = String(d.getDate()).padStart(2, "0");

    const dateOnly = `${year}-${month}-${day}`;
    onDateSelect(dateOnly);
  };

  return (
    <div className="border-2 border-black rounded-xl p-4 shadow-md bg-white">
      <h2 className="text-2xl font-bold mb-4">Project Calendar</h2>
      <FullCalendar
        plugins={[dayGridPlugin, interactionPlugin]}
        initialView="dayGridMonth"
        dateClick={handleDateClick}
        height="auto"
      />
    </div>
  );
};

export default AdminProjectCalendar;
