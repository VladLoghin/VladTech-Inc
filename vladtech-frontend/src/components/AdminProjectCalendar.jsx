// src/components/AdminProjectCalendar.jsx
import FullCalendar from "@fullcalendar/react";
import dayGridPlugin from "@fullcalendar/daygrid";
import interactionPlugin from "@fullcalendar/interaction";

const AdminProjectCalendar = ({ onDateSelect }) => {
  const handleDateClick = (info) => {
    // info.dateStr = "YYYY-MM-DD"
    onDateSelect(info.dateStr);
  };

  return (
    <div className="border-2 border-black rounded-xl p-4 shadow-md bg-white">
      <h2 className="text-2xl font-bold mb-4">Project Calendar</h2>

      <FullCalendar
        plugins={[dayGridPlugin, interactionPlugin]}
        initialView="dayGridMonth"
        headerToolbar={{
          left: "prev,next today",
          center: "title",
          right: ""
        }}
        dateClick={handleDateClick}
        height="auto"
      />
    </div>
  );
};

export default AdminProjectCalendar;
