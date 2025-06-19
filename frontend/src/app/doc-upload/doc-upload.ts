import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { QnaComponent } from '../components/qna/qna.component';


import { HttpClient, HttpParams, HttpErrorResponse } from '@angular/common/http';

@Component({
  standalone: true,
  selector: 'app-doc-upload',
  templateUrl: './doc-upload.html',
  styleUrls: ['./doc-upload.scss'], // if you use styles
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


    // Update URL to your backend upload endpoint
      this.http.post('http://localhost:8081/api/documents/upload', formData, { responseType: 'json' }).subscribe({
     next: (operationLocation: string) => {
       this.uploadResponse = operationLocation;
       this.getResult(operationLocation); //  Get analysis result
      },
      error: (err: HttpErrorResponse) => {
        this.errorMessage = `Upload failed: ${err.message}`;
        this.uploadResponse = null;
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



