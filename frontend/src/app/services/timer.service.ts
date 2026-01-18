import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, interval, Subscription } from 'rxjs';
import { map } from 'rxjs/operators';

export interface TimerState {
  isRunning: boolean;
  elapsedMs: number;
  formattedTime: string;
}

@Injectable({
  providedIn: 'root'
})
export class TimerService {
  private timers: Map<string, {
    startTime: number;
    subscription: Subscription | null;
    subject: BehaviorSubject<TimerState>;
  }> = new Map();

  createTimer(id: string): Observable<TimerState> {
    if (!this.timers.has(id)) {
      this.timers.set(id, {
        startTime: 0,
        subscription: null,
        subject: new BehaviorSubject<TimerState>({
          isRunning: false,
          elapsedMs: 0,
          formattedTime: '0.000s'
        })
      });
    }
    return this.timers.get(id)!.subject.asObservable();
  }

  start(id: string): void {
    const timer = this.timers.get(id);
    if (!timer) return;

    timer.startTime = performance.now();
    timer.subscription?.unsubscribe();

    timer.subscription = interval(10).pipe(
      map(() => {
        const elapsedMs = performance.now() - timer.startTime;
        return {
          isRunning: true,
          elapsedMs,
          formattedTime: this.formatTime(elapsedMs)
        };
      })
    ).subscribe(state => timer.subject.next(state));
  }

  stop(id: string): TimerState {
    const timer = this.timers.get(id);
    if (!timer) {
      return { isRunning: false, elapsedMs: 0, formattedTime: '0.000s' };
    }

    timer.subscription?.unsubscribe();
    timer.subscription = null;

    const elapsedMs = performance.now() - timer.startTime;
    const finalState: TimerState = {
      isRunning: false,
      elapsedMs,
      formattedTime: this.formatTime(elapsedMs)
    };

    timer.subject.next(finalState);
    return finalState;
  }

  reset(id: string): void {
    const timer = this.timers.get(id);
    if (!timer) return;

    timer.subscription?.unsubscribe();
    timer.subscription = null;
    timer.startTime = 0;
    timer.subject.next({
      isRunning: false,
      elapsedMs: 0,
      formattedTime: '0.000s'
    });
  }

  private formatTime(ms: number): string {
    if (ms < 1000) {
      return `${ms.toFixed(0)}ms`;
    } else if (ms < 60000) {
      return `${(ms / 1000).toFixed(3)}s`;
    } else {
      const minutes = Math.floor(ms / 60000);
      const seconds = ((ms % 60000) / 1000).toFixed(1);
      return `${minutes}m ${seconds}s`;
    }
  }
}
