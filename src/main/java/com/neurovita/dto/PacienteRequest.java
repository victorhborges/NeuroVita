package com.neurovita.dto;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;

public class PacienteRequest {

    @NotBlank(message = "O nome do paciente é obrigatório.")
    private String nome;

    @NotNull(message = "A data de nascimento é obrigatória.")
    @Past(message = "A data de nascimento deve ser anterior à data atual.")
    private LocalDate dataNascimento;

    @Email(message = "Informe um e-mail válido.")
    private String email;

    @Pattern(regexp = "^[0-9.\\- ]{11,14}$", message = "O CPF deve conter 11 dígitos, podendo incluir pontos e hífen.")
    private String cpf;

    @Pattern(regexp = "^[0-9()+.\\- ]{10,20}$", message = "Informe um telefone válido.")
    private String telefone;

    private String endereco;

    @Pattern(regexp = "^[0-9.\\- ]{8,10}$", message = "O CEP deve conter 8 dígitos, podendo incluir hífen.")
    private String cep;

    private String cidade;
    private String numero;
    private String complemento;
    private String bairro;
    private LocalDate dataInicioAcompanhamento;
    private LocalDate dataEncerramentoAcompanhamento;
    private Boolean ativo;

    @Valid
    @Schema(description = "Responsável-paciente links. Omitted/null preserves current links on update; an empty list explicitly requests removal, subject to the minor-patient rule.")
    private List<VinculoResponsavelRequest> responsaveis;

    private Boolean possuiPlanoSaude;
    private String planoSaude;
    private String numeroCarteirinha;
    private LocalDate validadePlano;

    public PacienteRequest() {
    }

    @AssertTrue(message = "Informe um telefone válido.")
    public boolean isTelefoneValido() {
        if (telefone == null || telefone.isBlank()) {
            return true;
        }

        String numeros = telefone.replaceAll("\\D", "");

        if (numeros.length() != 10 && numeros.length() != 11) {
            return false;
        }

        if (numeros.matches("^(\\d)\\1+$")) {
            return false;
        }

        if (numeros.startsWith("0")) {
            return false;
        }

        String assinante = numeros.substring(2);

        return !assinante.matches("^0+$");
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = normalizarVazio(nome);
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = normalizarVazio(email);
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = normalizarVazio(cpf);
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = normalizarVazio(telefone);
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = normalizarVazio(endereco);
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = normalizarVazio(cep);
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = normalizarVazio(cidade);
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = normalizarVazio(numero);
    }

    public String getComplemento() {
        return complemento;
    }

    public void setComplemento(String complemento) {
        this.complemento = normalizarVazio(complemento);
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = normalizarVazio(bairro);
    }

    public LocalDate getDataInicioAcompanhamento() {
        return dataInicioAcompanhamento;
    }

    public void setDataInicioAcompanhamento(LocalDate dataInicioAcompanhamento) {
        this.dataInicioAcompanhamento = dataInicioAcompanhamento;
    }

    public LocalDate getDataEncerramentoAcompanhamento() {
        return dataEncerramentoAcompanhamento;
    }

    public void setDataEncerramentoAcompanhamento(LocalDate dataEncerramentoAcompanhamento) {
        this.dataEncerramentoAcompanhamento = dataEncerramentoAcompanhamento;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public List<VinculoResponsavelRequest> getResponsaveis() {
        return responsaveis;
    }

    public void setResponsaveis(List<VinculoResponsavelRequest> responsaveis) {
        this.responsaveis = responsaveis;
    }

    public Boolean getPossuiPlanoSaude() {
        return possuiPlanoSaude;
    }

    public void setPossuiPlanoSaude(Boolean possuiPlanoSaude) {
        this.possuiPlanoSaude = possuiPlanoSaude;
    }

    public String getPlanoSaude() {
        return planoSaude;
    }

    public void setPlanoSaude(String planoSaude) {
        this.planoSaude = normalizarVazio(planoSaude);
    }

    public String getNumeroCarteirinha() {
        return numeroCarteirinha;
    }

    public void setNumeroCarteirinha(String numeroCarteirinha) {
        this.numeroCarteirinha = normalizarVazio(numeroCarteirinha);
    }

    public LocalDate getValidadePlano() {
        return validadePlano;
    }

    public void setValidadePlano(LocalDate validadePlano) {
        this.validadePlano = validadePlano;
    }

    private String normalizarVazio(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}