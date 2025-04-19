
# Airport Service Application

The **Airport Service Application** is a full-stack web application for managing airport data. It provides a RESTful API backend built with **Spring Boot** and a **React-based frontend** for interacting with airport data stored in an in-memory **H2 database**.

Key features include:
- Adding airports
- Filtering by name
- Sorting & pagination
- Delete and Fetch by ICAO.
- Load Json data into Database via API.
- Scripts APIs (Average Elevation per country, Top 10 TZs, Airport without IATA)
- Remote access to application(Cloud Foundary Deployment on BTP)
- UI: https://airport-ui.cfapps.us10-001.hana.ondemand.com
- Backend: https://airport-service.cfapps.us10-001.hana.ondemand.com
  
- <img width="1728" alt="Screenshot 2025-04-20 at 2 01 53 AM" src="https://github.com/user-attachments/assets/8431b2bf-585e-4e27-a130-1a7a6c344a22" />

## Prerequisites

Ensure the following are installed:

1. **Java 17**
   ```bash
   java -version
   # Expected:
   java version "17.0.11" 2024-04-16 LTS
   Java(TM) SE Runtime Environment (build 17.0.11+7-LTS-207)
   Java HotSpot(TM) 64-Bit Server VM (build 17.0.11+7-LTS-207, mixed mode, sharing)
   ```

2. **Node.js 23.3.0**
   ```bash
   node -v
   # Expected: v23.3.0
   ```

3. **Maven 3.9.x**

4. **Bruno** – API Client  
    [Download from usebruno.com](https://www.usebruno.com)

5. **Git**

6. **Code Editor** – Recommended: IntelliJ IDEA CE

---

##  Setup Instructions

### 1. Clone the Repository

```bash
git clone https://github.com/mdataakhan/Airport-App.git
cd Airport-App
```

---

### 2. Set Up the Frontend

```bash
cd airport-ui
npm install
npm run dev
```

- App will start at: `http://localhost:5173`
- By default, it connects to remote backend:  
  `https://airport-service.cfapps.us10-001.hana.ondemand.com`

**To use the local backend instead:**

1. Open: `airport-ui/src/config.js`  
2. Change:
   ```javascript
   const useLocal = false;
   ```
   To:
   ```javascript
   const useLocal = true;
   ```

This switches the backend to `http://localhost:8080`.

---

### 3. Set Up the Backend( Optional, Only needed if "useLocal" from previous step is set to true by default it is false)

```bash
cd ..  # Return to root project directory
java -version  # Confirm Java 17.0.11
mvn clean install
mvn spring-boot:run
```

- Backend will start at: `http://localhost:8080`
- H2 Database auto-initialized at: `jdbc:h2:mem:airportsdb`
- Access H2 console: `http://localhost:8080/h2-console`  
  - **JDBC URL**: `jdbc:h2:mem:airportsdb`  
  - **Username**: `sa`  
  - **Password**: (leave empty)
  - 
  - <img width="1728" alt="Screenshot 2025-04-20 at 1 50 10 AM" src="https://github.com/user-attachments/assets/45bf3b47-51fd-435b-bd75-35cfe87154ff" />

---

### 4. Running the Application

- Open: `http://localhost:5173` in your browser or Remote (Ui: https://airport-ui.cfapps.us10-001.hana.ondemand.com )
- Recommended browser: **Chrome**

####  Features:
- Paginated airport list with sort (by name, city, state, country)
- Search by airport name
- Add new airports via form
- Analytics (e.g., top time zones)
---

### 5. Running API Scripts with Bruno

####  Install Bruno

-  [Get Bruno](https://www.usebruno.com)

####  Import API Collection

1. Open Bruno  
2. Import file:  
   `Api Collections/Airport Service Bruno Collection.json`

---

####  Set Envirnoment

1. If running on local set Envirnoment to Local
-  app_url: http://localhost:8080
  
-  <img width="1728" alt="Screenshot 2025-04-20 at 1 42 19 AM" src="https://github.com/user-attachments/assets/8f2dd500-d6b1-4d89-b2d2-546b9aa304d8" />

3. If running on Remote set Envirnoment to Cloud
-  app_url: https://airport-service.cfapps.us10-001.hana.ondemand.com
  
-  <img width="1728" alt="Screenshot 2025-04-20 at 1 41 46 AM" src="https://github.com/user-attachments/assets/10b718bd-8557-4711-9f27-44730c051146" />

---

####  Load Sample Data

1. Select **"Load Data"** POST request in the Bruno collection  
2. Add sample JSON from `airport.json` (in repo) to the request body  
3. Run the request  
4.  Response:  
   ```
   "Airport data loaded successfully"
   ```
5. This will load the entire data of json in database.
6. Screen Shot for reference.
   <img width="1728" alt="Screenshot 2025-04-20 at 1 33 00 AM" src="https://github.com/user-attachments/assets/9a312731-4544-4b67-9d2c-1bfd21fffac2" />

---
####  Run Other APIs


1. Select **"Get Airport List"** GET request in the Bruno collection to see all Airport data.
2. Select **"Get Airport By Id"** GET request in the Bruno collection to fetch Airport details by giving ICAO id.
3. Select **"Add New Airport"** POST request in the Bruno collection to add new Airport details with given payload.
4. Select **"Delete By Id"** DELETE request in the Bruno collection to delete specfic Airport details by giving its ICAO id.
5. Select **"Get Sorted List"** GET request in the Bruno collection to delete specfic Airport details by giving its ICAO id.
7. Select **"Filter By Name"** GET request in the Bruno collection to get specfic Airport details by giving its complete name or substring.

---


###  Run Scripts

####  Script 1: Get Average Elevation Per Country

- Run: `Get Average Elevation Per Country`
- View average elevation by country
- <img width="1728" alt="Screenshot 2025-04-20 at 1 36 54 AM" src="https://github.com/user-attachments/assets/780ce94f-6a45-4300-b140-816134d07c64" />

####  Script 2: Get Airports Without IATA Code

- Run: `Get Airports without IATA Code`
- View list of airports missing IATA codes
- <img width="1728" alt="Screenshot 2025-04-20 at 1 37 21 AM" src="https://github.com/user-attachments/assets/b21c303d-24b5-4422-b1a4-cc6c6133b572" />

####  Script 3: Get Top Time Zones

- Run: `Get Top Time Zones`
- View the most common time zones by airport count
- <img width="1728" alt="Screenshot 2025-04-20 at 1 37 40 AM" src="https://github.com/user-attachments/assets/ddb53437-aa7d-4824-a0e7-120aa894bb45" />

---




