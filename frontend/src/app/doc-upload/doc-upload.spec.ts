import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DocUpload } from './doc-upload';

describe('DocUpload', () => {
  let component: DocUpload;
  let fixture: ComponentFixture<DocUpload>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [DocUpload]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DocUpload);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
