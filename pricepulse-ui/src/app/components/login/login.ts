import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import {AuthService} from '../../services/auth';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class Login {
  email = '';
  password = '';
  error = '';

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  onLogin() {
    this.authService.login(this.email, this.password).subscribe({
      next: (res: any) => {
        localStorage.setItem('token', res.token);
        localStorage.setItem('userId', String(res.userId));
        localStorage.setItem('name', res.name);
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.error = 'Invalid email or password';
      }
    });
  }
}
