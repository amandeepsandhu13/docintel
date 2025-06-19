import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { QnaComponent } from '../components/qna/qna.component';

interface Chunk {
  index: number;
  text: string;
}

@Injectable({
  providedIn: 'root'
})
export class DocumentService {

  private backendUrl = 'http://localhost:8081/api/documents'; // Spring Boot backend
    private aiUrl = 'http://localhost:8081/api/ai';


  constructor(private http: HttpClient) { }

 uploadDocument(file: File, modelType: string): Observable<{docId: string}> {
   const formData = new FormData();
   formData.append('file', file);
   formData.append('modelType', modelType);

   return this.http.post<{docId: string}>(`${this.backendUrl}/upload`, formData);
 }

 getChunks(docId: string): Observable<{ chunks: Chunk[] }> {
   return this.http.get<{ chunks: Chunk[] }>(`${this.backendUrl}/${docId}/chunks`);
 }

  askQuestion(chunkContent: string, question: string): Observable<any> {
       return this.http.post(`${this.backendUrl}/ask-question`, { chunkContent, question }, { responseType: 'text' });

  }

  getAnalysisResult(operationLocation: string) {
    return this.http.get(`${this.backendUrl}/analyze-doc`, {
      params: new HttpParams().set('operationLocation', operationLocation)
    });
  }

chunkDocument(operationLocation: string): Observable<Chunk[]> {
  const params = new HttpParams().set('operationLocation', operationLocation);
  return this.http.get<Chunk[]>(`${this.backendUrl}/chunk`, { params });
}

}
