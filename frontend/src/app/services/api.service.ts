import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface Student {
  studentId: number;
  firstName: string;
  lastName: string;
  dob: string;
  studentClass: string;
  score: number;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly baseUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  generate(count: number): Observable<{ filePath: string }> {
    const params = new HttpParams().set('count', count);
    return this.http.post<{ filePath: string }>(`${this.baseUrl}/generate`, null, { params });
  }

  process(file: File): Observable<{ filePath: string }> {
    const form = new FormData();
    form.append('file', file);
    return this.http.post<{ filePath: string }>(`${this.baseUrl}/process`, form);
  }

  upload(file: File): Observable<{ inserted: number }> {
    const form = new FormData();
    form.append('file', file);
    return this.http.post<{ inserted: number }>(`${this.baseUrl}/upload`, form);
  }

  fetchStudents(page: number, size: number, studentId?: string, studentClass?: string): Observable<PageResponse<Student>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (studentId) {
      params = params.set('studentId', studentId);
    }
    if (studentClass) {
      params = params.set('class', studentClass);
    }
    return this.http.get<PageResponse<Student>>(`${this.baseUrl}/students`, { params });
  }

  export(format: 'excel' | 'csv' | 'pdf'): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/students/export/${format}`, { responseType: 'blob' });
  }
}
