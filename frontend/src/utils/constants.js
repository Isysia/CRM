// API Base URL
export const API_BASE_URL = 'http://localhost:8080/api';

// Customer endpoints
export const CUSTOMER_ENDPOINTS = {
  BASE: '/customers',
  BY_ID: (id) => `/customers/${id}`,
};

// Offer endpoints
export const OFFER_ENDPOINTS = {
  BASE: '/offers',
  BY_ID: (id) => `/offers/${id}`,
};

// Task endpoints
export const TASK_ENDPOINTS = {
  BASE: '/tasks',
  BY_ID: (id) => `/tasks/${id}`,
};

// Status options
export const CUSTOMER_STATUS = {
  LEAD: 'LEAD',
  ACTIVE: 'ACTIVE',
  INACTIVE: 'INACTIVE',
};

export const OFFER_STATUS = {
  DRAFT: 'DRAFT',
  SENT: 'SENT',
  ACCEPTED: 'ACCEPTED',
  REJECTED: 'REJECTED',
};

export const TASK_STATUS = {
  TODO: 'TODO',
  IN_PROGRESS: 'IN_PROGRESS',
  DONE: 'DONE',
};

export const TASK_PRIORITY = {
  LOW: 'LOW',
  MEDIUM: 'MEDIUM',
  HIGH: 'HIGH',
};

// User roles
export const USER_ROLES = {
  ADMIN: 'ROLE_ADMIN',
  MANAGER: 'ROLE_MANAGER',
  USER: 'ROLE_USER',
};
