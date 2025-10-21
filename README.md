# 🧮 HectoClash

### _An Interactive Real-Time Mental Math Game_

**Developed at:** Hackfest Hackathon  
**Team:** 🛡️ AppVengers  
**Members:**  
- Aryan Sharma  
- Yashendra Awasthi  
- Aayush Nagar  
- Manav Bhardwaj  
- Manisankar Das  

---

## 🚀 Overview

**HectoClash** is an engaging **mental math game** designed to make numerical practice fun, competitive, and educational.  
Inspired by the **Hectoc game format**, it challenges players to think quickly and accurately under time pressure.  

Unlike traditional math practice apps, **HectoClash** introduces:
- ⚔️ Real-time duels  
- 🧠 Daily puzzles  
- 🏆 Global leaderboard  

These features build a vibrant and competitive learning community.

---

## 🎯 Problem Statement

Students often struggle to improve their mental math speed and accuracy due to:
- Repetitive and unengaging practice methods  
- Lack of real-time competition or community engagement  
- No motivation or feedback system to track improvement  

---

## 💡 Our Solution

**HectoClash** transforms mental math practice into a thrilling experience by combining:
- Competitive **real-time duels**
- Structured **practice levels**
- **Daily puzzles** for consistent engagement  
- A **leaderboard and ranking system** to motivate users  

---

## 🧩 Features Implemented

### 🕹️ 1. Practice Mode (Levels)
- Offline mode with **1000+ challenge levels**.  
- Levels unlock sequentially after completion.  
- Builds gradual improvement in accuracy and speed.  

---

### ⚔️ 2. Real-Time Duel Mode
- Connects players using **Firebase Realtime Database**.  
- Randomly matches two users for a duel.  
- Fetches a random **Hecto sequence** from a JSON dataset.  
- The first player to solve correctly wins.  
- **Scoring system:**  
  - 🥇 Winner: +50 points  
  - 🥈 Loser: −20 points  
- Scores stored in **Firebase Firestore**.

---

### 🧠 3. Daily Puzzle
- A new **Hecto sequence puzzle** every day.  
- Can be attempted **once per day**.  
- Prevents multiple submissions for fairness.  

---

### 🏆 4. Leaderboard & Ranking System
- Displays player ranks in two modes:
  1. **By Hecto Score**
  2. **By Hecto Level**
- Encourages engagement and competitiveness.  

---

### 👤 5. Profile & More Section
- Shows user profile and progress.  
- Includes **Help** section linking to official Hecto website.  

---

## 🛠️ Tech Stack

| Component | Technology Used |
|------------|-----------------|
| **Frontend** | XML (Android UI) |
| **Backend** | Kotlin |
| **Database** | Firebase Realtime Database, Firestore |
| **Data Source** | JSON (for Hecto sequences) |
| **Platform** | Android |

---
## 🔨 Future Enhancements

- 🎙️ **Voice-based challenges** — make problem-solving more interactive.  
- 🌍 **Global tournaments & team duels** — compete with players worldwide.  
- 🤖 **AI-driven adaptive difficulty** — adjust difficulty based on user performance.  
- 💻 **Cross-platform support (iOS + Web)** — reach a wider audience.  

---

## 📬 Contact

**For queries or collaborations:**  
📧 **aryan180906@gmail.com**  
💻 [**LinkedIn – Aryan Sharma**].(https://www.linkedin.com/in/aryan-sharma-26276131a/)



