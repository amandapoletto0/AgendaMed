# Roteiro rápido de testes

## 1. Criar médico
POST http://localhost:8080/medicos
```json
{"nome":"Carlos Silva","crm":"CRM12345","especialidade":"Cardiologia","email":"carlos@clinica.com"}
```

## 2. Criar paciente
POST http://localhost:8080/pacientes
```json
{"nome":"Amanda Poletto","email":"amanda@email.com","telefone":"(55) 99999-9999"}
```

## 3. Agendar consulta
POST http://localhost:8080/consultas
```json
{"dataHora":"2026-10-15T14:00:00","observacao":"Consulta de rotina","medicoId":1,"pacienteId":1}
```

## 4. Ver consultas
GET http://localhost:8080/consultas

## 5. Ver consultas do paciente
GET http://localhost:8080/consultas/paciente/1

## 6. Testar conflito de horário
Envie novamente uma consulta para o mesmo médico, no mesmo `dataHora`. A API deve retornar 409.

## 7. Testar limite do paciente
Crie três consultas futuras para o mesmo paciente. Na quarta consulta ativa, a API deve retornar 409.

## 8. Finalizar consulta
PATCH http://localhost:8080/consultas/1/status
```json
{"status":"REALIZADA"}
```

## 9. Cancelar consulta
PATCH http://localhost:8080/consultas/1/status
```json
{"status":"CANCELADA"}
```
Se faltarem menos de 24 horas para a consulta, o backend registrará `CANCELADA_TARDIA`.

## 10. Testar validação
Tente criar paciente com e-mail inválido ou telefone inválido. A API deve responder 400 com os campos e mensagens de erro.


## Segurança de senha e sincronização de cadastro
- [ ] Criar conta com senha fraca: a API deve rejeitar a solicitação (HTTP 400).
- [ ] Criar conta com senha forte e confirmar que o login funciona.
- [ ] Conferir no H2 que a senha armazenada não é igual à senha digitada e começa com o formato de hash BCrypt (`$2a$`).
- [ ] Tentar redefinir senha com CPF incorreto: deve falhar.
- [ ] Redefinir senha com e-mail e CPF corretos e testar o login com a nova senha. A recuperação automática de senha está habilitada apenas para pacientes; a conta administrativa inicial não usa esse fluxo.
- [ ] Alterar o e-mail do paciente pela operação de atualização e confirmar que o novo e-mail também permite login.
- [ ] Tentar atualizar o e-mail para outro já utilizado: a API deve rejeitar a operação.

## Integração com Google Agenda (interface web)
- [ ] Entrar como paciente e abrir **Consultas**.
- [ ] Em uma consulta com status `AGENDADA`, clicar em **Adicionar ao Google Agenda**.
- [ ] Confirmar que o Google Agenda abre em uma nova aba com título, data e horário preenchidos. O evento só é salvo depois de confirmar no Google Agenda.
- [ ] Confirmar que consultas canceladas ou realizadas não exibem o botão.


## Horário de atendimento
- Tentar agendar às 07:59: deve retornar erro.
- Agendar às 08:00: deve ser aceito se as demais regras forem atendidas.
- Agendar às 18:30: deve ser aceito se as demais regras forem atendidas (termina às 19:00).
- Agendar às 18:31 ou às 19:00: deve ser rejeitado, pois a consulta duraria além do fechamento.
- Agendar uma consulta para o mesmo médico com início 15 minutos antes ou depois de outra ativa: deve ser rejeitado por sobreposição.
- Agendar uma consulta para o mesmo paciente com início 15 minutos antes ou depois de outra ativa: deve ser rejeitado por sobreposição.
- Agendar uma consulta exatamente 30 minutos depois de outra: deve ser permitido, se as demais regras forem atendidas.
- Tentar agendar às 19:01: deve retornar erro.
