# Exportador de Relatórios 🤖

Este projeto é uma API desenvolvida em Java com Spring Boot para geração de relatórios em segundo plano.

A ideia é permitir que o usuário solicite um relatório, acompanhe o andamento do processamento e faça o download do arquivo quando ele estiver pronto, sem travar a aplicação durante a geração.

## Funcionalidades 📁

- Geração de relatórios em background
- Acompanhamento de status e progresso
- Download de arquivo CSV por link temporário
- Listagem de relatórios com filtros e paginação
- Reprocessamento de relatórios
- Cancelamento de relatórios em andamento
- Exclusão de relatórios
- Expiração e limpeza automática dos arquivos
- Tratamento padronizado de erros

## Tipos de relatório 📌

Atualmente, a API permite gerar relatórios de:

- Transações
- Clientes
- Vendas

## Tecnologias utilizadas 🤖

- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- MySQL
- Maven
- Lombok
- Bean Validation
- Processamento assíncrono com `@Async`
- Tarefas automáticas com `@Scheduled`

## Como executar 📊

Antes de iniciar, é necessário ter o Java 21 e o MySQL instalados.

O projeto utiliza as seguintes variáveis de ambiente:

DB_URL
DB_USERNAME
DB_PASSWORD
PORT

Caso elas não sejam informadas, a aplicação utiliza o MySQL local configurado no `application.properties`.

No Windows, execute:

mvnw.cmd spring-boot:run

Ou rode a classe principal diretamente pelo VS Code:

ExportadorRelatoriosApplication

A aplicação ficará disponível em:

http://localhost:8080

## Principais endpoints 🌐

POST /relatorios/exportar
GET /relatorios/{idRelatorio}/status
GET /relatorios/download/{token}
GET /relatorios
GET /relatorios/tipos
GET /relatorios/resumo
POST /relatorios/{idRelatorio}/reprocessar
POST /relatorios/{idRelatorio}/cancelar
DELETE /relatorios/{idRelatorio}

## Sobre o projeto 💻

Este projeto foi desenvolvido para praticar conceitos importantes de backend, como processamento assíncrono, persistência de dados, geração de arquivos, organização em camadas e criação de APIs REST.

Como próximas melhorias, podem ser adicionados autenticação, documentação com Swagger, testes automatizados e armazenamento dos arquivos em nuvem.

## Autor 👨‍💻
Vitor Pereira
