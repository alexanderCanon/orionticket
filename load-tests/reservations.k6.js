import http from 'k6/http';
import { check, sleep } from 'k6';
import { uuidv4 } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';

export const options = {
    stages: [
        { duration: '10s', target: 50 },   // Ramp-up to 50 users (to avoid killing local dev quickly)
        { duration: '30s', target: 50 },   // Stay at 50 users for 30s
        { duration: '10s', target: 100 },  // Ramp-up to 100 users
        { duration: '10s', target: 0 },    // Ramp-down to 0
    ],
    // The options limit concurrency but these users will spam requests as fast as possible
};

const GATEWAY_URL = 'http://localhost:8080/v1'; // Fetching catalog through gateway
const SEATING_URL = 'http://localhost:8083/v1'; // Direct to Seating Service to avoid gateway overhead

export function setup() {
    console.log('Fetching catalog to find a valid event...');
    
    // 1. Get Events
    const catalogRes = http.get(`${GATEWAY_URL}/catalog/events?page=0&size=10`);
    if (catalogRes.status !== 200) {
        throw new Error(`Failed to fetch catalog: ${catalogRes.status}`);
    }
    
    const events = catalogRes.json('events');
    if (!events || events.length === 0) {
        throw new Error('No events found in catalog. Run seed scripts first.');
    }
    
    const event = events[0];
    if (!event.dates || event.dates.length === 0) {
        throw new Error('Event has no dates.');
    }
    
    const date = event.dates[0];
    const eventId = event.eventId;
    const dateId = date.dateId;
    
    console.log(`Using Event: ${eventId}, Date: ${dateId}`);
    
    // 2. Get Available Seats
    const seatsRes = http.get(`${SEATING_URL}/events/${eventId}/dates/${dateId}/seats`);
    if (seatsRes.status !== 200) {
        throw new Error(`Failed to fetch seats: ${seatsRes.status}`);
    }
    
    const seats = seatsRes.json();
    if (!seats || seats.length === 0) {
        throw new Error('No seats available for this event date.');
    }
    
    console.log(`Found ${seats.length} available seats.`);
    
    return {
        eventId: eventId,
        dateId: dateId,
        seats: seats
    };
}

export default function (data) {
    const url = `${SEATING_URL}/reservations`;

    // Pick a random seat from the pool
    const randomSeat = data.seats[Math.floor(Math.random() * data.seats.length)];

    const payload = JSON.stringify({
        seatId: randomSeat.seatId,
        buyerId: uuidv4(), // Simulate a different buyer for every request
        eventId: data.eventId,
        dateId: data.dateId,
        batchId: randomSeat.batchId,
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    const res = http.post(url, payload, params);

    // Verify response
    check(res, {
        'status is 201 (Created)': (r) => r.status === 201,
        'status is 409 (Conflict - Overbooking Prevented)': (r) => r.status === 409,
        'status is 410 (Batch Exhausted)': (r) => r.status === 410,
    });
    
    // Tiny sleep to avoid completely overwhelming the OS network stack
    sleep(0.01); // 10ms
}
