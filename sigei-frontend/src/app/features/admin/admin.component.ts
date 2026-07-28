import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [RouterLink],
  template: `
    <div class="min-h-screen bg-gray-100 p-8 flex flex-col items-center justify-center">
      <h1 class="text-3xl font-bold text-gray-800 mb-4">Área Administrativa</h1>
      <p class="text-gray-600 mb-6">Em construção...</p>
      <a routerLink="/" class="text-green-600 hover:underline font-semibold">
        Voltar para o Mapa Público
      </a>
    </div>
  `
})
export class AdminComponent {}