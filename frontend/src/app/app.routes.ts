import { Route, Routes } from '@angular/router';

import { PainelShell } from './pages/painel-shell/painel-shell';
import { CONFIG_PERFIS, Perfil } from './shared/perfil';
import { authGuard } from './shared/auth.guard';

/**
 * Cada perfil tem uma "moldura" (menu à esquerda) e as telas dentro dela.
 * O `data: { perfil }` do pai é repassado às telas filhas (ver withRouterConfig em app.config.ts).
 * A guarda protege a navegação; os endpoints continuam aplicando autorização no backend.
 */
function rotasDoPerfil(perfil: Perfil, filhas: Route[] = []): Route {
  return {
    path: perfil,
    component: PainelShell,
    canActivate: [authGuard],
    data: { perfil },
    children: [
      {
        path: '',
        loadComponent: () => import('./pages/painel-inicio/painel-inicio').then((m) => m.PainelInicio),
        title: CONFIG_PERFIS[perfil].titulo,
      },
      ...filhas,
      ...rotasComuns(),
    ],
  };
}

function rotasComuns(): Route[] {
  return [
    { path: 'configuracoes', loadComponent: () => import('./pages/configuracoes/configuracoes').then((m) => m.Configuracoes), title: 'Configurações | NeuroVita' },
    { path: 'ajuda', loadComponent: () => import('./pages/ajuda/ajuda').then((m) => m.Ajuda), title: 'Ajuda | NeuroVita' },
    { path: 'ajuda/fale-conosco', loadComponent: () => import('./pages/fale-conosco/fale-conosco').then((m) => m.FaleConosco), title: 'Fale conosco | NeuroVita' },
    { path: 'ajuda/:topico', loadComponent: () => import('./pages/ajuda-topico/ajuda-topico').then((m) => m.AjudaTopico), title: 'Ajuda | NeuroVita' },
  ];
}

/** Telas de quem atende pacientes (psicólogo e psiquiatra): pacientes, prontuário e agenda. */
function rotasDoProfissional(): Route[] {
  return [
    { path: 'pacientes', loadComponent: () => import('./pages/pacientes/pacientes').then((m) => m.Pacientes), title: 'Pacientes | NeuroVita' },
    { path: 'pacientes/:id', loadComponent: () => import('./pages/prontuario/prontuario').then((m) => m.Prontuario), title: 'Prontuário | NeuroVita' },
    { path: 'pacientes/:id/:secao', loadComponent: () => import('./pages/secao-paciente/secao-paciente').then((m) => m.SecaoPaciente), title: 'Prontuário | NeuroVita' },
    { path: 'pacientes/:id/:secao/novo', loadComponent: () => import('./pages/documento-editor/documento-editor').then((m) => m.DocumentoEditor), title: 'Novo documento | NeuroVita' },
    { path: 'pacientes/:id/:secao/:docId/editar', loadComponent: () => import('./pages/documento-editor/documento-editor').then((m) => m.DocumentoEditor), title: 'Editar documento | NeuroVita' },
    { path: 'pacientes/:id/:secao/:docId', loadComponent: () => import('./pages/documento-leitura/documento-leitura').then((m) => m.DocumentoLeitura), title: 'Documento | NeuroVita' },
    { path: 'agenda', loadComponent: () => import('./pages/agenda/agenda').then((m) => m.Agenda), title: 'Agenda | NeuroVita' },
  ];
}

const rotasDaSecretaria: Route[] = [
  { path: 'pacientes', loadComponent: () => import('./pages/gestao-pacientes/gestao-pacientes').then((m) => m.GestaoPacientes), title: 'Pacientes | NeuroVita' },
  { path: 'pacientes/novo', loadComponent: () => import('./pages/paciente-form/paciente-form').then((m) => m.PacienteForm), title: 'Cadastrar paciente | NeuroVita' },
  { path: 'pacientes/:id/editar', loadComponent: () => import('./pages/paciente-form/paciente-form').then((m) => m.PacienteForm), title: 'Editar paciente | NeuroVita' },
  { path: 'profissionais', loadComponent: () => import('./pages/profissionais/profissionais').then((m) => m.Profissionais), title: 'Profissionais | NeuroVita' },
  { path: 'consultas', loadComponent: () => import('./pages/consulta-form/consulta-form').then((m) => m.ConsultaForm), title: 'Gerenciamento de consultas | NeuroVita' },
  { path: 'agenda', loadComponent: () => import('./pages/agenda/agenda').then((m) => m.Agenda), title: 'Agenda | NeuroVita' },
];

const rotasDoAdministrador: Route[] = [
  { path: 'pacientes', loadComponent: () => import('./pages/gestao-pacientes/gestao-pacientes').then((m) => m.GestaoPacientes), title: 'Pacientes | NeuroVita' },
  { path: 'profissionais', loadComponent: () => import('./pages/profissionais/profissionais').then((m) => m.Profissionais), title: 'Profissionais | NeuroVita' },
  { path: 'profissionais/novo', loadComponent: () => import('./pages/profissional-form/profissional-form').then((m) => m.ProfissionalForm), title: 'Cadastrar profissional | NeuroVita' },
  { path: 'profissionais/:id/editar', loadComponent: () => import('./pages/profissional-form/profissional-form').then((m) => m.ProfissionalForm), title: 'Editar profissional | NeuroVita' },
  { path: 'secretarias', loadComponent: () => import('./pages/secretarias/secretarias').then((m) => m.Secretarias), title: 'Secretárias | NeuroVita' },
  { path: 'secretarias/novo', loadComponent: () => import('./pages/secretaria-form/secretaria-form').then((m) => m.SecretariaForm), title: 'Cadastrar secretária | NeuroVita' },
  { path: 'secretarias/:id/editar', loadComponent: () => import('./pages/secretaria-form/secretaria-form').then((m) => m.SecretariaForm), title: 'Editar secretária | NeuroVita' },
  { path: 'agenda', loadComponent: () => import('./pages/agenda/agenda').then((m) => m.Agenda), title: 'Agenda | NeuroVita' },
];

export const routes: Routes = [
  { path: '', loadComponent: () => import('./pages/login/login').then((m) => m.Login), title: 'Entrar | NeuroVita' },
  { path: 'esqueci-senha', loadComponent: () => import('./pages/esqueci-senha/esqueci-senha').then((m) => m.EsqueciSenha), title: 'Esqueci minha senha | NeuroVita' },
  { path: 'codigo', loadComponent: () => import('./pages/codigo/codigo').then((m) => m.Codigo), title: 'Digite o código | NeuroVita' },
  { path: 'redefinir-senha', loadComponent: () => import('./pages/redefinir-senha/redefinir-senha').then((m) => m.RedefinirSenha), title: 'Redefinir senha | NeuroVita' },
  { path: 'primeiro-acesso', loadComponent: () => import('./pages/primeiro-acesso/primeiro-acesso').then((m) => m.PrimeiroAcesso), title: 'Definir senha | NeuroVita' },

  rotasDoPerfil('psicologo', rotasDoProfissional()),
  rotasDoPerfil('psiquiatra', rotasDoProfissional()),
  rotasDoPerfil('secretaria', rotasDaSecretaria),
  rotasDoPerfil('admin', rotasDoAdministrador),

  { path: '**', loadComponent: () => import('./pages/nao-encontrada/nao-encontrada').then((m) => m.NaoEncontrada), title: 'Página não encontrada | NeuroVita' },
];
