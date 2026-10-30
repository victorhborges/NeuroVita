import { Route, Routes } from '@angular/router';

import { Login } from './pages/login/login';
import { EsqueciSenha } from './pages/esqueci-senha/esqueci-senha';
import { Codigo } from './pages/codigo/codigo';
import { RedefinirSenha } from './pages/redefinir-senha/redefinir-senha';
import { NaoEncontrada } from './pages/nao-encontrada/nao-encontrada';
import { Agenda } from './pages/agenda/agenda';
import { DocumentoEditor } from './pages/documento-editor/documento-editor';
import { DocumentoLeitura } from './pages/documento-leitura/documento-leitura';
import { ConsultaForm } from './pages/consulta-form/consulta-form';
import { GestaoPacientes } from './pages/gestao-pacientes/gestao-pacientes';
import { PacienteForm } from './pages/paciente-form/paciente-form';
import { ProfissionalForm } from './pages/profissional-form/profissional-form';
import { SecretariaForm } from './pages/secretaria-form/secretaria-form';
import { Secretarias } from './pages/secretarias/secretarias';
import { Pacientes } from './pages/pacientes/pacientes';
import { Profissionais } from './pages/profissionais/profissionais';
import { PainelInicio } from './pages/painel-inicio/painel-inicio';
import { PainelShell } from './pages/painel-shell/painel-shell';
import { Prontuario } from './pages/prontuario/prontuario';
import { SecaoPaciente } from './pages/secao-paciente/secao-paciente';
import { CONFIG_PERFIS, Perfil } from './shared/perfil';

/**
 * Cada perfil tem uma "moldura" (menu à esquerda) e as telas dentro dela.
 * O `data: { perfil }` do pai é repassado às telas filhas (ver withRouterConfig em app.config.ts).
 * TODO (back-end): proteger estas rotas (guard) quando o login for real.
 */
function rotasDoPerfil(perfil: Perfil, filhas: Route[] = []): Route {
  return {
    path: perfil,
    component: PainelShell,
    data: { perfil },
    children: [{ path: '', component: PainelInicio, title: CONFIG_PERFIS[perfil].titulo }, ...filhas],
  };
}

/** Telas de quem atende pacientes (psicólogo e psiquiatra): pacientes, prontuário e agenda. */
function rotasDoProfissional(): Route[] {
  return [
    { path: 'pacientes', component: Pacientes, title: 'Pacientes | NeuroVita' },
    { path: 'pacientes/:id', component: Prontuario, title: 'Prontuário | NeuroVita' },
    { path: 'pacientes/:id/:secao', component: SecaoPaciente, title: 'Prontuário | NeuroVita' },
    { path: 'pacientes/:id/:secao/novo', component: DocumentoEditor, title: 'Novo documento | NeuroVita' },
    { path: 'pacientes/:id/:secao/:docId/editar', component: DocumentoEditor, title: 'Editar documento | NeuroVita' },
    { path: 'pacientes/:id/:secao/:docId', component: DocumentoLeitura, title: 'Documento | NeuroVita' },
    { path: 'agenda', component: Agenda, title: 'Agenda | NeuroVita' },
  ];
}

const rotasDaSecretaria: Route[] = [
  { path: 'pacientes', component: GestaoPacientes, title: 'Pacientes | NeuroVita' },
  { path: 'pacientes/novo', component: PacienteForm, title: 'Cadastrar paciente | NeuroVita' },
  { path: 'pacientes/:id/editar', component: PacienteForm, title: 'Editar paciente | NeuroVita' },
  { path: 'profissionais', component: Profissionais, title: 'Profissionais | NeuroVita' },
  { path: 'consultas', component: ConsultaForm, title: 'Gerenciamento de consultas | NeuroVita' },
  { path: 'agenda', component: Agenda, title: 'Agenda | NeuroVita' },
];

const rotasDoAdministrador: Route[] = [
  { path: 'pacientes', component: GestaoPacientes, title: 'Pacientes | NeuroVita' },
  { path: 'profissionais', component: Profissionais, title: 'Profissionais | NeuroVita' },
  { path: 'profissionais/novo', component: ProfissionalForm, title: 'Cadastrar profissional | NeuroVita' },
  { path: 'profissionais/:id/editar', component: ProfissionalForm, title: 'Editar profissional | NeuroVita' },
  { path: 'secretarias', component: Secretarias, title: 'Secretárias | NeuroVita' },
  { path: 'secretarias/novo', component: SecretariaForm, title: 'Cadastrar secretária | NeuroVita' },
  { path: 'secretarias/:id/editar', component: SecretariaForm, title: 'Editar secretária | NeuroVita' },
  { path: 'agenda', component: Agenda, title: 'Agenda | NeuroVita' },
];

export const routes: Routes = [
  { path: '', component: Login, title: 'Entrar | NeuroVita' },
  { path: 'esqueci-senha', component: EsqueciSenha, title: 'Esqueci minha senha | NeuroVita' },
  { path: 'codigo', component: Codigo, title: 'Digite o código | NeuroVita' },
  { path: 'redefinir-senha', component: RedefinirSenha, title: 'Redefinir senha | NeuroVita' },

  rotasDoPerfil('psicologo', rotasDoProfissional()),
  rotasDoPerfil('psiquiatra', rotasDoProfissional()),
  rotasDoPerfil('secretaria', rotasDaSecretaria),
  rotasDoPerfil('admin', rotasDoAdministrador),

  { path: '**', component: NaoEncontrada, title: 'Página não encontrada | NeuroVita' },
];
