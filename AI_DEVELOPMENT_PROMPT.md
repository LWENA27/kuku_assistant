# 🐔 KUKU ASSISTANT - COMPREHENSIVE AI DEVELOPMENT PROMPT

## PROJECT OVERVIEW
**App Name:** Kuku Assistant (Fowl Typhoid Monitor)
**Type:** Android Native App (Java)
**Purpose:** Agricultural health management system for poultry farmers, veterinarians, and administrators
**Target Market:** Kenya and East Africa (Swahili + English)
**Technology Stack:** Android (Java), Supabase Backend, REST API

---

## 📱 CORE FUNCTIONALITY REQUIREMENTS

### 1. **MULTI-USER AUTHENTICATION SYSTEM**
```
User Types:
├── 🧑‍🌾 FARMERS
├── 🩺 VETERINARIANS (VETS)  
└── 👨‍💼 ADMINISTRATORS

Features Required:
- Supabase Auth integration
- JWT token management
- Role-based access control (RLS)
- Multi-language support (English/Swahili)
- Profile management per user type
- Secure session handling
```

### 2. **FARMER INTERFACE** 
```
Main Dashboard:
├── Profile Management
│   ├── Farm details (location, size, bird count)
│   ├── Contact information
│   └── Profile picture upload
├── Disease Reporting System
│   ├── Symptom tracking with photos
│   ├── Real-time report submission
│   ├── Report status monitoring
│   └── Historical report viewing
├── Veterinary Consultations
│   ├── Request consultation with vets
│   ├── Chat/messaging system
│   ├── Consultation history
│   └── Appointment scheduling
├── Health Monitoring
│   ├── Bird health tracking
│   ├── Vaccination reminders
│   ├── Treatment history
│   └── Health analytics
└── Alerts & Notifications
    ├── Disease outbreak alerts
    ├── Vaccination reminders
    ├── Weather warnings
    └── Government advisories
```

### 3. **VETERINARIAN INTERFACE**
```
Professional Dashboard:
├── Profile Management
│   ├── Qualifications & certifications
│   ├── Specialization areas
│   ├── Service areas/location
│   ├── Availability schedule
│   └── Contact information
├── Report Management
│   ├── Review farmer reports
│   ├── Diagnosis & recommendations
│   ├── Report status updates
│   └── Priority case handling
├── Consultation System
│   ├── Accept/decline requests
│   ├── Video/audio consultations
│   ├── Prescription management
│   ├── Follow-up scheduling
│   └── Patient history
├── Analytics & Insights
│   ├── Disease pattern analysis
│   ├── Treatment effectiveness
│   ├── Regional health trends
│   └── Performance metrics
└── Professional Tools
    ├── Disease database access
    ├── Treatment protocols
    ├── Drug information
    └── Research updates
```

### 4. **ADMINISTRATOR INTERFACE**
```
Management Dashboard:
├── User Management
│   ├── Farmer account oversight
│   ├── Vet verification & approval
│   ├── User statistics
│   └── Account moderation
├── System Analytics
│   ├── Platform usage metrics
│   ├── Disease outbreak mapping
│   ├── Regional health reports
│   └── Performance analytics
├── Content Management
│   ├── Disease information updates
│   ├── Treatment protocol management
│   ├── Educational content
│   └── Alert system management
├── Data Management
│   ├── Report validation
│   ├── Data quality control
│   ├── Backup management
│   └── Export capabilities
└── System Administration
    ├── Platform configuration
    ├── Security management
    ├── API management
    └── Integration oversight
```

---

## 🛠 TECHNICAL REQUIREMENTS

### **Frontend (Android)**
```
Architecture:
├── MVVM Pattern
├── Repository Pattern  
├── Dependency Injection
└── Clean Architecture

Key Components:
├── Activities (30+ screens)
├── Fragments for dynamic content
├── RecyclerViews for lists
├── Bottom Navigation
├── Material Design 3
├── Custom UI components
├── Image handling (Glide)
├── Chart libraries (MPAndroidChart)
└── Real-time notifications

Core Services:
├── AuthManager - User authentication
├── ApiClient - REST API communication  
├── NotificationManager - Push notifications
├── LocationService - GPS tracking
├── CameraService - Photo capture
├── FileManager - Document handling
└── LocalDatabase - Offline storage
```

