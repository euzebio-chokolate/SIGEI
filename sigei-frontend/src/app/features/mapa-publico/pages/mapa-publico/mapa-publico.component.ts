import { ChangeDetectorRef, Component, OnDestroy, OnInit, inject } from '@angular/core';
import * as L from 'leaflet';
import { InfraestruturaService } from '@domains/escolas/data-access/infraestrutura.service';
import { HeaderComponent } from '@shared/ui/header/header.component';
import { EscolaMapaItem, MapaBuscaComponent } from '@features/mapa-publico/ui/mapa-busca/mapa-busca.component';
import { MapaFiltros, MapaFiltrosComponent } from '@features/mapa-publico/ui/mapa-filtros/mapa-filtros.component';
import { environment } from '@environments/environment';

type CamadaMapa = 'padrao' | 'satelite' | 'terreno' | 'escuro';

@Component({
  selector: 'app-mapa-publico',
  standalone: true,
  imports: [HeaderComponent, MapaFiltrosComponent, MapaBuscaComponent],
  templateUrl: './mapa-publico.component.html',
})
export class MapaPublicoComponent implements OnInit, OnDestroy {
  private readonly infraestruturaService = inject(InfraestruturaService);
  private readonly cdr = inject(ChangeDetectorRef);

  private map?: L.Map;
  private tileLayerAtual?: L.TileLayer;
  private readonly grupoMarcadores = L.layerGroup();
  private marcadoresPorId = new Map<number, L.Marker>();

  carregando = true;
  erroMensagem = '';
  menuCamadasAberto = false;
  camadaAtual: CamadaMapa = 'padrao';
  private abrirPopupAoPassarMouse = true;
  private exibirFotosNoMapa = true;

  readonly nomesCamadas: Record<CamadaMapa, string> = {
    padrao: 'Padrão',
    satelite: 'Satélite',
    terreno: 'Terreno',
    escuro: 'Escuro',
  };

