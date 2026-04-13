FROM node:20-alpine AS build
WORKDIR /app
COPY package.json .
RUN npm install
COPY public ./public
COPY src ./src
RUN npm run build

ARG REACT_APP_GATEWAY_URL=http://localhost:8086
ENV REACT_APP_GATEWAY_URL=$REACT_APP_GATEWAY_URL
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/build /usr/share/nginx/html
EXPOSE 80
