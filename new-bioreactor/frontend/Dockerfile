# ── Build stage ────────────────────────────────────────────
FROM node:20-alpine AS build
WORKDIR /app

COPY package.json .
RUN npm install

COPY public ./public
COPY src    ./src

ARG REACT_APP_PHYSICAL_URL=http://localhost:8081
ARG REACT_APP_STRAIN_URL=http://localhost:8085
ARG REACT_APP_GATEWAY_URL=http://localhost:8086
ARG REACT_APP_TWIN_URL=http://localhost:8082
ARG REACT_APP_EXPERIMENT_URL=http://localhost:8087

ENV REACT_APP_PHYSICAL_URL=$REACT_APP_PHYSICAL_URL
ENV REACT_APP_STRAIN_URL=$REACT_APP_STRAIN_URL
ENV REACT_APP_GATEWAY_URL=$REACT_APP_GATEWAY_URL
ENV REACT_APP_TWIN_URL=$REACT_APP_TWIN_URL
ENV REACT_APP_EXPERIMENT_URL=$REACT_APP_EXPERIMENT_URL

RUN npm run build

# ── Serve stage ────────────────────────────────────────────
FROM nginx:alpine
COPY --from=build /app/build /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
