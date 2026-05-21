import { Injectable, signal } from '@angular/core';
import { openDB, IDBPDatabase } from 'idb';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

export interface QueuedScan {
  id?: number;
  orderId: string;
  scannedAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class OfflineSyncService {
  private dbPromise: Promise<IDBPDatabase>;
  public isOnline = signal<boolean>(navigator.onLine);
  public pendingSyncCount = signal<number>(0);

  constructor(private http: HttpClient) {
    this.dbPromise = openDB('orion-validator-db', 1, {
      upgrade(db) {
        if (!db.objectStoreNames.contains('scans')) {
          db.createObjectStore('scans', { keyPath: 'id', autoIncrement: true });
        }
      }
    });

    window.addEventListener('online', () => {
      this.isOnline.set(true);
      this.syncPendingScans();
    });
    
    window.addEventListener('offline', () => {
      this.isOnline.set(false);
    });

    this.updatePendingCount();
  }

  async queueScan(orderId: string): Promise<void> {
    const db = await this.dbPromise;
    await db.add('scans', {
      orderId,
      scannedAt: new Date().toISOString()
    });
    await this.updatePendingCount();

    if (this.isOnline()) {
      this.syncPendingScans();
    }
  }

  private async updatePendingCount() {
    const db = await this.dbPromise;
    const count = await db.count('scans');
    this.pendingSyncCount.set(count);
  }

  public async syncPendingScans() {
    const db = await this.dbPromise;
    const allPending = await db.getAll('scans');
    
    if (allPending.length === 0) return;

    for (const scan of allPending) {
      try {
        await firstValueFrom(this.http.post('http://localhost:8080/v1/access/scan', { orderId: scan.orderId, timestamp: scan.scannedAt }));
        await db.delete('scans', scan.id);
      } catch (err) {
        console.warn('Fallo al sincronizar ticket. Se reintentará en el próximo ciclo online.', scan.orderId);
        break; // Detener para no saturar si el backend está abajo
      }
    }
    
    this.updatePendingCount();
  }
}
