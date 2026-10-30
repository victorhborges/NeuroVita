# Etapa 1: Compilar o frontend Angular
FROM node:24-alpine AS frontend-build

WORKDIR /frontend

COPY frontend/package*.json ./

RUN npm ci

COPY frontend/ ./

RUN npm run build


# Etapa 2: Compilar o backend Java
FROM eclipse-temurin:21-jdk AS backend-build

WORKDIR /app

COPY . .

# Copia os arquivos compilados do Angular para os recursos estáticos
COPY --from=frontend-build /frontend/dist/neurovita/browser/ src/main/resources/static/

RUN chmod +x mvnw

RUN ./mvnw clean package -DskipTests


# Etapa 3: Executar a aplicação
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=backend-build /app/target/neurovita-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
