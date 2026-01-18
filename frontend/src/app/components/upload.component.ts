import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Subscription } from 'rxjs';
import { ApiService } from '../services/api.service';
import { TimerService, TimerState } from '../services/timer.service';

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
export class UploadComponent implements OnInit, OnDestroy {
  selectedFile?: File;
  loading = false;
  inserted = 0;
  dragOver = false;
  timerState: TimerState = { isRunning: false, elapsedMs: 0, formattedTime: '0.000s' };
  finalTime = '';

  private timerSubscription?: Subscription;
  private readonly timerId = 'upload';

  constructor(
    private api: ApiService,
    private snackBar: MatSnackBar,
    private timerService: TimerService
  ) {}

  ngOnInit(): void {
    this.timerSubscription = this.timerService.createTimer(this.timerId)
      .subscribe(state => this.timerState = state);
  }

  ngOnDestroy(): void {
    this.timerSubscription?.unsubscribe();
    this.timerService.reset(this.timerId);
  }

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
    this.finalTime = '';
    this.loading = true;
    this.timerService.start(this.timerId);

    this.api.upload(this.selectedFile).subscribe({
      next: (res) => {
        const finalState = this.timerService.stop(this.timerId);
        this.finalTime = finalState.formattedTime;
        this.inserted = res.inserted;
        this.loading = false;
        this.snackBar.open(`Inserted ${res.inserted.toLocaleString()} records in ${this.finalTime}!`, 'Close', {
          duration: 5000,
          panelClass: ['success-snackbar']
        });
      },
      error: (err) => {
        const finalState = this.timerService.stop(this.timerId);
        this.finalTime = finalState.formattedTime;
        this.loading = false;
        this.snackBar.open(err?.error?.message || 'Upload failed', 'Close', {
          duration: 5000,
          panelClass: ['error-snackbar']
        });
      }
    });
  }
}
