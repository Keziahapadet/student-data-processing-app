import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatMenuModule } from '@angular/material/menu';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Subscription } from 'rxjs';
import { ApiService, Student } from '../services/api.service';
import { TimerService, TimerState } from '../services/timer.service';

@Component({
  selector: 'app-report',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatTableModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatMenuModule,
    MatTooltipModule,
    MatSnackBarModule
  ],
  templateUrl: './report.component.html',
  styleUrl: './report.component.scss'
})
export class ReportComponent implements OnInit, OnDestroy {
  students: Student[] = [];
  displayedColumns = ['studentId', 'firstName', 'lastName', 'dob', 'studentClass', 'score'];
  page = 0;
  size = 10;
  total = 0;
  searchId = '';
  classFilter = '';
  loading = false;
  exporting = false;
  readonly classOptions = ['Class1', 'Class2', 'Class3', 'Class4', 'Class5'];

  // Timer states
  loadTimerState: TimerState = { isRunning: false, elapsedMs: 0, formattedTime: '0.000s' };
  exportTimerState: TimerState = { isRunning: false, elapsedMs: 0, formattedTime: '0.000s' };
  lastLoadTime = '';
  lastExportTime = '';

  private loadTimerSubscription?: Subscription;
  private exportTimerSubscription?: Subscription;
  private readonly loadTimerId = 'report-load';
  private readonly exportTimerId = 'report-export';

  constructor(
    private api: ApiService,
    private snackBar: MatSnackBar,
    private timerService: TimerService
  ) {}

  ngOnInit(): void {
    this.loadTimerSubscription = this.timerService.createTimer(this.loadTimerId)
      .subscribe(state => this.loadTimerState = state);
    this.exportTimerSubscription = this.timerService.createTimer(this.exportTimerId)
      .subscribe(state => this.exportTimerState = state);
    this.load();
  }

  ngOnDestroy(): void {
    this.loadTimerSubscription?.unsubscribe();
    this.exportTimerSubscription?.unsubscribe();
    this.timerService.reset(this.loadTimerId);
    this.timerService.reset(this.exportTimerId);
  }

  load(): void {
    this.loading = true;
    this.timerService.start(this.loadTimerId);

    this.api.fetchStudents(this.page, this.size, this.searchId, this.classFilter).subscribe({
      next: (res) => {
        const finalState = this.timerService.stop(this.loadTimerId);
        this.lastLoadTime = finalState.formattedTime;
        this.students = res.content;
        this.total = res.totalElements;
        this.loading = false;
      },
      error: () => {
        const finalState = this.timerService.stop(this.loadTimerId);
        this.lastLoadTime = finalState.formattedTime;
        this.loading = false;
        this.snackBar.open('Failed to load students', 'Close', {
          duration: 3000,
          panelClass: ['error-snackbar']
        });
      }
    });
  }

  onPageChange(event: PageEvent): void {
    this.page = event.pageIndex;
    this.size = event.pageSize;
    this.load();
  }

  applyFilters(): void {
    this.page = 0;
    this.searchId = this.searchId.trim();
    this.classFilter = this.classFilter.trim();
    this.load();
  }

  clearFilters(): void {
    this.searchId = '';
    this.classFilter = '';
    this.page = 0;
    this.load();
  }

  export(format: 'excel' | 'csv' | 'pdf'): void {
    this.exporting = true;
    this.lastExportTime = '';
    this.timerService.start(this.exportTimerId);

    this.snackBar.open(`Exporting to ${format.toUpperCase()}...`, '', { duration: 2000 });

    this.api.export(format).subscribe({
      next: (blob) => {
        const finalState = this.timerService.stop(this.exportTimerId);
        this.lastExportTime = finalState.formattedTime;
        this.exporting = false;

        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `students.${format === 'excel' ? 'xlsx' : format}`;
        link.click();
        window.URL.revokeObjectURL(url);

        this.snackBar.open(`Exported in ${this.lastExportTime}!`, 'Close', {
          duration: 3000,
          panelClass: ['success-snackbar']
        });
      },
      error: () => {
        const finalState = this.timerService.stop(this.exportTimerId);
        this.lastExportTime = finalState.formattedTime;
        this.exporting = false;

        this.snackBar.open('Export failed', 'Close', {
          duration: 3000,
          panelClass: ['error-snackbar']
        });
      }
    });
  }
}
