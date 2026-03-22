# Safar Morocco - Smart Tourism Platform

## 🌍 Concept
Safar Morocco is a smart tourism application designed to promote Moroccan cultural heritage. It helps tourists discover destinations, events, and authentic experiences across the country.  
The platform features an interactive map, a smart AI chatbot for travel assistance, and a comprehensive guide to Morocco's historical, natural, and cultural sites in multiple languages (English, French, Arabic).

---

## 🚀 How to Run

### 1️⃣ Backend (Spring Boot)
> Requires MySQL running on port **3306**

```bash
cd backend
./mvnw spring-boot:run
```

### 2️⃣ Frontend (Angular)
```bash
cd frontend
npm install
npm start
```
Access the application at:
👉 [http://localhost:4200](http://localhost:4200)

### 3️⃣ Chatbot (Python)
```bash
cd chatbot
pip install -r requirements.txt
python main.py
```

---

## 📸 Screenshots & Features Overview

### 🏠 Home Page – Discover Morocco
**Description:**
The landing page introduces users to Morocco’s cultural richness through an immersive hero section, destination search, and quick navigation to maps, events, and destinations.

![Home Page](screenshots/home.png)

### 🗺️ Interactive Map – Explore by Location
**Description:**
An interactive map allows users to visually explore Moroccan cities and regions. Users can search for places, click on map markers, and access destination details easily.

![Interactive Map](screenshots/map.png)

### 🎉 Events Page – Cultural & Local Experiences
**Description:**
Displays upcoming cultural events such as festivals, music shows, and local celebrations. Each event includes date, location, category, and entry type (free or paid).

![Events Page](screenshots/events.png)

### 🔐 Authentication – Login & Signup
**Description:**
Secure authentication system with login, signup, and form validation. Includes password visibility toggle, error handling, and a clean modern UI.

![Authentication](screenshots/login.png)

### 🧑💼 Admin Dashboard – Platform Overview
**Description:**
Provides administrators with a global overview of platform activity, including total travelers, active destinations, upcoming events, pending reviews, and analytics charts.

![Admin Dashboard](screenshots/dashboard.png)

### 📍 Destinations Management (Admin)
**Description:**
Admin interface for managing tourist destinations. Admins can create, update, activate, deactivate, or delete destinations and assign categories such as Cultural, Nature, or Historical.

![Destinations Management](screenshots/destinations_mgmt.png)

### 📅 Events Management (Admin)
**Description:**
Allows administrators to manage events across Morocco, including event creation, scheduling, venue selection, duration, and entry configuration.

![Events Management](screenshots/events_mgmt.png)

### 👤 Profile & Security Settings
**Description:**
User profile management page where users can update personal information and manage account security, including password changes and sensitive actions.

![Profile](screenshots/profile.png)

### 🤖 AI Chatbot – Safar Assistant
**Description:**
An intelligent chatbot that assists users with destination recommendations, travel questions, and platform navigation, improving accessibility and user engagement.

![AI Chatbot](screenshots/chatbot.png)

### 📱 Responsive Design – Mobile Friendly
**Description:**
The platform is fully responsive and optimized for mobile devices, ensuring a smooth experience on smartphones, tablets, and desktops.

![Mobile Design](screenshots/mobile.png)

---

## ✅ Key Features
- Interactive map-based exploration
- Cultural events discovery
- Admin dashboard & content management
- AI-powered travel assistant
- Multi-language support
- Secure authentication system
- Responsive & modern UI

## 🛠️ Technologies Used
- **Frontend:** Angular
- **Backend:** Spring Boot
- **Database:** MySQL
- **Chatbot:** Python
- **Maps:** OpenStreetMap
- **UI Design:** Modern responsive layout
