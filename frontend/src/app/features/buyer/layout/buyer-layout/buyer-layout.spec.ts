import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BuyerLayout } from './buyer-layout';

describe('BuyerLayout', () => {
  let component: BuyerLayout;
  let fixture: ComponentFixture<BuyerLayout>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BuyerLayout],
    }).compileComponents();

    fixture = TestBed.createComponent(BuyerLayout);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
