# Gantt Chart
```mermaid
gantt
title VoIP Coursework Plan
dateFormat YYYY-MM-DD
axisFormat %d %b
todayMarker off

section Setup
Task allocation (All) :a1, 2026-02-17, 1d
Gntt chart and Trello submitted (All) :a2, 2026-02-26, 1d

section Channel 1 baseline
Audio layer and threading baseline (Charlie) :b1, 2026-02-17, 5d
VoIP and Audio layer split and packet format (Charlie) :b2, after b1, 4d
Channel 1 duplex working (Charlie) :b3, after b2, 2d

section Channel 2
Channel 2 analysis (Bosco) :c1, 2026-02-20, 5d
Channel 2 mitigation and tuning (Bosco) :c2, after c1, 7d

section Channel 3
Channel 3 analysis (Ayrton) :d1, 2026-02-20, 5d
Channel 3 mitigation and tuning (Ayrton) :d2, after d1, 7d

section Security (Channel 1)
Security design (Ayrton, Bosco) :e1, after b2, 2d
Encryption and authentication implemented (Ayrton, Bosco) :e2, after e1, 5d
Security test and demo ready (Ayrton, Bosco) :e3, after e2, 2d

section Evaluation and report
QoS measurements for channels 1 to 3 (All) :f1, 2026-03-03, 6d
Report writing and figures (All) :f2, 2026-03-06, 9d

section Milestones
Demo :m1, 2026-03-11, 1d
Report submission :m2, 2026-03-16, 1d
```
---
# Channel Analysis
## Channel 2 (DatagramSocket2)
* Received: 7510 / 10000
* Loss: 2490 packets (24.9%)
* Maximum burst length: 15
* Average burst length: 5
## Channel 3 
* Received: 1540 / 2000
* Loss: 460 packets (23.0%)
* Burst loss: 263 bursts
* Max burst: 154 packets (about 4.9 s)
* Avg burst length: about 1.75 packets (about 56 ms)
* Avg delay: 45.3 ms
* Max delay: 325.3 ms

