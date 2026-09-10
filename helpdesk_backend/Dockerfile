# Etapa 1: Build da aplicacao (compilacao)
# JDK 21: o pom declara <java.version>21</java.version>. Com a imagem 17 anterior o
# build falhava ao compilar com --release 21, e o runtime levantaria
# UnsupportedClassVersionError -- a imagem nunca chegou a ser construivel.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copia os arquivos de dependência primeiro (otimiza o cache do Docker)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copia o código fonte e compila ignorando os testes na esteira
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Execução (Imagem menor e mais segura)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Pega o .jar gerado na etapa 1 e joga para a etapa atual
COPY --from=build /app/target/*.jar app.jar

# Expõe a porta padrão do Spring Boot
EXPOSE 8080

# Comando para rodar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]