import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class ProductService {

  //private baseUrl = 'http://localhost:8080/api';
  private baseUrl = 'https://peaceful-reprieve-production.up.railway.app/api';

  constructor(private http: HttpClient) {}

  getAllProducts() {
    return this.http.get(`${this.baseUrl}/products`);
  }

  addProduct(product: any) {
    return this.http.post(`${this.baseUrl}/products`, product);
  }

  createAlert(userId: number, productId: number, targetPrice: number) {
    return this.http.post(
      `${this.baseUrl}/alerts?userId=${userId}&productId=${productId}&targetPrice=${targetPrice}`,
      {}
    );
  }

  getUserAlerts(userId: number) {
    return this.http.get(`${this.baseUrl}/alerts/user/${userId}`);
  }
}
