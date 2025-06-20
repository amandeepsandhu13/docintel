import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { DocUploadComponent } from './components/doc-upload/doc-upload.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, DocUploadComponent],
  templateUrl: './app.html',
  styleUrls: ['./app.scss']
})
export class App {
  protected title = 'frontend';
}
