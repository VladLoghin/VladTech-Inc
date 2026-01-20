// src/components/AdminProjectCalendar.jsx
import FullCalendar from "@fullcalendar/react";
import dayGridPlugin from "@fullcalendar/daygrid";
import interactionPlugin from "@fullcalendar/interaction";
import { useTranslation } from "react-i18next";

// FullCalendar locales
import frLocale from "@fullcalendar/core/locales/fr";
import enLocale from "@fullcalendar/core/locales/en-gb";

const AdminProjectCalendar = ({ onDateSelect }) => {
  const { t, i18n } = useTranslation();

  const lang = i18n.resolvedLanguage || i18n.language || "en";
  const isFr = lang.startsWith("fr");

  const handleDateClick = (info) => {
    onDateSelect(info.dateStr); // "YYYY-MM-DD"
  };

  return (
    <div className="border-2 border-black rounded-xl p-4 shadow-md bg-white">
      <h2 className="text-2xl font-bold mb-4">{t("admin.projectCalendar")}</h2>

      <FullCalendar
        key={isFr ? "fr" : "en"}          // important: force redraw on language switch
        plugins={[dayGridPlugin, interactionPlugin]}
        initialView="dayGridMonth"
        locales={[frLocale, enLocale]}   // register locales
        locale={isFr ? "fr" : "en"}      // activate locale

        headerToolbar={{
          left: "prev,next today",
          center: "title",
          right: "",
        }}

        // translate the button label too
        buttonText={{
          today: t("calendar.today"),
        }}

        dateClick={handleDateClick}
        height="auto"
      />
    </div>
  );
};

export default AdminProjectCalendar;
