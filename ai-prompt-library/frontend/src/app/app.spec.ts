import { describe, beforeEach, it, expect } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { App } from './app';
import { PromptService } from './prompt.service';
import { of } from 'rxjs';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';

describe('App', () => {
  let mockPromptService: any;

  beforeEach(async () => {
    mockPromptService = {
      getTemplates: () => of([]),
      getHistory: () => of([]),
      getVersionsForComparison: () => of([]),
    };

    await TestBed.configureTestingModule({
      imports: [App],
      providers: [
        { provide: PromptService, useValue: mockPromptService },
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('should render brand header', async () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    await fixture.whenStable();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('h1')?.textContent).toContain('Prompt Studio');
  });
});
