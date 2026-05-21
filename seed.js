const fs = require('fs');

async function seed() {
  const VENUE_ID = '6e83e420-c122-4d27-a7b1-c92ad6ecd93d';
  
  // 1. Get Org Token
  const orgRes = await fetch('http://localhost:8080/v1/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email: 'org2@orion.com', password: 'Password123!' })
  }).then(r => r.json());
  const orgToken = orgRes.accessToken;
  console.log('Org logged in');

  // 2. Get Admin Token
  const adminRes = await fetch('http://localhost:8080/v1/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email: 'admin2@orion.com', password: 'Password123!' })
  }).then(r => r.json());
  const adminToken = adminRes.accessToken;
  console.log('Admin logged in');

  const categories = ['Música', 'Conferencia', 'Deportes', 'Teatro', 'Arte', 'Comedia'];
  const eventsData = [];
  for (let i = 1; i <= 20; i++) {
    const category = categories[i % categories.length];
    eventsData.push({
      name: `Evento Masivo ${category} #${i}`,
      category: category,
      description: `Disfruta de la mejor experiencia en este espectacular evento de ${category}.`
    });
  }

  for (const edata of eventsData) {
    // Create Event
    const eventReq = await fetch('http://localhost:8080/v1/events', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${orgToken}` },
      body: JSON.stringify(edata)
    });
    if (!eventReq.ok) {
        console.error('Failed to create event', await eventReq.text());
        continue;
    }
    const eventData = await eventReq.json();
    const eventId = eventData.eventId;
    console.log(`Created event: ${edata.name} (${eventId})`);

    // Add 10 Dates
    for (let i = 1; i <= 10; i++) {
      const date = new Date();
      date.setDate(date.getDate() + (i * 7)); // every week
      
      const dateReq = await fetch(`http://localhost:8080/v1/events/${eventId}/dates`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${orgToken}` },
        body: JSON.stringify({
          scheduledAt: date.toISOString(),
          venueId: VENUE_ID,
          capacity: 100 // small capacity for quick seat generation
        })
      });
      if (!dateReq.ok) console.error('Failed to add date', await dateReq.text());
    }
    console.log(`Added 10 dates to ${eventId}`);

    // Submit for review
    const submitReq = await fetch(`http://localhost:8080/v1/events/${eventId}/submit`, {
      method: 'POST',
      headers: { 'Authorization': `Bearer ${orgToken}` }
    });
    if (!submitReq.ok) console.error('Failed to submit', await submitReq.text());

    // Approve
    const approveReq = await fetch(`http://localhost:8080/v1/events/${eventId}/approve`, {
      method: 'POST',
      headers: { 'Authorization': `Bearer ${adminToken}` }
    });
    if (!approveReq.ok) console.error('Failed to approve', await approveReq.text());
    else console.log(`Approved event ${eventId}`);
  }
}

seed().catch(console.error);
