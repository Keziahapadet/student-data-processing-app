import { Routes } from '@angular/router';
import { GenerateComponent } from './components/generate.component';
import { ProcessComponent } from './components/process.component';
import { UploadComponent } from './components/upload.component';
import { ReportComponent } from './components/report.component';

export const routes: Routes = [
  { path: '', redirectTo: 'generate', pathMatch: 'full' },
  { path: 'generate', component: GenerateComponent },
  { path: 'process', component: ProcessComponent },
  { path: 'upload', component: UploadComponent },
  { path: 'report', component: ReportComponent }
];
