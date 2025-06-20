import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { QnaComponent } from './components/qna/qna.component';
import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { App } from './app';
import { DocUploadComponent } from './components/doc-upload/doc-upload.component';


@NgModule({
   imports: [FormsModule, HttpClientModule, BrowserModule, App],
  bootstrap: [App]
})
export class AppModule {}
