# ===================================================
# STAGE 1: Build React Application
# ===================================================
FROM node:24-alpine AS build

WORKDIR /app

# Copy package files
COPY frontend/package*.json ./

# Install dependencies
RUN npm ci --only=production

# Copy source code
COPY frontend/ ./

# Build production bundle
RUN npm run build

# ===================================================
# STAGE 2: Serve with Nginx
# ===================================================
FROM nginx:1.27-alpine

# Copy built files from previous stage
COPY --from=build /app/dist /usr/share/nginx/html

# Copy custom nginx configuration
COPY frontend/nginx.conf /etc/nginx/conf.d/default.conf

# Expose port 80
EXPOSE 80

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:80/ || exit 1

# Start nginx
CMD ["nginx", "-g", "daemon off;"]
