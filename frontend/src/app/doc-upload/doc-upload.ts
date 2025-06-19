import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { QnaComponent } from '../components/qna/qna.component';
import { HttpClient, HttpParams, HttpErrorResponse } from '@angular/common/http';

@Component({
  standalone: true,
  selector: 'app-doc-upload',
  templateUrl: './doc-upload.html',
  styleUrls: ['./doc-upload.scss'],
  imports: [CommonModule, FormsModule, QnaComponent]
})
export class DocUploadComponent {
  modelType: string = 'invoice';
  selectedFile: File | null = null;
  uploadResponse: any = null;
  errorMessage: string = '';
  analysisResult: any = null;
  loading: boolean = false;

  constructor(private http: HttpClient) {}

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.selectedFile = input.files[0];
      this.uploadResponse = null;
      this.errorMessage = '';
    }
  }

upload(): void {
  if (!this.selectedFile) {
    this.errorMessage = 'No file selected';
    return;
  }
  this.loading = true;

  const formData = new FormData();
  formData.append('file', this.selectedFile);
  formData.append('modelType', this.modelType);

  this.http.post<{ docId: string }>('http://localhost:8081/api/documents/upload', formData)
    .subscribe({
      next: (res) => {
        this.uploadResponse = res.docId;  // store docId
        this.errorMessage = '';

        // Fetch the parsed result chunks or full data using docId
        this.loadChunks(res.docId);
      },
      error: (err: HttpErrorResponse) => {
        this.errorMessage = `Upload failed: ${err.message}`;
        this.uploadResponse = null;
        this.loading = false;
      }
    });
}

loadChunks(docId: string): void {
  this.http.get<{ chunks: any[] }>(`http://localhost:8081/api/documents/${docId}/chunks`)
    .subscribe({
      next: (res) => {
        // Assuming your chunks are in res.chunks
        this.analysisResult = res;
        this.errorMessage = '';
        this.loading = false;
      },
      error: (err: HttpErrorResponse) => {
        this.errorMessage = `Error loading chunks: ${err.message}`;
        this.loading = false;
      }
    });
}



  getResult(operationLocation: string) {
    const params = new HttpParams().set('operationLocation', operationLocation);

    this.http.get<any>('http://localhost:8081/api/documents/result', { params })
      .subscribe({
        next: (res) => {
          this.analysisResult = res;
          this.errorMessage = '';
          this.loading = false;
        },
        error: (err: HttpErrorResponse) => {
          this.errorMessage = 'Error getting result: ' + err.message;
          this.loading = false;
        }
      });
  }
}
