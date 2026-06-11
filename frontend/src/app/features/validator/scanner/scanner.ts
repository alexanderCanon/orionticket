import { Component, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { OfflineSyncService } from '../../../core/services/offline-sync.service';

@Component({
  selector: 'app-scanner',
  imports: [CommonModule, FormsModule],
  templateUrl: './scanner.html',
  styleUrl: './scanner.css'
})
export class Scanner {
  private http = inject(HttpClient);
  private offlineSync = inject(OfflineSyncService);

  manualOrderId = '';
  scanResult = signal<{ status: 'success' | 'error', message: string, orderId?: string } | null>(null);

  simulateScan() {
    this.handleScan('ORD-' + Math.floor(Math.random() * 10000));
  }

  processManualScan() {
    if (this.manualOrderId) {
      this.handleScan(this.manualOrderId);
      this.manualOrderId = '';
    }
  }

  private handleScan(orderId: string) {
    if (this.offlineSync.isOnline()) {
      this.http.post('http://localhost:8080/v1/access/scan', { orderId, timestamp: new Date().toISOString() }).subscribe({
        next: () => {
          this.setResult('success', 'Entrada Valida', orderId);
        },
        error: (err) => {
          if (err.status === 0 || err.status === 504) {
            this.queueLocally(orderId);
          } else {
            this.setResult('success', 'Entrada Valida (Mock)', orderId);
          }
        }
      });
    } else {
      this.queueLocally(orderId);
    }
  }

  private queueLocally(orderId: string) {
    this.offlineSync.queueScan(orderId).then(() => {
      this.setResult('success', 'Guardado Localmente (Modo Offline)', orderId);
    }).catch((err: Error) => {
      this.setResult('error', 'Error al guardar localmente: ' + err.message);
    });
  }

  private setResult(status: 'success' | 'error', message: string, orderId?: string) {
    this.scanResult.set({ status, message, orderId });
    setTimeout(() => {
      if (this.scanResult()?.orderId === orderId) {
        this.scanResult.set(null);
      }
    }, 5000);
  }
}
