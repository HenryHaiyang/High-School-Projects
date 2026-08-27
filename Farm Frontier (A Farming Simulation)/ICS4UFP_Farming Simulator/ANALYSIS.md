# Logic Issues Found: Building Count and Task Generation

## Issues Identified

### 1. **CRITICAL: Task Generation Never Triggers for Warehouse (Already Has Default)**

**Location:** `generateDailyTasks()` method, line 1999
```java
if(farm.getCurrentInventorySize() > farm.getInventoryCapacity() * 0.75 && farm.countBuildings("Warehouse") == 0) {
    taskQueue.enqueue(new Task("Build at least 1 Warehouse to increase storage", "BUILD", "Warehouse", 1, 500));
}
```

**Problem:** 
- In the Farm constructor (line 985), a default Warehouse is created: `buildings[2] = new Warehouse();`
- The task checks `countBuildings("Warehouse") == 0`, which will NEVER be true at game start
- This means the Warehouse building task can never be generated for new games
- Players starting fresh will never get this task

---

### 2. **FRAGILE: Building Tasks Only Check for Count == 0**

**Locations:** Lines 1999, 2002, 2005
```java
farm.countBuildings("Warehouse") == 0      // Line 1999
farm.countBuildings("Greenhouse") == 0     // Line 2002
farm.countBuildings("Supermarket") == 0    // Line 2005
```

**Problem:**
- Tasks only trigger when building count equals exactly 0
- If a player already has 1 Warehouse and builds a 2nd one, the task will never trigger again
- Better approach: Check if count is LESS THAN a target amount (e.g., `< 1`)
- This makes tasks recyclable/progressive: "Build 1 Warehouse" → "Build 2 Warehouses" → etc.

---

### 3. **MAP vs BUILDINGS ARRAY MISMATCH**

**Locations:**
- FarmMap constructor (lines 444-446): Places Barn, Pond, Greenhouse on map as symbols
- Farm constructor (lines 985-986): Creates Barn, FishPond, Warehouse as objects

**Problem:**
- FarmMap places a Greenhouse at [4][4] but Farm doesn't add it to the buildings array
- `countBuildings("Greenhouse")` returns 0, even though there's a 'G' symbol on the map
- The buildings array and the map are out of sync
- If map is loaded via `loadBuildingsFromMap()`, it will overwrite the manually-created buildings

**Example Mismatch:**
```
Map has:          Buildings array has:
B (Barn)          Barn
P (Pond)          FishPond
G (Greenhouse)    Warehouse
W (Warehouse)     (nothing at index 3)
```

---

### 4. **Inconsistent Building Addition**

**Location:** Farm constructor, line 987
```java
farmMap.placeBuilding(3, 1, 'W');  // Adds to map but...
// buildings array already has Warehouse added above at line 986
```

**Problem:**
- A second Warehouse is placed on the map
- But only 1 Warehouse object was added to the buildings array
- This creates inconsistency between visual map and counting logic

---

## Impact Summary

| Issue | Affects | Severity |
|-------|---------|----------|
| Default Warehouse | Task never generates | **CRITICAL** |
| Only checks `== 0` | Task won't repeat; fragile logic | **HIGH** |
| Map/Array mismatch | CountBuildings incorrect; inconsistent state | **HIGH** |
| Extra map Warehouse | Visual/counting mismatch | **MEDIUM** |

---

## Recommended Fixes

1. **Remove default buildings from constructor** OR
   - Load buildings from map at startup instead of hardcoding

2. **Change task conditions** from `== 0` to `< targetAmount`
   - Example: `countBuildings("Warehouse") < 1` instead of `== 0`

3. **Sync map with buildings array**
   - Either load all from map, or add all created buildings to both map and array

4. **Check for this pattern in other building-related logic**
   - Review all places that depend on building counts
