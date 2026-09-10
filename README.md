# 🛠️ Helpdesk Backend API

## 📖 Visão Geral
O **Helpdesk Backend API** é o núcleo de um sistema de gerenciamento de chamados (tickets) voltado para suporte técnico corporativo. Seu objetivo central é garantir o fluxo eficiente de atendimento, desde a abertura da solicitação pelo usuário final, passando pela triagem, escalonamento técnico e resolução, com um controle rigoroso de SLA (Acordo de Nível de Serviço), auditoria de histórico e inventário de equipamentos.

## 💻 Tech Stack
- **Linguagem:** Java 21
- **Framework:** Spring Boot 3.x
- **Segurança:** Spring Security com autenticação JWT (JSON Web Token)
- **Persistência:** Spring Data JPA / Hibernate
- **Banco de Dados:** PostgreSQL (Homologação/Produção) e H2 (Testes)
- **Validação:** Bean Validation (Jakarta Validation)
- **Boilerplate:** Lombok
- **Gerenciador de Dependências:** Maven

## 🚀 Como Inicializar

### 1. Clonagem e Configuração do Repositório
```bash
# Clone o repositório
git clone https://github.com/sua-organizacao/helpdesk-backend.git
cd helpdesk-backend
```

### 2. Configuração das Variáveis de Ambiente (.env / application.properties)
Crie um arquivo `.env` na raiz do projeto ou configure diretamente no `src/main/resources/application.properties` as seguintes variáveis vitais:

```properties
# ===================================================================
# AMBIENTE & PERFIL SPRING
# ===================================================================

SPRING_PROFILES_ACTIVE=prod

# ===================================================================
# BANCO DE DADOS(PostgreSQL / Render/ Neon/ Supabase / outra aplicação)
# ===================================================================
# URL JDBC do PostgreSQL. Na Render/Koyeb/Railway, use a String formatada como:
# jdbc:postgresql://HOST:PORTA/NOME_DO_BANCO
DATABASE_URL=jdbc:postgresql://localhost:5432/helpdesk_db
DB_USER=postgres
DB_PASS=sua_senha_segura_aqui

# Estratégia DDL do Hibernate ('update' ou 'validate' para produção, mais recomendado o 'validate')
DDL_AUTO=update

# ===================================================================
# SEGURANÇA & JWT
# ===================================================================
# Chave secreta para assinatura HMAC-SHA (mínimo de 32 caracteres/Bytes)
JWT_SECRET=helpdesk_secret_key_32_bytes_min_length_for_hmac_sha

# Tempo de expiração em milissegundos(Ex: 86400000 = 24 horas)
JWT_EXPIRATION=86400000

# ===================================================================
# CORS (Integração com o Front-end)
# ===================================================================
# Domínio Público do Front-end hospedado (ex: Vercel). Múltiplos separados por vírgula.
CORS_ALLOWED_ORIGINS=https://seu-front.vercel/qualquer.plataforma.app

# ===================================================================
# SEED DO ADMINISTRADOR INICIAL
# ===================================================================
ADMIN_EMAIL=admin@helpdesk.com
ADMIN_SENHA=SuaSenhaAdminSuperSegura123!

# ===================================================================
# CONFIGURAÇÕES DE UPLOAD (ANEXOS)
# ===================================================================
MAX_FILE_SIZE=10MB
MAX_REQUEST_SIZE=10MB
```

### 3. Instalação de Dependências
Certifique-se de ter o Maven e o Java 21 instalados na máquina. Execute o comando para baixar as dependências e compilar o projeto:
```bash
mvn clean install -DskipTests
```

### 4. Execução da Aplicação
Inicie a aplicação utilizando o plugin do Spring Boot:
```bash
mvn spring-boot:run
```
A API estará disponível por padrão em: `http://localhost:8080`