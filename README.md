# Jamming – Spontaneous Live Music Events App

Jamming is an Android application designed to connect people who are looking for small, intimate, and spontaneous live music events with venue owners and event organizers who want to publish such events in real time.

The app focuses on short-notice events (e.g., live shows in bars, open mic nights, local DJs) and allows users to discover nearby events, register for them, and receive updates, while owners can create, manage, and monitor their events.

This project was developed as part of an academic course and follows modern Android development practices and architecture.

---

## Main Features

### For Regular Users
- Browse nearby events on a map or in a list  
- Filter events by distance and date/time and more  
- View full event details (location, time, description, available spots)  
- Register and cancel registration for events  
- View **My Events** (all registered events)  
- Edit profile and preferences  
- Receive notifications about:
  - Event cancellations  
  - Changes in event details  

### For Event Owners
- Create new events with:
  - Name, description, date, time, location, capacity, music genre(s)
- Edit or cancel existing events  
- View all events created by the owner in a dashboard  
- Track number of registered users and availability  
- Receive notifications about:
  - Sold-out events  
  - Registration changes  
- Edit owner profile and venue details  

---

## Architecture

The project is built using the **MVVM (Model–View–ViewModel)** architecture:

- **View (Activities/Fragments)**  
  Responsible only for UI rendering and user interactions. Observes LiveData from ViewModels.

- **ViewModel**  
  Holds UI state and business logic, performs validation, and communicates with repositories.

- **Repository**  
  Handles data operations and communication with Firebase services.

- **Model**  
  Represents core entities such as User, Owner, Event, etc.

Additionally, the project uses Utility classes (e.g., for date handling, location calculations, formatting) to keep ViewModels focused on business logic.

---

## Tech Stack

- **Platform:** Android (Java)  
- **Architecture:** MVVM  
- **Backend & Services:** Firebase  
  - Firebase Authentication  
  - Cloud Firestore  
  - Firebase Storage (if applicable)  
- **Maps & Location:** Google Maps API  
- **UI & State Management:** LiveData, ViewModel  

---

## Testing

- **Unit Tests:**  
  Implemented mainly for ViewModels to test:
  - Input validation  
  - Business logic (e.g., genre selection)  
  - Successful event creation with mocked repositories  

- **UI Tests (Espresso):**  
  Implemented for the Create Event screen to verify:
  - Successful event creation flow  
  - Validation errors on empty/invalid input  
  - Proper UI state after fixing errors  

- **Build System:** Gradle  

---

## Project Structure

- `app/` – Main Android application source code  
  - `view/` – Activities / UI layer  
  - `viewmodel/` – ViewModels (business logic & state)  
  - `repository/` – Data access layer (Firebase, etc.)  
  - `model/` – Data models (Event, User, Owner, etc.)  
  - `utils/` – Utility/helper classes  
- `assignment_docs/` – Course documents, reports, and submissions  
- Root files: `build.gradle`, `settings.gradle`, `gradlew`, etc.

---

## How to Run the Project

1. Clone the repository:
   ```bash
   git clone <your-repo-url>
2. Open the project in Android Studio.  
3. Make sure you have a valid Google Maps API key and Firebase configuration.  
4. Add your local `google-services.json` file to:
5. This file is not included in the repository for security reasons.  
6. Sync Gradle and run the app on an emulator or a physical device.

---

## 🔐 Firebase & API Keys Notice

- The file `google-services.json` is intentionally not included in the repository.  
- Each developer / tester should use their own Firebase project configuration.  
- Google Maps API keys should be restricted (e.g., by SHA-1) and not exposed publicly.

---

## 📌 Project Scope

### Included
- Event discovery (map & list)  
- Event registration and cancellation  
- Event creation and management by owners  
- Real-time availability updates  
- Notifications for users and owners  
- Profile and preferences management  

### Not Included
- Payments or ticket purchasing  
- Integration with social media platforms (e.g., Facebook, Instagram)  
- Large-scale commercial concerts or festivals  
(The app focuses only on small, intimate, and spontaneous events)

---

## 👥 Team

- Shirat Hutterer  
- Or Deri  
- Reut Gerber  

Developed as part of an academic software engineering / Android development course project.

---

## 📄 License

This project was developed for educational purposes.

