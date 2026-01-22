# Etap 1: Budowanie aplikacji React (Build Stage)
FROM node:20-alpine as build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# Etap 2: Serwowanie przez Nginx (Production Stage)
FROM nginx:alpine
# Kopiujemy zbudowane pliki z etapu 1 do katalogu Nginx
COPY --from=build /app/dist /usr/share/nginx/html
# Kopiujemy konfigurację Nginx
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]