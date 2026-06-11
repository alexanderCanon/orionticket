import { TestBed } from '@angular/core/testing';

import { TicketPdf } from './ticket-pdf';

describe('TicketPdf', () => {
  let service: TicketPdf;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(TicketPdf);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
