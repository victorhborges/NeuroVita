
package com.neurovita.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class SecretariaRequest {

    @NotBlank(message = "O nome é obrigatório.")
    @Size(max = 120, message = "O nome deve ter até 120 caracteres.")
    private String nome;

    @NotBlank(message = "O e-mail é obrigatório.")
    @Email(message = "Informe um e-mail válido.")
    @Size(max = 254, message = "O e-mail deve ter até 254 caracteres.")
    private String email;

    @Pattern(
        regexp = "^[0-9()+.\\- ]{10,20}$",
        message = "Informe um telefone válido."
    )
    private String telefone;
    private String emailContato;
    private LocalDate dataNascimento;
    private String cpf;
    private String endereco;
    @Pattern(
        regexp = "^[0-9.\\- ]{8,10}$",
        message = "O CEP deve conter 8 dígitos, podendo incluir hífen."
    )
    private String cep;
    private String cidade;
    private String cargo;
    private LocalDate admissao;
    private Boolean ativo;
    @Size(min = 8, max = 72, message = "A senha provisória deve ter entre 8 e 72 caracteres.")
    private String senhaProvisoria;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmailContato() { return emailContato; }
    public void setEmailContato(String emailContato) { this.emailContato = emailContato; }
    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }
    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }
    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }
    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }
    public LocalDate getAdmissao() { return admissao; }
    public void setAdmissao(LocalDate admissao) { this.admissao = admissao; }
    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
    public String getSenhaProvisoria() { return senhaProvisoria; }
    public void setSenhaProvisoria(String senhaProvisoria) { this.senhaProvisoria = senhaProvisoria; }
}
