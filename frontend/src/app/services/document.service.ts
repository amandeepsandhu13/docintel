import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { SimpleAnalysisResult } from '../models/simple-analysis-result.model';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class DocumentService {

  private backendUrl = 'http://localhost:8081/api/documents';

  constructor(private http: HttpClient) {}

  uploadDocument(file: File, modelType: string): Observable<{ docId: string }> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('modelType', modelType);
    return this.http.post<{ docId: string }>(`${this.backendUrl}/upload`, formData);
  }

  getAnalysisResult(docId: string): Observable<SimpleAnalysisResult> {
    return this.http.get<SimpleAnalysisResult>(`${this.backendUrl}/${docId}/result`);
  }
}
