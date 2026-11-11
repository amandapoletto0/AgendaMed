# Sistema de Agendamento de Consultas Médicas

Projeto acadêmico de API REST desenvolvido com Java 17, Spring Boot, Spring Web, Spring Data JPA, Hibernate, banco H2, Bean Validation e interface web em HTML, CSS e JavaScript. A organização segue as camadas trabalhadas nas aulas: `controller`, `service`, `repository`, `entity`, `dto` e `exception`.

## Integrantes

- Amanda Poletto
- Taina de Oliveira

## Tema e entidades

O sistema permite administrar médicos e pacientes e registrar consultas entre eles.

- **Medico (entidade-base 1):** nome, CRM, especialidade e e-mail.
- **Paciente (entidade-base 2):** nome, e-mail, telefone, CPF, data de nascimento, endereço e data de cadastro gerada automaticamente.
- **Consulta (entidade central):** data e hora, data de criação gerada pelo sistema, status, observação, médico e paciente associados.

A entidade `Consulta` possui relacionamentos `@ManyToOne` com `Medico` e `Paciente`; as entidades-base possuem relacionamentos `@OneToMany`. As respostas da API utilizam DTOs, sem expor diretamente as entidades JPA.

## Quatro regras de negócio

1. **Disponibilidade:** um médico não pode ter duas consultas com status `AGENDADA` no mesmo horário. O paciente também não pode agendar duas consultas no mesmo horário.
2. **Limite por paciente:** cada paciente pode ter no máximo três consultas com status `AGENDADA`.
3. **Dados gerados no backend:** a data de criação e o status inicial da consulta são definidos pelo sistema, não recebidos no JSON de agendamento.
4. **Cancelamento tardio:** quando uma consulta futura é cancelada com menos de 24 horas de antecedência, seu status fica como `CANCELADA_TARDIA`. Cancelamentos com 24 horas ou mais, ou após o horário previsto, ficam como `CANCELADA`. Uma consulta cancelada ou realizada não pode ser reativada nem ter seu status alterado; para remarcar, é necessário criar um novo agendamento.

As regras de negócio são verificadas na camada `service`. Métodos que modificam dados usam `@Transactional` quando necessário.

## Requisitos técnicos aplicados

- Projeto Maven com Java 17 e Spring Boot.
- Dependências: Spring Web, Spring Data JPA, H2 Database, Validation, DevTools e Spring Boot Test.
- Entidades JPA com `@Entity`, chave primária e geração automática de ID.
- Repositórios baseados em `JpaRepository` e métodos de consulta `findBy...`.
- Rotas REST com substantivos no plural e verbos HTTP apropriados.
- DTOs de entrada/saída com Bean Validation.
- Tratamento global de erros por `@RestControllerAdvice`.
- Enum de status persistido como texto com `@Enumerated(EnumType.STRING)`.
- Console H2 habilitado para inspeção durante a avaliação.

## Como executar

1. Extraia o ZIP.
2. Abra no IntelliJ IDEA a pasta que contém `pom.xml`.
3. Aguarde a importação e o download das dependências Maven.
4. Configure o JDK 17 ou superior.
5. Execute `src/main/java/com/amanda/agendamento/AgendamentoApplication.java`.
6. Acesse `http://localhost:8080`.

A aplicação usa H2 em memória; os dados são reiniciados quando a aplicação é reiniciada.

### Conta administrativa inicial

- E-mail: `admin@agendamed.com`
- Senha inicial: `admin123`
- A senha é armazenada com hash BCrypt. A senha inicial é apenas para demonstração acadêmica; altere-a antes de qualquer uso real.

### Médico inicial

- Nome: Dra. Ana Martins
- CRM: 123456
- Especialidade: Clínica Geral
- E-mail: ana.martins@agendamed.com

Somente o administrador pode cadastrar, editar ou excluir médicos. O cadastro de paciente no site solicita dados pessoais e credenciais. As senhas novas exigem no mínimo 8 caracteres, uma letra maiúscula, uma minúscula, um número e um caractere especial. O backend armazena hashes BCrypt, nunca a senha nova em texto puro. A recuperação acadêmica de pacientes exige e-mail e CPF cadastrados; para uso real, deve ser substituída por recuperação por link/token enviado ao e-mail. A recuperação automática não é habilitada para a conta administrativa inicial.

