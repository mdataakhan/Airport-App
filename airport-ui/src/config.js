
// If false call will go to Remote URL
// If true call will go to Local URL
const useLocal = false;

export const BASE_URL = useLocal
  ? 'http://localhost:8080/api/airports' // Local
  : 'https://airport-service.cfapps.us10-001.hana.ondemand.com/api/airports'; // Remote
