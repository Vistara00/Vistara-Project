import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CheckinPopup } from './checkin-popup';

describe('CheckinPopup', () => {
  let component: CheckinPopup;
  let fixture: ComponentFixture<CheckinPopup>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CheckinPopup],
    }).compileComponents();

    fixture = TestBed.createComponent(CheckinPopup);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
