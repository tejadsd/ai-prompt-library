import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface PromptRequest {
  category: string;
  promptTemplate: string;
  inputPrompt: string;
  promptKey?: string;
}

export interface PromptResponse {
  id: number;
  category: string;
  promptTemplate: string;
  inputPrompt: string;
  outputResponse: string;
  reviewNotes?: string;
  version: number;
  promptKey: string;
  rating?: number;
  createdAt: string;
}

export interface Template {
  category: string;
  title: string;
  prompt: string;
}

@Injectable({
  providedIn: 'root'
})
export class PromptService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8080/api';

  generatePrompt(request: PromptRequest): Observable<PromptResponse> {
    const category = request.category.toLowerCase();
    if (category === 'code') {
      return this.http.post<PromptResponse>(`${this.apiUrl}/generateCode`, request);
    } else if (category === 'documentation') {
      return this.http.post<PromptResponse>(`${this.apiUrl}/generateDoc`, request);
    } else if (category === 'testing') {
      return this.http.post<PromptResponse>(`${this.apiUrl}/generateTestCases`, request);
    } else {
      return this.http.post<PromptResponse>(`${this.apiUrl}/generate`, request);
    }
  }

  getHistory(category?: string): Observable<PromptResponse[]> {
    const url = category ? `${this.apiUrl}/history?category=${category}` : `${this.apiUrl}/history`;
    return this.http.get<PromptResponse[]>(url);
  }

  getExecutionById(id: number): Observable<PromptResponse> {
    return this.http.get<PromptResponse>(`${this.apiUrl}/history/${id}`);
  }

  updateReview(id: number, reviewNotes: string, rating?: number): Observable<PromptResponse> {
    return this.http.put<PromptResponse>(`${this.apiUrl}/history/${id}/review`, { reviewNotes, rating });
  }

  getVersionsForComparison(promptKey: string): Observable<PromptResponse[]> {
    return this.http.get<PromptResponse[]>(`${this.apiUrl}/history/compare?promptKey=${promptKey}`);
  }

  getTemplates(): Observable<Template[]> {
    return this.http.get<Template[]>(`${this.apiUrl}/templates`);
  }
}
