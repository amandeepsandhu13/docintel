import { Component, OnInit, Input } from '@angular/core';
import { OpenAIService } from '../../services/openai.service';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { DocumentService } from '../../services/document';

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
 @Input() docId: string = '';
  chunks: Chunk[] = [];
  selectedChunk = '';
  question = '';
  answer = '';
  loading = false;

  constructor(
    private documentService: DocumentService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
  const routeDocId = this.route.snapshot.paramMap.get('docId');
    if (!this.docId && routeDocId) {
      this.docId = routeDocId;
    }
      if (this.docId) {
        this.documentService.getChunks(this.docId).subscribe({
          next: (res: any) => {
            this.chunks = res.chunks || [];
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
     if (!this.selectedChunk || !this.question.trim()) return;

     this.loading = true;
     this.answer = '';

     this.documentService.askQuestion(this.selectedChunk, this.question).subscribe({
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
 }
