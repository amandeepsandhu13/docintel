import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs';


@Injectable({ providedIn: 'root' })
export class OpenAIService {
  private baseUrl = 'http://localhost:8081/api/ai';

  constructor(private http: HttpClient) {}

askQuestion(chunk: string, question: string): Observable<string> {
  return this.http
    .post<{ answer: string }>(`${this.baseUrl}/ask`, { chunk, question })
    .pipe(
      map((response: { answer: string }) => response.answer)
    );
}

}
