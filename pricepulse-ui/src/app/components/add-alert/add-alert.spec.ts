import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddAlert } from './add-alert';

describe('AddAlert', () => {
  let component: AddAlert;
  let fixture: ComponentFixture<AddAlert>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddAlert],
    }).compileComponents();

    fixture = TestBed.createComponent(AddAlert);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
