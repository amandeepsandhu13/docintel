import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { QnaComponent } from '../components/qna/qna.component';
import { DocumentService } from '../services/document'; // import your service
import { HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})

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
  uploadResponse: string | null = null;
  errorMessage: string = '';
  analysisResult: any = null;
  loading: boolean = false;
  docId: string | null = null;

  constructor(private documentService: DocumentService) {}

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.selectedFile = input.files[0];
      this.uploadResponse = null;
      this.errorMessage = '';
      this.analysisResult = null;
      this.docId = null;
    }
  }

  upload(): void {
    if (!this.selectedFile) {
      this.errorMessage = 'No file selected';
      return;
    }
    this.loading = true;

    this.documentService.uploadDocument(this.selectedFile, this.modelType)
      .subscribe({
        next: (res: { docId: string }) => {
          this.docId = res.docId;
          this.uploadResponse = this.docId;
          this.errorMessage = '';
          this.loadChunks(this.docId!);
        },
            error: (err: HttpErrorResponse) => {  // <--- specify type here
              this.errorMessage = `Upload failed: ${err.message}`;
              this.uploadResponse = null;
              this.loading = false;
            }
      });
  }

  loadChunks(docId: string): void {
    this.documentService.getChunks(docId).subscribe({
      next: (res: { chunks: any[] }) => {
        this.analysisResult = res; // contains chunks and other info
        this.errorMessage = '';
        this.loading = false;
      },
      error: (err: HttpErrorResponse) => {
        this.errorMessage = `Error loading chunks: ${err.message}`;
        this.loading = false;
      }
    });
  }
}
