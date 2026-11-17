import { TestBed } from '@angular/core/testing';

import { ListaSuspensa } from './lista-suspensa';

describe('ListaSuspensa', () => {
  it('não permite selecionar uma opção marcada como indisponível', () => {
    const fixture = TestBed.createComponent(ListaSuspensa);
    fixture.componentRef.setInput('titulo', 'Idioma');
    fixture.componentRef.setInput('opcoes', [
      { valor: 'pt', rotulo: 'Português' },
      { valor: 'en', rotulo: 'Inglês (indisponível)', desabilitada: true },
    ]);
    fixture.componentRef.setInput('valor', 'pt');
    fixture.detectChanges();

    fixture.nativeElement.querySelector('.lista-campo').click();
    fixture.detectChanges();
    const opcaoIngles = fixture.nativeElement.querySelectorAll('[role="option"]')[1] as HTMLButtonElement;
    expect(opcaoIngles.disabled).toBe(true);
    opcaoIngles.click();

    expect(fixture.componentInstance.valor()).toBe('pt');
  });
});
