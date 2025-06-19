import { Component, OnInit } from '@angular/core';
import { OpenAIService } from '../../services/openai.service';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { DocumentService } from '../../services/document';

@Component({
  selector: 'app-qna',
  standalone: true,
  templateUrl: './qna.component.html',
  styleUrls: ['./qna.component.css'],
  imports: [CommonModule, FormsModule]
})
export class QnaComponent implements OnInit{
  docId: string = '';
  chunks: any[] = [];
  selectedChunk = '';
  question = '';
  answer = '';
  loading = false;

  constructor(
    private documentService: DocumentService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.docId = this.route.snapshot.paramMap.get('docId') || '';
    if (this.docId) {
      this.documentService.getChunks(this.docId).subscribe((res: { chunks: any[] }) => {
        this.chunks = res.chunks;
      });
    }
  }

  selectChunk(content: string) {
    this.selectedChunk = content;
    this.answer = '';
    this.question = '';
  }


  submit() {
    if (!this.chunks || !this.question) return;

    this.loading = true;
    this.answer = '';

    this.documentService.askQuestion(this.selectedChunk, this.question).subscribe({
      next: (res: any) => {
        this.answer = res;
        this.loading = false;
      },
      error: (err: any) => {
        this.answer = 'Error: ' + err.message;
        this.loading = false;
      }
    });
  }
}
