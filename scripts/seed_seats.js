const fs = require('fs');

async function seedSeats() {
  // 1. Get Token
  const orgRes = await fetch('http://localhost:8080/v1/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email: 'org2@orion.com', password: 'Password123!' })
  }).then(r => r.json());
  const token = orgRes.accessToken;

  // 2. Fetch all catalog events
  const catalogRes = await fetch('http://localhost:8080/v1/catalog/events?page=0&size=100').then(r => r.json());
  const events = catalogRes.events;

  for (const event of events) {
    console.log(`Processing event: ${event.name}`);
    for (const date of event.dates) {
      console.log(`  Date: ${date.dateId}`);

      // Try to create batch
      const batchReq = await fetch(`http://localhost:8083/v1/events/${event.eventId}/dates/${date.dateId}/batches`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
        body: JSON.stringify({
          name: "Lote Inicial",
          price: 150.00,
          currency: "GTQ",
          capacity: 100,
          scheduledStartAt: new Date().toISOString()
        })
      });
      
      if (!batchReq.ok) {
        console.log(`   Batch creation failed/skipped (maybe exists). Status: ${batchReq.status} ${await batchReq.text()}`);
        continue;
      }
      const batchData = await batchReq.json();
      const batchId = batchData.batchId;

      // Create Seating Map
      const mapReq = await fetch(`http://localhost:8083/v1/events/${event.eventId}/dates/${date.dateId}/seating-map`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
        body: JSON.stringify({
          batchId: batchId,
          generalAdmission: {
            capacity: 100,
            accessPolicy: "FIFO"
          }
        })
      });
      
      if (mapReq.ok) {
        console.log(`   Created 100 seats for ${date.dateId}`);
      } else {
        console.error(`   Failed to create seats:`, await mapReq.text());
      }
    }
  }
}

seedSeats().catch(console.error);
