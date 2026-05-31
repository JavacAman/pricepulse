import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ProductService } from '../../services/product';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class Dashboard implements OnInit {

  products: any[] = [];
  alerts: any[] = [];
  showAddProduct = false;
  showAddAlert = false;
  selectedProductId = 0;
  targetPrice = 0;
  userName = '';
  formError = '';
  loadError = '';
  loading = true;

  newProduct = {
    name: '',
    category: '',
    currentPrice: 0,
    imageUrl: ''
  };

  constructor(
    private productService: ProductService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.userName = localStorage.getItem('name') || 'User';
    this.loadProducts();
    this.loadAlerts();
  }

  loadProducts() {
    this.loading = true;
    this.loadError = '';
    this.productService.getAllProducts().subscribe({
      next: (data: any) => {
        this.products = data;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.loadError = 'Could not load products. Is the backend running?';
        this.loading = false;
        this.cdr.detectChanges();
        console.error(err);
      }
    });
  }

  loadAlerts() {
    const userId = localStorage.getItem('userId');
    if (userId) {
      this.productService.getUserAlerts(Number(userId)).subscribe({
        next: (data: any) => {
          this.alerts = data;
          this.cdr.detectChanges();
        },
        error: (err) => console.error(err)
      });
    }
  }

  addProduct() {
    if (!this.newProduct.name.trim() || !this.newProduct.category.trim() || !this.newProduct.currentPrice) {
      this.formError = 'Name, category and price are required.';
      return;
    }
    this.formError = '';
    this.productService.addProduct(this.newProduct).subscribe({
      next: () => {
        this.showAddProduct = false;
        this.newProduct = { name: '', category: '', currentPrice: 0, imageUrl: '' };
        this.loadProducts();
      },
      error: (err) => {
        this.formError = 'Failed to save product.';
        console.error(err);
      }
    });
  }

  setAlert(productId: number) {
    this.showAddProduct = false;
    this.formError = '';
    this.selectedProductId = productId;
    this.showAddAlert = true;
  }

  closeProductModal() {
    this.showAddProduct = false;
    this.formError = '';
    this.newProduct = { name: '', category: '', currentPrice: 0, imageUrl: '' };
  }

  createAlert() {
    const userId = parseInt(localStorage.getItem('userId') || '0');
    this.productService.createAlert(
      userId,
      this.selectedProductId,
      this.targetPrice
    ).subscribe({
      next: () => {
        this.showAddAlert = false;
        this.targetPrice = 0;
        this.loadAlerts();
      },
      error: (err) => console.error(err)
    });
  }

  logout() {
    localStorage.clear();
    this.router.navigate(['/login']);
  }
}