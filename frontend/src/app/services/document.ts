import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Chunk } from '../models/chunk';
import { QnaComponent } from '../components/qna/qna.component';



@Injectable({
  providedIn: 'root'
})
export class DocumentService {

  private backendUrl = 'http://localhost:8081/api/documents'; // Spring Boot backend
    private aiUrl = 'http://localhost:8081/api/ai';


  constructor(private http: HttpClient) { }

  uploadDocument(file: File, modelType: string) {
     const formData = new FormData();
        formData.append('file', file);              // must match @RequestPart("file")
        formData.append('modelType', modelType);    // must match @RequestParam("modelType")


    return this.http.post(`${this.backendUrl}/upload`, formData, {
      params: new HttpParams().set('modelType', modelType),
      responseType: 'text'
    });
  }

 getChunks(docId: string): Observable<any> {
    return this.http.get(`${this.backendUrl}/${docId}/chunks`);
  }
  askQuestion(chunk: string, question: string): Observable<any> {
    return this.http.post(`${this.aiUrl}/ask`, {
      chunk,
      question
    });
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
