import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ApiService } from '../services/api.service';

@Component({
  selector: 'app-generate',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule,
    MatSnackBarModule
  ],
  templateUrl: './generate.component.html',
  styleUrl: './generate.component.scss'
})
export class GenerateComponent {
  count = 1000;
  loading = false;
  result = '';

  constructor(
    private api: ApiService,
    private snackBar: MatSnackBar
  ) {}

  generate(): void {
    this.result = '';
    this.loading = true;
    this.api.generate(this.count).subscribe({
      next: (res) => {
        this.result = res.filePath;
        this.loading = false;
        this.snackBar.open('Excel file generated successfully!', 'Close', {
          duration: 5000,
          panelClass: ['success-snackbar']
        });
      },
      error: (err) => {
        this.loading = false;
        this.snackBar.open(err?.error?.message || 'Generation failed', 'Close', {
          duration: 5000,
          panelClass: ['error-snackbar']
        });
      }
    });
  }
}
