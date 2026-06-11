import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TicketSuccess } from './ticket-success';

describe('TicketSuccess', () => {
  let component: TicketSuccess;
  let fixture: ComponentFixture<TicketSuccess>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TicketSuccess],
    }).compileComponents();

    fixture = TestBed.createComponent(TicketSuccess);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
