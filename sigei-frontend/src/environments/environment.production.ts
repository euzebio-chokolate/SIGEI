export const environment = {
  production: true,
  // O Nginx encaminha /api para o backend (API_UPSTREAM). A mesma origem é
  // obrigatória: a sessão usa cookies HttpOnly com SameSite=Strict.
  apiUrl: '/api'
};
