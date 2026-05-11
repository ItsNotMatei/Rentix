import React, { useState, useEffect } from 'react';
import FullCalendar from '@fullcalendar/react';
import dayGridPlugin from '@fullcalendar/daygrid';

const CalendarPage = () => {
    const [anuntId, setAnuntId] = useState('');
    const [events, setEvents] = useState([]);

    const loadCalendar = async () => {
        const response = await fetch(`/api/reservations/availability/${anuntId}`);
        const dates = await response.json();
        const formattedEvents = dates.map(date => ({
            title: 'Reserved',
            start: date,
            allDay: true
        }));
        setEvents(formattedEvents);
    };

    return (
        <div style={{ padding: '30px' }}>
            <h1>Calendar Disponibilitate</h1>
            <input
                type="number"
                value={anuntId}
                onChange={(e) => setAnuntId(e.target.value)}
                placeholder="Anunt ID"
            />
            <button onClick={loadCalendar}>Load Calendar</button>

            <div style={{ marginTop: '20px' }}>
                <FullCalendar
                    plugins={[dayGridPlugin]}
                    initialView="dayGridMonth"
                    events={events}
                />
            </div>
        </div>
    );
};

export default CalendarPage;