### **Backend Requirements (Supabase)**
```
Database Schema:
├── users (authentication)
├── farmers (farmer profiles)
├── vets (veterinarian profiles)
├── reports (disease reports)
├── consultations (vet consultations)
├── messages (chat system)
├── notifications (alerts)
├── diseases (disease database)
├── treatments (treatment protocols)
└── analytics (system metrics)

API Endpoints:
├── Authentication (login/register/refresh)
├── Profile management (CRUD operations)
├── Report management (submit/update/view)
├── Consultation system (request/accept/chat)
├── Notification system (send/receive)
├── Analytics (metrics/reports)
├── File upload (images/documents)
└── Real-time subscriptions

Security Features:
├── Row Level Security (RLS)
├── JWT token authentication
├── API rate limiting
├── Data encryption
├── Audit logging
└── Backup procedures
```

---

## 🎨 UI/UX REQUIREMENTS

### **Design System**
```
Visual Identity:
├── Agricultural green color palette
├── Modern Material Design 3
├── Accessible design (WCAG 2.1)
├── Offline-first approach
└── Multi-language support

Navigation:
├── Bottom navigation for main sections
├── Drawer navigation for secondary features
├── Contextual action bars
├── Breadcrumb navigation
└── Deep linking support

Responsive Design:
├── Phone optimization (primary)
├── Tablet support
├── Landscape mode support
├── Different screen densities
└── Accessibility features
```

### **Key Screens Required**
```
Authentication Flow:
├── Splash/Launcher screen
├── User type selection
├── Login/Register screens
├── Password recovery
└── Profile setup wizard

Farmer Screens:
├── Dashboard/Main activity
├── Profile edit
├── Report symptoms
├── View reports
├── Request consultation
├── Consultations list
├── Alerts/notifications
├── Settings
└── Help/support

Veterinarian Screens:
├── Professional dashboard
├── Profile management
├── View reports queue
├── Consultation details
├── Chat/messaging
├── Analytics
├── Settings
└── Professional tools

Admin Screens:
├── Admin dashboard
├── User management
├── Analytics overview
├── Content management
├── System settings
├── Report validation
├── Alert management
└── Data exports
```

---

## 📊 FEATURE SPECIFICATIONS

### **Disease Reporting System**
```
Report Submission:
├── Multi-step form with validation
├── Photo capture/upload (multiple images)
├── GPS location tagging
├── Symptom checklist interface
├── Severity assessment
├── Urgency classification
└── Offline submission capability

Report Processing:
├── Automatic validation
├── AI-powered preliminary diagnosis
├── Vet assignment algorithm
├── Priority queue management
├── Status tracking system
└── Notification triggers
```

### **Consultation System**
```
Booking System:
├── Vet availability calendar
├── Appointment scheduling
├── Service type selection
├── Location preferences
└── Emergency consultation option

Communication:
├── In-app messaging
├── Video consultation capability
├── File sharing (images/documents)
├── Voice messages
└── Translation support

Management:
├── Consultation history
├── Payment integration (future)
├── Rating/review system
├── Follow-up scheduling
└── Treatment tracking
```

### **Notification System**
```
Alert Types:
├── Disease outbreak warnings
├── Vaccination reminders
├── Consultation updates
├── Report status changes
├── Weather alerts
├── Government advisories
└── App updates

Delivery Methods:
├── Push notifications
├── In-app notifications
├── SMS integration (future)
├── Email notifications
└── WhatsApp integration (future)

Customization:
├── User preference settings
├── Frequency controls
├── Category filtering
├── Quiet hours
└── Emergency override
```

---

## 🌍 LOCALIZATION REQUIREMENTS

### **Language Support**
```
Primary Languages:
├── English (default)
└── Swahili (Kiswahili)

Implementation:
├── String resources externalization
├── RTL layout support preparation
├── Number/date formatting
├── Currency localization (KES)
├── Cultural considerations
└── Voice input in local languages
```

### **Regional Considerations**
```
Kenya-Specific Features:
├── County/subcounty location data
├── Local vet directory
├── Government compliance
├── Mobile money integration prep
├── Local disease patterns
└── Agricultural calendar integration
```

---

## 🔧 DEVELOPMENT REQUIREMENTS

### **Code Architecture**
```
Package Structure:
├── ui/ (activities, fragments, adapters)
├── data/ (models, repositories, api)
├── services/ (background services)
├── utils/ (helper classes)
├── config/ (configuration)
└── resources/ (layouts, strings, etc.)

Design Patterns:
├── MVVM with LiveData
├── Repository pattern
├── Singleton for managers
├── Observer pattern for updates
├── Factory pattern for object creation
└── Dependency injection
```

