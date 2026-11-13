import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Calendario } from './calendario';

describe('Calendario', () => {
  let fixture: ComponentFixture<Calendario>;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    fixture = TestBed.createComponent(Calendario);
    fixture.componentRef.setInput('mes', 'novembro de 2026');
    fixture.componentRef.setInput('semanas', []);
  });

  it('emite navegação para o mês anterior pelo controle existente', () => {
    const navegacoes: number[] = [];
    fixture.componentInstance.navegarMes.subscribe((delta) => navegacoes.push(delta));
    fixture.detectChanges();

    const botao = fixture.nativeElement.querySelector('[aria-label="Mês anterior"]') as HTMLButtonElement;
    botao.click();

    expect(navegacoes).toEqual([-1]);
  });

  it('emite navegação para o próximo mês pelo controle existente', () => {
    const navegacoes: number[] = [];
    fixture.componentInstance.navegarMes.subscribe((delta) => navegacoes.push(delta));
    fixture.detectChanges();

    const botao = fixture.nativeElement.querySelector('[aria-label="Próximo mês"]') as HTMLButtonElement;
    botao.click();

    expect(navegacoes).toEqual([1]);
  });
});
