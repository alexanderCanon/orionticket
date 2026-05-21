import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ValidatorLayout } from './validator-layout';

describe('ValidatorLayout', () => {
  let component: ValidatorLayout;
  let fixture: ComponentFixture<ValidatorLayout>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ValidatorLayout],
    }).compileComponents();

    fixture = TestBed.createComponent(ValidatorLayout);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
