import { Component, computed, effect, inject, input } from '@angular/core';
import { RouterLink } from '@angular/router';

import { Clinica } from '../../shared/clinica/clinica';
import { nomeProfissional } from '../../shared/clinica/rotulos';
import { SECOES, secoesDoPerfil } from '../../shared/clinica/secoes';
import { FaixaPaciente } from '../../shared/faixa-paciente/faixa-paciente';
import { Icone } from '../../shared/icone/icone';
import { Perfil } from '../../shared/perfil';
import { TituloPagina } from '../../shared/titulo-pagina/titulo-pagina';
import { formatarData, paraISO } from '../../shared/util';

/** Prontuário do paciente: dados pessoais, responsável, atalhos e histórico de consultas. */
@Component({
    selector: 'app-prontuario',
    imports: [FaixaPaciente, Icone, RouterLink, TituloPagina],
    templateUrl: './prontuario.html',
})
export class Prontuario {
    private readonly clinica = inject(Clinica);

    /** Vem da rota: /:perfil/pacientes/:id */
    readonly id = input.required<string>();
    /** Vem do `data` da rota (psicologo ou psiquiatra). */
    readonly perfil = input<Perfil>('psicologo');
    protected readonly raiz = computed(() => `/${this.perfil()}`);

    protected readonly paciente = computed(() => this.clinica.paciente(this.id()));
    protected readonly nascimento = computed(() => formatarData(this.paciente()?.nascimento));
    protected readonly endereco = computed(() => {
        const paciente = this.paciente();
        if (!paciente) return '—';
        const partes = [paciente.endereco, paciente.numero, paciente.complemento, paciente.bairro, paciente.cidade, paciente.cep]
            .map((parte) => parte?.trim())
            .filter((parte): parte is string => !!parte);
        return partes.join(', ') || '—';
    });
    protected readonly secoes = computed(() => secoesDoPerfil(this.perfil()).map((id) => SECOES[id]));
    protected readonly consultas = computed(() =>
        (this.paciente()?.consultas ?? []).map((c) => {
            const profissional = this.clinica.profissional(c.profissional);
            return {
                ...c,
                profissional: profissional ? nomeProfissional(profissional) : c.profissional || '—',
                dataBR: formatarData(c.data),
            };
        }),
    );

    protected readonly ultimas = computed(() => {
        const agora = new Date();
        const referencia = `${paraISO(agora)} ${String(agora.getHours()).padStart(2, '0')}:${String(agora.getMinutes()).padStart(2, '0')}`;
        const chave = (consulta: { data: string; inicio: string }) => `${consulta.data} ${consulta.inicio}`;
        const consultas = this.clinica.agendamentos()
            .filter((consulta) => consulta.pacienteId === this.id() && consulta.status !== 'cancelado');
        const exibir = (consulta: (typeof consultas)[number] | undefined) => {
            if (!consulta) return null;
            const profissional = this.clinica.profissional(consulta.profissionalId);
            return {
                dataBR: formatarData(consulta.data),
                profissional: profissional ? `${nomeProfissional(profissional)} - ${profissional.especialidade}` : '—',
            };
        };
        const proxima = consultas.filter((consulta) => chave(consulta) >= referencia)
            .sort((a, b) => chave(a).localeCompare(chave(b)))[0];
        const ultima = consultas.filter((consulta) => chave(consulta) < referencia)
            .sort((a, b) => chave(b).localeCompare(chave(a)))[0];
        return { proxima: exibir(proxima), ultima: exibir(ultima) };
    });

    constructor() {
        effect(() => this.clinica.carregarDocumentos(this.id()));
        effect(() => this.clinica.carregarDiagnosticos(this.id()));
    }

}