## Integração com Google Agenda

Na lista de consultas, cada consulta com status `AGENDADA` apresenta o botão **Adicionar ao Google Agenda**. O botão abre o Google Agenda em uma nova aba com título, data, horário, médico, paciente e observação (quando houver) preenchidos. A duração do evento é de 30 minutos. O usuário precisa revisar e salvar o evento no Google Agenda; o sistema não cria eventos automaticamente nem sincroniza alterações/cancelamentos.

## Console do banco H2

Com a aplicação em execução, acesse `http://localhost:8080/h2-console`.

- JDBC URL: `jdbc:h2:mem:consultasdb`
- Usuário: `sa`
- Senha: deixe em branco.

## Endpoints da API

As rotas protegidas recebem o cabeçalho `Authorization: Bearer <token>` obtido no login.

### Autenticação

| Método | Rota | Função |
|---|---|---|
| POST | `/auth/login` | Entrar no sistema |
| POST | `/auth/registro` | Registrar paciente e conta |
| POST | `/auth/redefinir-senha` | Redefinir senha de paciente validando e-mail e CPF |

### Médicos

| Método | Rota | Função |
|---|---|---|
| POST | `/medicos` | Cadastrar (administrador) |
| GET | `/medicos` | Listar; aceita `?nome=...` |
| GET | `/medicos/{id}` | Buscar por ID |
| PUT | `/medicos/{id}` | Atualizar (administrador) |
| DELETE | `/medicos/{id}` | Excluir (administrador, se não houver consultas) |

### Pacientes

| Método | Rota | Função |
|---|---|---|
| POST | `/pacientes` | Cadastrar (administrador) |
| GET | `/pacientes` | Listar; aceita `?nome=...` (administrador) |
| GET | `/pacientes/{id}` | Buscar por ID conforme permissões |
| PUT | `/pacientes/{id}` | Atualizar conforme permissões |
| DELETE | `/pacientes/{id}` | Excluir (administrador, se não houver consultas) |

### Consultas

| Método | Rota | Função |
|---|---|---|
| POST | `/consultas` | Agendar consulta |
| GET | `/consultas` | Listar consultas |
| GET | `/consultas/{id}` | Buscar por ID |
| GET | `/consultas/medico/{medicoId}` | Listar consultas de um médico |
| GET | `/consultas/paciente/{pacienteId}` | Listar consultas de um paciente |
| PATCH | `/consultas/{id}/status` | Atualizar status |
| DELETE | `/consultas/{id}` | Excluir consulta |

## Respostas e erros

A API utiliza respostas HTTP adequadas: `200` para consultas e atualizações, `201` para criação, `204` para exclusão bem-sucedida, `400` para requisições inválidas, `404` para recursos/rotas não encontrados e `409` para conflitos de regras de negócio. O tratamento global apresenta o status, a mensagem e, quando aplicável, os detalhes dos campos inválidos.

## Estrutura de pastas

- `controller`: recebe requisições HTTP e monta `ResponseEntity`.
- `service`: regras de negócio e conversão entre entidades e DTOs.
- `repository`: acesso aos dados com Spring Data JPA.
- `entity`: entidades persistidas e enum de status.
- `dto`: objetos de entrada e saída com validação.
- `exception`: exceções de domínio e tratamento global.
- `src/main/resources/static`: interface web.

## Testes de entrega

Recomenda-se iniciar a aplicação e testar os endpoints no Postman ou Insomnia: cadastro com dados válidos e inválidos, e-mail/CPF duplicado, IDs inexistentes, conflito de horário, limite de três consultas ativas, cancelamento com menos de 24 horas e permissões de administrador. O projeto inclui testes automatizados de unidade para o serviço de senhas. Execute `mvn test` no terminal do projeto para executá-los, além dos testes manuais descritos neste arquivo.


### Horário de atendimento
O consultório atende das 08:00 às 19:00. Cada consulta dura 30 minutos, portanto o último horário de início permitido é 18:30. A regra é validada na interface e na camada `ConsultaService`. O sistema também impede sobreposição de consultas de um mesmo médico ou paciente; consultas com início exatamente 30 minutos após a anterior são permitidas.
