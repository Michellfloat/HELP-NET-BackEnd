# Etapa 1: Build da aplicação (compilação)
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app

# Copia os arquivos de dependência primeiro (otimiza o cache do Docker)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copia o código fonte e compila ignorando os testes na esteira
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Execução (Imagem menor e mais segura)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Pega o .jar gerado na etapa 1 e joga para a etapa atual
COPY --from=build /app/target/*.jar app.jar

# Expõe a porta padrão do Spring Boot
EXPOSE 8080

# Comando para rodar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]