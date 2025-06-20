import { inject } from '@angular/core';
import { Component, OnInit, Input } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
//import { ActivatedRoute } from '@angular/router';
import { DocumentService } from '../../services/document.service';
import { SimpleAnalysisResult } from '../../models/simple-analysis-result.model';
import { OpenAIService } from '../../services/openai.service';

interface Chunk {
  index: number;
  text: string;
}

@Component({
  selector: 'app-qna',
  standalone: true,
  templateUrl: './qna.component.html',
  styleUrls: ['./qna.component.css'],
  imports: [CommonModule, FormsModule]
})
export class QnaComponent implements OnInit{
@Input() docId: string | null = null;
  chunks: Chunk[] = [];
  selectedChunk = '';
  question = '';
  answer = '';
  loading = false;

  constructor(
    private documentService: DocumentService,
    private openAIService: OpenAIService = inject(OpenAIService),

  ) {}

  ngOnInit(): void {
//   const routeDocId = this.route.snapshot.paramMap.get('docId');
//
//     if (!this.docId && routeDocId) {
//       this.docId = routeDocId;
//     }
      if (this.docId) {

      this.documentService.getAnalysisResult(this.docId).subscribe({
        next: (result: SimpleAnalysisResult) => {
          this.chunks = result.chunks;
           console.log('Chunks loaded:', this.chunks);

          },
          error: (err) => {
            console.error('Error loading chunks:', err);
          }
        });
      }
  }

   selectChunk(content: string | null): void {
     if (!content) return;
     this.selectedChunk = content;
     this.answer = '';
     this.question = '';
   }

submit(): void {
  if (this.selectedChunk === null || !this.question.trim()) return;

  const selectedIndex = Number(this.selectedChunk); // ✅ convert to number
  const chunk = this.chunks.find(c => c.index === selectedIndex);

  if (!chunk) {
    this.answer = 'Error: Selected chunk not found.';
    return;
  }

  this.loading = true;
  this.answer = '';

  this.openAIService.askQuestion(chunk.text, this.question).subscribe({
    next: (res: string) => {
      this.answer = res;
    },
    error: (err: any) => {
      this.answer = 'Error: ' + err.message;
    },
    complete: () => {
      this.loading = false;
    }
  });
}

get selectedChunkText(): string | null {
  const selectedIndex = Number(this.selectedChunk); // ✅ convert to number
  const chunk = this.chunks.find(c => c.index === selectedIndex);
  return chunk ? chunk.text : null;
}


 }
