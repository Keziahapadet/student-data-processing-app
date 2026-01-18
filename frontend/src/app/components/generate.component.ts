import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Subscription } from 'rxjs';
import { ApiService } from '../services/api.service';
import { TimerService, TimerState } from '../services/timer.service';

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
export class GenerateComponent implements OnInit, OnDestroy {
  count = 1000;
  loading = false;
  result = '';
  timerState: TimerState = { isRunning: false, elapsedMs: 0, formattedTime: '0.000s' };
  finalTime = '';

  private timerSubscription?: Subscription;
  private readonly timerId = 'generate';

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

  generate(): void {
    this.result = '';
    this.finalTime = '';
    this.loading = true;
    this.timerService.start(this.timerId);

    this.api.generate(this.count).subscribe({
      next: (res) => {
        const finalState = this.timerService.stop(this.timerId);
        this.finalTime = finalState.formattedTime;
        this.result = res.filePath;
        this.loading = false;
        this.snackBar.open(`Excel file generated in ${this.finalTime}!`, 'Close', {
          duration: 5000,
          panelClass: ['success-snackbar']
        });
      },
      error: (err) => {
        const finalState = this.timerService.stop(this.timerId);
        this.finalTime = finalState.formattedTime;
        this.loading = false;
        this.snackBar.open(err?.error?.message || 'Generation failed', 'Close', {
          duration: 5000,
          panelClass: ['error-snackbar']
        });
      }
    });
  }
}
