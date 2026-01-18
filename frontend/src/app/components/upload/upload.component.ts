import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ApiService } from '../services/api.service';

@Component({
  selector: 'app-upload',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule,
    MatSnackBarModule
  ],
  templateUrl: './upload.component.html',
  styleUrl: './upload.component.scss'
})
export class UploadComponent {
  selectedFile?: File;
  loading = false;
  inserted = 0;
  dragOver = false;

  constructor(
    private api: ApiService,
    private snackBar: MatSnackBar
  ) {}

  onFileSelected(event: Event): void {
    const target = event.target as HTMLInputElement;
    if (target.files && target.files.length > 0) {
      this.selectedFile = target.files[0];
    }
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    this.dragOver = true;
  }

  onDragLeave(event: DragEvent): void {
    event.preventDefault();
    this.dragOver = false;
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    this.dragOver = false;
    if (event.dataTransfer?.files && event.dataTransfer.files.length > 0) {
      this.selectedFile = event.dataTransfer.files[0];
    }
  }

  upload(): void {
    if (!this.selectedFile) {
      this.snackBar.open('Please select a CSV file first', 'Close', {
        duration: 3000,
        panelClass: ['error-snackbar']
      });
      return;
    }
    this.inserted = 0;
    this.loading = true;
    this.api.upload(this.selectedFile).subscribe({
      next: (res) => {
        this.inserted = res.inserted;
        this.loading = false;
        this.snackBar.open(`Successfully inserted ${res.inserted.toLocaleString()} records!`, 'Close', {
          duration: 5000,
          panelClass: ['success-snackbar']
        });
      },
      error: (err) => {
        this.loading = false;
        this.snackBar.open(err?.error?.message || 'Upload failed', 'Close', {
          duration: 5000,
          panelClass: ['error-snackbar']
        });
      }
    });
  }
}
