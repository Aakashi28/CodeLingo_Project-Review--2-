# CodeLingo – Online Language Learning Platform  
A GUI-based Java project using Swing and MySQL, designed to support structured language learning through lesson creation, user management, and progress tracking.

---

## 📌 Abstract  
CodeLingo is a Java Swing–based language learning platform integrated with MySQL using JDBC. It supports role-based access for Admins, Instructors, and Learners. The system uses OOP concepts, collections, multithreading, and DAO-based database operations to deliver a simple and effective lesson management and progress-tracking experience.

---

## 🎯 Problem Statement  
Traditional language learning lacks proper lesson management and progress tracking. This project offers a simple Java-based GUI system where admins manage users, instructors create lessons, and learners track their progress through a connected MySQL database.

---

## 🎯 Objectives  
- Provide a simple GUI platform for learning languages.  
- Enable admins to manage users.  
- Allow instructors to create and organize lessons.  
- Allow learners to view lessons and track progress.  
- Integrate Java Swing with MySQL using JDBC for data handling.

---

## 🧩 System Architecture  
The platform follows a three-layer architecture:  
- **GUI Layer:** Java Swing user interface  
- **Logic Layer:** Services, OOP classes, and application logic  
- **Database Layer:** JDBC connection to MySQL storing users, lessons, and progress  

---

## 🧠 OOP Concepts Used  
- **Inheritance:** Admin, Instructor, and Learner classes extend a base User class  
- **Polymorphism:** Each user type displays its own dashboard  
- **Abstraction:** Shared attributes/methods defined in an abstract User class  
- **Interfaces:** Learner implements the ProgressTrackable interface  
- **Encapsulation:** Data access controlled via getters/setters  

---

## 🗄️ JDBC & Database Design  
The project uses JDBC to connect the Java application with a MySQL database.  
DAO classes perform operations on three main tables:  
- **users**  
- **lessons**  
- **progress**  

This ensures secure, modular, and efficient data handling.

---

## 🌐 Servlet Integration (Review 2)
A Java Servlet (`LoginServlet.java`) is implemented to demonstrate server-side processing.  
The servlet performs login validation using JDBC and interacts with the same MySQL database used by the GUI application.  
This shows proper separation between client-side (Swing) and server-side (Servlet) components.


## 🔧 Functional Modules  
### **Admin Module**
- Manage users  
@@ -76,9 +82,9 @@
---

## ✔️ Conclusion  
This project implements a functional language learning system using Java Swing and MySQL. By applying OOP concepts, JDBC, collections, and multithreading, it delivers a clean and user-friendly platform for lesson management and progress tracking.

CodeLingo successfully demonstrates a Java GUI-based language learning platform using Swing and MySQL with JDBC integration. The project applies core Object-Oriented Programming concepts, collections, and multithreading to deliver role-based functionality for Admins, Instructors, and Learners. For Review 2, a Java Servlet is integrated to showcase server-side processing and backend validation, ensuring proper separation between client and server components. Overall, the project meets all review requirements with a clean, modular, and scalable design.
---
