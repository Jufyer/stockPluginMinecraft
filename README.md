# 📦 Stock Plugin – Project Roadmap & Development Board

Diese Datei enthält die komplette Roadmap, alle Issues, Features, Fixes und das GitHub-Project-Board-Template für dein Minecraft-Stock-Plugin.

---
# 🏗️ **Projektüberblick**
Dieses Projekt ist ein Wirtschaftssystem für Minecraft mit Aktien, Handel, GUIs, dynamischen Preisen und vielfältigen Features. Diese Roadmap dient als Grundlage für langfristige Weiterentwicklung.

---
# 🟥 **PHASE 1 — Kritische Fixes**
Fixe diese Punkte **zuerst**, da sie Performance, Stabilität und Datenintegrität betreffen.

## ✔️ 1.1 – IO Operationen in Async verschieben
**Ort:** `FetchFromGitRepo`, `FetchFromDataFolder`, Commands, stockLoader

**Task:** Alle File-Lesevorgänge, Preisabfragen und Git-Requests müssen **asynchron** laufen.

---
## ✔️ 1.2 – Portfolio & Money persistent speichern
**Ort:** `PortfolioManager`, `Money`

**Task:** Speicherung in JSON/YAML/SQLite einführen.

---
## ✔️ 1.3 – NullPointer Fixes
**Ort:** `ASCIIBarChart`, GUIs, ItemMeta, Preisfetcher

**Task:** Überall sichere Checks einsetzen.

---
## ✔️ 1.4 – config.yml implementieren
**Task:** Alle Hardcoded Werte wie Pfade, Steuern, GUI-Slots, Update-Raten in Config verschieben.

---
## ✔️ 1.5 – MoneySystem refactoren
**Task:** MoneyService einführen, atomic updates, persistenz.

---
# 🟧 **PHASE 2 — Architektur verbessern**

## ✔️ 2.1 – Service Layer einführen
- PriceService
- TradingService
- PortfolioService

Commands werden "dünn" und delegieren nur.

---
## ✔️ 2.2 – PriceCache hinzufügen
Regelmäßige Updates, Zugriff über Cache statt Files.

---
## ✔️ 2.3 – Custom Events
- StockBuyEvent
- StockSellEvent
- PriceUpdateEvent

---
## ✔️ 2.4 – GUI Manager erstellen
Ein zentrales Eventhandling für alle GUIs.

---
# 🟨 **PHASE 3 — Performance Optimierungen**

## ✔️ 3.1 – GUI Inventar Templates
Items nicht jedes Mal neu erzeugen.

---
## ✔️ 3.2 – Chart Caching
ASCII-Charts für 5 Sekunden zwischenspeichern.

---
## ✔️ 3.3 – Git Timeout + Fallback
Timeouts setzen + lokale Dateien als Backup.

---
# 🟩 **PHASE 4 — Neue Features**

## ✔️ 4.1 – GUI Preisverlauf Charts
Anzeige der Preisänderung direkt im Inventar.

---
## ✔️ 4.2 – Price Alerts System
Spieler können Alarme setzen.

---
## ✔️ 4.3 – Limit Orders
Buy-Limit, Sell-Limit, Stop-Loss.

---
## ✔️ 4.4 – Börsenöffnungszeiten
Öffnungszeiten (z. B. 08–20 Uhr) mit Config.

---
## ✔️ 4.5 – Statistiken
- Volatilität
- Most-Traded Stock
- Tagesgewinne

---
# 🟦 **PHASE 5 — High-End Features (Optional)**

## ✔️ 5.1 – NPC Börsenmakler
Mit Citizens oder Minecraft Villagern.

---
## ✔️ 5.2 – Fonds
Vordefinierte Stock-Bundles.

---
## ✔️ 5.3 – Spielerbasierte Börse (Order Book)
P2P Trading zwischen Spielern.

---
## ✔️ 5.4 – Web Dashboard API
Daten von Live-Preisen über HTTP abrufbar.

---
# 📋 GitHub Project Board Template

## **Board Columns**
```
To Do
In Progress
Review
Done
```

## **Issues zum Kopieren**

### 🔥 Critical Fixes
- [ ] Convert all IO to async (FetchFromGitRepo, FetchFromDataFolder)
- [ ] Add persistence for portfolio & money
- [ ] Add null-safety to all GUIs
- [ ] Move hardcoded values into config.yml
- [ ] Refactor Money system

---
### 🛠️ Architecture
- [ ] Add TradingService, PortfolioService, PriceService
- [ ] Add PriceCache
- [ ] Add Custom Events
- [ ] Add GUI Manager

---
### ⚡ Performance
- [ ] Add Inventory Templates
- [ ] Add Chart Cache
- [ ] Add Git timeout & fallback

---
### 🌟 Features
- [ ] GUI Charts
- [ ] Price Alerts
- [ ] Limit Orders
- [ ] Market Hours
- [ ] Statistics System

---
### 🚀 High-End
- [ ] NPC Broker
- [ ] Fonds / ETFs
- [ ] Player Order Book
- [ ] Web Dashboard API

---
# 🎯 Schluss
Diese Roadmap enthält alle notwendigen Tasks, um dein Plugin sauber, stabil, schnell und langfristig erweiterbar zu machen.

Wenn du willst, kann ich:
- passende Code-Dateien vollständig schreiben
- PRs oder Branch-Struktur vorbereiten
- einzelne Systeme (z. B. PriceCache) direkt implementieren.