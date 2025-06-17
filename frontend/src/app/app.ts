import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { DocUploadComponent } from './doc-upload/doc-upload';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, DocUploadComponent],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {
  protected title = 'frontend';
}