### **Quality Assurance**
```
Testing Requirements:
├── Unit tests (80% coverage)
├── Integration tests
├── UI automation tests
├── Performance testing
├── Security testing
└── Accessibility testing

Code Quality:
├── ESLint/Checkstyle compliance
├── Documentation requirements
├── Code review process
├── Git workflow (GitFlow)
├── CI/CD pipeline
└── Automated testing
```

### **Performance Requirements**
```
App Performance:
├── Launch time < 3 seconds
├── Smooth 60fps UI
├── Memory usage < 100MB
├── Battery optimization
├── Network efficiency
└── Offline capability

Scalability:
├── Support 10,000+ concurrent users
├── Handle 1000+ reports/day
├── Real-time updates
├── Efficient data synchronization
└── Background processing
```

---

## 📡 INTEGRATION REQUIREMENTS

### **Third-Party Services**
```
Required Integrations:
├── Supabase (backend/database)
├── Google Maps (location services)
├── Firebase (push notifications)
├── Glide (image loading)
├── Chart libraries (analytics)
└── Camera/gallery access

Future Integrations:
├── Payment gateways (M-Pesa)
├── SMS gateway
├── Email service
├── WhatsApp Business API
├── Weather API
└── Government databases
```

### **API Architecture**
```
REST API Design:
├── RESTful endpoints
├── JSON data format
├── JWT authentication
├── Rate limiting
├── Error handling
├── API versioning
├── Documentation (OpenAPI)
└── SDK development
```

---

## 🚀 DEPLOYMENT & MAINTENANCE

### **Deployment Strategy**
```
Release Process:
├── Staged deployment (dev/staging/prod)
├── Play Store optimization
├── Beta testing program
├── A/B testing capability
├── Feature flags
└── Rollback procedures

Monitoring:
├── Crash reporting (Crashlytics)
├── Performance monitoring
├── User analytics
├── Error logging
├── Business metrics
└── Security monitoring
```

### **Maintenance Requirements**
```
Ongoing Support:
├── Bug fixes and updates
├── Security patches
├── Performance optimization
├── Feature enhancements
├── User support system
└── Documentation updates

Analytics & Insights:
├── User behavior tracking
├── Feature usage analytics
├── Performance metrics
├── Business intelligence
├── ROI measurement
└── Growth tracking
```

---

## 📋 DEVELOPMENT PHASES

### **Phase 1: Foundation (Weeks 1-4)**
```
Core Development:
├── Project setup and architecture
├── Basic authentication system
├── User registration/login
├── Profile management
├── Database schema setup
└── Basic UI framework
```

### **Phase 2: Core Features (Weeks 5-12)**
```
Feature Development:
├── Disease reporting system
├── Basic consultation booking
├── Farmer dashboard
├── Vet interface basics
├── Notification system
└── Image upload functionality
```

### **Phase 3: Advanced Features (Weeks 13-20)**
```
Enhanced Functionality:
├── Real-time messaging
├── Analytics dashboard
├── Admin interface
├── Advanced reporting
├── Search and filtering
└── Performance optimization
```

### **Phase 4: Polish & Launch (Weeks 21-24)**
```
Final Preparation:
├── UI/UX refinements
├── Testing and QA
├── Performance optimization
├── Security hardening
├── Documentation completion
└── Store submission
```

---

## 🎯 SUCCESS METRICS

### **Technical KPIs**
```
Performance Metrics:
├── App launch time < 3s
├── 99.9% uptime
├── < 1% crash rate
├── 4.5+ Play Store rating
└── < 100MB memory usage
```

### **Business KPIs**
```
Adoption Metrics:
├── 10,000+ farmer registrations
├── 500+ active vets
├── 1,000+ reports/month
├── 80% user retention
└── 90% consultation completion rate
```

---

## 🔒 SECURITY & COMPLIANCE

### **Security Requirements**
```
Data Protection:
├── End-to-end encryption
├── GDPR compliance
├── Local data protection laws
├── Secure API communication
├── User privacy controls
└── Data retention policies

Authentication Security:
├── Multi-factor authentication
├── Password complexity rules
├── Session management
├── Account lockout protection
├── Secure password recovery
└── Biometric authentication
```

---

**FINAL NOTES:**
This app serves critical agricultural needs in Kenya, focusing on poultry health management. The system must be robust, scalable, and user-friendly for farmers with varying technical literacy. Emphasize offline capability, clear Swahili translations, and intuitive UI design. The success of this platform directly impacts livestock health and farmer livelihoods.

**DELIVERABLES EXPECTED:**
1. Complete Android application source code
2. Supabase backend configuration
3. API documentation
4. User documentation (English & Swahili)
5. Testing suite and QA procedures
6. Deployment scripts and procedures
7. Maintenance and update guidelines