  private readonly camadas: Record<CamadaMapa, L.TileLayer> = {
    padrao: L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '&copy; OpenStreetMap contributors',
    }),
    satelite: L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}', {
      maxZoom: 18,
      attribution: 'Tiles &copy; Esri',
    }),
    terreno: L.tileLayer('https://{s}.tile.opentopomap.org/{z}/{x}/{y}.png', {
      maxZoom: 17,
      attribution: 'Map data &copy; OpenStreetMap contributors, SRTM | Map style &copy; OpenTopoMap',
    }),
    escuro: L.tileLayer('https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png', {
      maxZoom: 19,
      attribution: '&copy; OpenStreetMap contributors &copy; CARTO',
    }),
  };

  dadosEscolasOriginal: EscolaMapaItem[] = [];
  dadosEscolasFiltrados: EscolaMapaItem[] = [];
  termoBuscaTexto = '';
  filtrosAvancados: MapaFiltros = {
    situacao: '',
    tipoAcesso: '',
    apenasComInternet: false,
    apenasComPredio: false,
  };

  ngOnInit(): void {
    this.carregarPreferencias();
    this.iniciarMapa();
    this.buscarDadosDaApi();
  }

  ngOnDestroy(): void {
    this.map?.remove();
  }

  aoMudarBusca(termo: string): void {
    this.termoBuscaTexto = termo;
    this.processarFiltros();
  }

  aoMudarFiltros(filtros: MapaFiltros): void {
    this.filtrosAvancados = { ...filtros };
    this.processarFiltros();
  }

  focarEscola(item: EscolaMapaItem): void {
    const coordenadas = this.obterCoordenadas(item);
    if (!coordenadas || !this.map) return;

    this.map.flyTo(coordenadas, 13, { duration: 0.8 });
    window.setTimeout(() => this.marcadoresPorId.get(item.id)?.openPopup(), 850);
  }

  mudarCamada(novaCamada: CamadaMapa): void {
    if (!this.map || this.camadaAtual === novaCamada) {
      this.menuCamadasAberto = false;
      return;
    }

    if (this.tileLayerAtual) this.map.removeLayer(this.tileLayerAtual);
    this.camadaAtual = novaCamada;
    this.tileLayerAtual = this.camadas[novaCamada].addTo(this.map);
    this.tileLayerAtual.bringToBack();
    this.menuCamadasAberto = false;
  }

  tentarNovamente(): void {
    this.buscarDadosDaApi();
  }

  private iniciarMapa(): void {
    this.map = L.map('map', { zoomControl: false, minZoom: 5 }).setView([-8.77, -70.55], 7);
    L.control.zoom({ position: 'bottomright' }).addTo(this.map);
    L.control.scale({ position: 'bottomright', imperial: false }).addTo(this.map);
    this.tileLayerAtual = this.camadas[this.camadaAtual].addTo(this.map);
    this.grupoMarcadores.addTo(this.map);
  }

  private buscarDadosDaApi(): void {
    this.carregando = true;
    this.erroMensagem = '';

    // Um marcador por escola, com os dados do ano mais recente
    this.infraestruturaService.listarMaisRecentes().subscribe({
      next: (dadosRecebidos: EscolaMapaItem[]) => {
        this.dadosEscolasOriginal = dadosRecebidos ?? [];
        this.processarFiltros();
        this.carregando = false;
        this.cdr.markForCheck();
      },
      error: (erro) => {
        console.error('Erro ao carregar escolas do mapa:', erro);
        this.erroMensagem = 'Não foi possível carregar as escolas no mapa.';
        this.carregando = false;
        this.cdr.markForCheck();
      },
    });
  }

  private processarFiltros(): void {
    const termo = this.normalizar(this.termoBuscaTexto);

    this.dadosEscolasFiltrados = this.dadosEscolasOriginal.filter((item) => {
      const nome = this.normalizar(item.escola?.nome);
      const codigo = this.normalizar(item.escola?.codigo);
      const situacao = this.normalizar(item.situacao).toUpperCase();
      const acesso = this.normalizar(item.tipoAcesso).toUpperCase();

      const passaBusca = !termo || nome.includes(termo) || codigo.includes(termo);
      const passaSituacao = !this.filtrosAvancados.situacao || situacao === this.filtrosAvancados.situacao;
      const passaAcesso = !this.filtrosAvancados.tipoAcesso || acesso === this.filtrosAvancados.tipoAcesso;
      const passaInternet = !this.filtrosAvancados.apenasComInternet || item.temInternet === true;
      const passaPredio = !this.filtrosAvancados.apenasComPredio || item.temPredio === true;

      return passaBusca && passaSituacao && passaAcesso && passaInternet && passaPredio;
    });

    this.adicionarPinsEscolas();
  }

  private adicionarPinsEscolas(): void {
    this.grupoMarcadores.clearLayers();
    this.marcadoresPorId.clear();

    const iconeVerde = this.criarIcone('assets/markers/escola-marker-green.svg');
    const iconeVermelho = this.criarIcone('assets/markers/escola-marker-red.svg');

    for (const item of this.dadosEscolasFiltrados) {
      const coordenadas = this.obterCoordenadas(item);
      if (!coordenadas) continue;

      const paralisada = this.isParalisada(item.situacao);
      const marcador = L.marker(coordenadas, {
        icon: paralisada ? iconeVermelho : iconeVerde,
        title: String(item.escola?.nome || 'Escola indígena'),
        alt: String(item.escola?.nome || 'Escola indígena'),
      });

      marcador.bindPopup(this.criarPopup(item, paralisada), { maxWidth: 340, minWidth: 240 });
      if (this.abrirPopupAoPassarMouse) {
        marcador.on('mouseover', () => marcador.openPopup());
      }

      marcador.addTo(this.grupoMarcadores);
      this.marcadoresPorId.set(item.id, marcador);
    }
  }

  private criarPopup(item: EscolaMapaItem, paralisada: boolean): string {
    const nome = this.escaparHtml(item.escola?.nome || 'Escola sem nome');
    const codigo = this.escaparHtml(item.escola?.codigo || 'Sem INEP');
    const situacao = this.escaparHtml(item.situacao || 'Não informada');
    const acesso = this.escaparHtml(item.tipoAcesso || 'Não informado');
    const fotoUrl = this.exibirFotosNoMapa ? this.resolverFotoUrl(item.fotoUrl) : null;
    const foto = fotoUrl
      ? `<img src="${this.escaparHtml(fotoUrl)}" alt="Foto de ${nome}" class="mb-3 h-40 w-full rounded-lg bg-slate-100 object-cover" loading="lazy">`
      : '';

    return `
      <article class="min-w-[220px] font-sans">
        ${foto}
        <h3 class="mb-1 text-base font-bold leading-tight text-slate-800">${nome}</h3>
        <p class="mb-3 inline-block rounded border border-slate-200 bg-slate-100 px-1.5 py-0.5 font-mono text-[11px] text-slate-500">INEP: ${codigo}</p>
        <div class="mb-3 grid grid-cols-2 gap-3 border-y border-slate-100 py-2">
          <div><span class="block text-[9px] font-bold uppercase text-slate-400">Situação</span><strong class="text-xs ${paralisada ? 'text-red-600' : 'text-emerald-600'}">${situacao}</strong></div>
          <div><span class="block text-[9px] font-bold uppercase text-slate-400">Acesso</span><strong class="text-xs text-slate-700">${acesso}</strong></div>
        </div>
        <div class="mb-3 flex gap-2">
          ${this.criarSelo('Internet', Boolean(item.temInternet), 'sky')}
          ${this.criarSelo('Prédio', Boolean(item.temPredio), 'emerald')}
        </div>
      </article>`;
  }

  private resolverFotoUrl(fotoUrl?: string | null): string | null {
    if (!fotoUrl) return null;
    if (/^(data:|blob:|https?:\/\/)/i.test(fotoUrl)) return fotoUrl;

    const apiUrl = environment.apiUrl.replace(/\/$/, '');
    const caminho = fotoUrl.replace(/^\//, '');
    return `${apiUrl}/${caminho}`;
  }

  private carregarPreferencias(): void {
    const preferenciasSalvas = localStorage.getItem('sigei-preferencias');
    if (!preferenciasSalvas) return;

    try {
      const preferencias = JSON.parse(preferenciasSalvas) as {
        camadaMapa?: CamadaMapa;
        abrirPopupAoPassarMouse?: boolean;
        exibirFotosNoMapa?: boolean;
      };

      if (preferencias.camadaMapa && preferencias.camadaMapa in this.camadas) {
        this.camadaAtual = preferencias.camadaMapa;
      }
      this.abrirPopupAoPassarMouse = preferencias.abrirPopupAoPassarMouse ?? true;
      this.exibirFotosNoMapa = preferencias.exibirFotosNoMapa ?? true;
    } catch {
      localStorage.removeItem('sigei-preferencias');
    }
  }

  private criarSelo(rotulo: string, ativo: boolean, cor: 'sky' | 'emerald'): string {
    const classe = ativo
      ? cor === 'sky' ? 'border-sky-100 bg-sky-50 text-sky-700' : 'border-emerald-100 bg-emerald-50 text-emerald-700'
      : 'border-slate-200 bg-slate-100 text-slate-400';
    return `<span class="rounded border px-2 py-0.5 text-[10px] font-bold ${classe}">${ativo ? '✓ ' : ''}${rotulo}</span>`;
  }

  private criarIcone(url: string): L.Icon {
    return L.icon({ iconUrl: url, iconSize: [46, 46], iconAnchor: [23, 46], popupAnchor: [0, -42] });
  }

  private obterCoordenadas(item: EscolaMapaItem): L.LatLngTuple | null {
    // Number(null) e Number('') valem 0: sem esta checagem, a escola sem coordenadas iria para (0, 0)
    if (!this.temValor(item.latitude) || !this.temValor(item.longitude)) return null;
    const latitude = Number(item.latitude);
    const longitude = Number(item.longitude);
    if (!Number.isFinite(latitude) || !Number.isFinite(longitude)) return null;
    if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) return null;
    return [latitude, longitude];
  }

  private temValor(valor: unknown): boolean {
    return valor !== null && valor !== undefined && String(valor).trim() !== '';
  }

  /** Escola fora de funcionamento (paralisada ou desativada): marcador vermelho. */
  private isParalisada(valor?: string | null): boolean {
    const situacao = this.normalizar(valor).toUpperCase();
    return situacao === 'PARALISADA' || situacao === 'PARALIZADA' || situacao === 'DESATIVADA';
  }

  private normalizar(valor: unknown): string {
    return String(valor ?? '').normalize('NFD').replace(/[\u0300-\u036f]/g, '').trim().toLowerCase();
  }

  private escaparHtml(valor: unknown): string {
    return String(valor ?? '').replace(/[&<>'"]/g, (caractere) => ({
      '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#039;', '"': '&quot;',
    })[caractere] ?? caractere);
  }
}
