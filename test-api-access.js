// Script de prueba para endpoints protegidos usando Node.js y axios
// Instala axios: npm install axios

const axios = require('axios');

const BASE_URL = 'http://localhost:8080/api';

// Reemplaza estos tokens por JWTs válidos de tu backend
const TOKENS = {
  ADMIN:    '<ADMIN_TOKEN>',
  COMPANY:  '<COMPANY_TOKEN>',
  EMPLOYEE: '<EMPLOYEE_TOKEN>'
};

async function testEndpoint(endpoint, token, role) {
  try {
    const res = await axios.get(`${BASE_URL}${endpoint}`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    console.log(`[${role}] ${endpoint} ->`, res.status, Array.isArray(res.data) ? `Items: ${res.data.length}` : res.data);
  } catch (err) {
    if (err.response) {
      console.log(`[${role}] ${endpoint} ->`, err.response.status, err.response.data);
    } else {
      console.log(`[${role}] ${endpoint} -> ERROR`, err.message);
    }
  }
}

async function main() {
  // Prueba de acceso a empleados
  await testEndpoint('/employees', TOKENS.ADMIN, 'ADMIN');
  await testEndpoint('/employees', TOKENS.COMPANY, 'COMPANY');
  await testEndpoint('/employees', TOKENS.EMPLOYEE, 'EMPLOYEE');

  // Prueba de acceso a encuestas de una empresa (ajusta el ID)
  const testCompanyId = 1;
  await testEndpoint(`/employees/company/${testCompanyId}`, TOKENS.ADMIN, 'ADMIN');
  await testEndpoint(`/employees/company/${testCompanyId}`, TOKENS.COMPANY, 'COMPANY');
  await testEndpoint(`/employees/company/${testCompanyId}`, TOKENS.EMPLOYEE, 'EMPLOYEE');

  // Prueba de dashboard
  await testEndpoint(`/dashboard/company/${testCompanyId}`, TOKENS.ADMIN, 'ADMIN');
  await testEndpoint(`/dashboard/company/${testCompanyId}`, TOKENS.COMPANY, 'COMPANY');
  await testEndpoint(`/dashboard/company/${testCompanyId}`, TOKENS.EMPLOYEE, 'EMPLOYEE');
}

main();
