import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class OpenAIService {

  private aiUrl = 'http://localhost:8081/api/ai';

  constructor(private http: HttpClient) {}

  askQuestion(context: string, question: string): Observable<string> {
    const request = { context, question };
    return this.http.post<{ answer: string }>(`${this.aiUrl}/ask`, request)
      .pipe(map(res => res.answer));
  }
}
