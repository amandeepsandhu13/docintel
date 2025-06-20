import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';import { DocumentService } from '../../services/document.service';
import { OpenAIService } from '../../services/openai.service';
import { SimpleAnalysisResult, Chunk } from '../../models/simple-analysis-result.model';

@Component({
  selector: 'app-doc-upload',
  standalone: true,
  templateUrl: './doc-upload.component.html',
  styleUrls: ['./doc-upload.component.scss'],
  imports: [CommonModule, FormsModule],
  providers: [DocumentService, OpenAIService]
})
export class DocUploadComponent {

  selectedFile: File | null = null;
  modelType: string = 'invoice';
  docId: string | null = null;
  analysisResult: SimpleAnalysisResult | null = null;
  selectedChunk: Chunk | null = null;
  question: string = '';
  answer: string = '';
  loading: boolean = false;

  constructor(private documentService: DocumentService, private openAIService: OpenAIService) {}

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.selectedFile = input.files[0];
    }
  }

  upload(): void {
    if (!this.selectedFile) return;
    this.loading = true;
    this.documentService.uploadDocument(this.selectedFile, this.modelType).subscribe({
      next: (res) => {
        this.docId = res.docId;
        this.loadAnalysisResult();
      },
      error: () => this.loading = false
    });
  }

  loadAnalysisResult(): void {
    if (!this.docId) return;
    this.documentService.getAnalysisResult(this.docId).subscribe({
      next: (result) => {
        this.analysisResult = result;
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  selectChunk(chunk: Chunk): void {
    this.selectedChunk = chunk;
    this.question = '';
    this.answer = '';
  }

  ask(): void {
    if (!this.selectedChunk || !this.question) return;
    this.openAIService.askQuestion(this.selectedChunk.text, this.question).subscribe(res => {
      this.answer = res;
    });
  }
}
