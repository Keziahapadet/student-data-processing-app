import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    MatSidenavModule,
    MatToolbarModule,
    MatListModule,
    MatIconModule,
    MatButtonModule
  ],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  title = 'Student Data Processing';
  sidenavOpened = true;

  navItems = [
    { path: '/generate', icon: 'add_circle', label: 'Generate Data', description: 'Create Excel files' },
    { path: '/process', icon: 'sync', label: 'Process Data', description: 'Excel to CSV' },
    { path: '/upload', icon: 'cloud_upload', label: 'Upload Data', description: 'CSV to Database' },
    { path: '/report', icon: 'assessment', label: 'Reports', description: 'View & Export' }
  ];

  toggleSidenav(): void {
    this.sidenavOpened = !this.sidenavOpened;
  }
}