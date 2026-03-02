# Terminal Buffer Emulator

![Kotlin](https://img.shields.io/badge/kotlin-1.9+-purple.svg?logo=kotlin)
![Java](https://img.shields.io/badge/java-21+-red.svg?logo=java)
![Gradle](https://img.shields.io/badge/gradle-build-blue.svg?logo=gradle)
![Tests](https://img.shields.io/badge/tests-junit5-brightgreen.svg)

A neat and memory-optimized implementation of a terminal buffer in Kotlin—designed for the greatest IDE makers in the world in mind.

## 📋 Table of Contents

- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Requirements Met](#-requirements-met)
- [Architecture](#-architecture)
- [Class Structure](#-class-structure)
- [Test Coverage](#-test-coverage)
- [Design Trade-offs](#-design-trade-offs)
- [Performance Considerations](#-performance-considerations)
- [Getting Started](#-getting-started--local-setup)
- [Quick Start Examples](#quick-start-examples)

---

## ✨ Features

- **Text Rendering & Manipulation**
  - Write and insert text with full support for Unicode Code Points
  - Per-cell text attributes (colors, styles)
  - Intelligent cursor movement and positioning
  
- **Terminal Operations**
  - Full line clearing and partial screen clearing operations
  - Terminal resizing (horizontal and vertical)
  - Scrollback history for rows that no longer fit on screen
  
- **Advanced Styling**
  - Flexible text attributes system (colors, styles per cell)
  - Mutable styling for optimized memory usage
  
- **Error Handling**
  - Custom exception hierarchy for better debugging
  - Comprehensive boundary validation

---

## 🛠️ Tech Stack

| Component | Version |
|-----------|---------|
| **Language** | Kotlin |
| **Runtime** | Java 21 (JVM) |
| **Build Tool** | Gradle |
| **Testing** | JUnit 5 |

---

## ✅ Requirements Met

All specified task requirements have been successfully implemented, with several bonus functionalities added:

- ✓ **Bonus**: Scrollback history, Unicode support, mutable styling optimization

---


## 📦 Class Structure

### Models

| Class | Purpose                                 | Key Fields                                                            |
|-------|-----------------------------------------|-----------------------------------------------------------------------|
| `Cell` | Represents a single symbol with styling | `codePoint: Int`, `textAttributes: TextAttributes`                    |
| `Cursor` | Manages cursor position and movement    | `row: Int`, `column: Int`                                             |
| `Color` | Represents RGB color values             | `red`, `green`, `blue`, etc.                                          |
| `Style` | Enumeration of text styles              | `BOLD`, `ITALIC`, `UNDERLINE`, etc.                                   |
| `TextAttributes` | Per-cell styling information            | `foreground: Color`, `background: Color`, `styles: MutableSet<Style>` |

### Exception Hierarchy

```
RuntimeException
    └── TerminalBufferException (Custom base exception)
        ├── InvalidConfigurationException
        │   └── Thrown when buffer dimensions are invalid
        └── OutOfBoundsException
            └── Thrown when cursor or operations exceed boundaries
```

---

## 🧪 Test Coverage

Testing was a priority throughout development. The test suite is organized into **6 specialized test files** for maintainability and clarity.

**Testing Approach:**
- JUnit 5 with parameterized tests for comprehensive coverage
- Focus on boundary conditions and error handling
- AAA pattern applied
- While 100% code coverage is not achieved, critical paths and edge cases are thoroughly tested

---

## 🔀 Design Trade-offs

This section documents the key architectural decisions and their rationale.

### 1. Terminal Resizing Strategy

**The Challenge:** What happens when the terminal window shrinks horizontally or vertically?

**Option A: Reflow (Not Chosen)**
- Pros: Preserves all text by wrapping overflow to next line
- Cons: Requires 1D tape + soft/hard wrap differentiation; complex state management
- Implementation: Would need continuous line indexing and wrap-flag tracking

**Option B: Classic Model (Chosen) ✓**
```
Horizontal Shrinking → Irreversibly crop rightmost characters
Vertical Shrinking   → Lossless; push excess rows to scrollBack history
```
- Pros: Pure 2D grid, predictable coordinates, simple implementation
- Cons: Horizontal shrinking loses data (matches xterm behavior)
- Rationale: Aligns with industry-standard terminal emulators (xterm, VT100)

### 2. Buffer Structure: 2D vs. 1D Array

**Selected: 2D Array (`Array<Array<Cell>>`) ✓**

```kotlin
data class TerminalBuffer(
    val screen: Array<Array<Cell>>,
    val scrollBackHistory: List<Array<Cell>>
)
```

**Trade-off Analysis:**
As Donald Knuth said, "Premature optimization is the root of all evil." The 2D structure provides superior code clarity and maintainability, with negligible memory overhead for typical terminal sizes.

### 3. Character Representation: CodePoint vs. String

**Selected: Int CodePoint ✓**

```kotlin
data class Cell(
    val codePoint: Int,
    val textAttributes: TextAttributes
)
```

**Why not String?**
- Problem: Thousands of String objects per terminal → GC overhead
- Solution: Store raw `Int` CodePoint, convert to String only when needed

**Features:**
- ✓ Full Unicode support (including emojis, complex scripts)
- ✓ Minimal memory footprint (4 bytes per cell vs. String allocation)
- ✓ Conversion utility: `Array<Cell>` → `String` using StringBuilder

### 4. Edge-of-Screen Behavior: Truncation vs. Wrapping

**Selected: Truncation in `writeText`, Wrapping in `insertText` ✓**

```kotlin
// writeText: Stop at boundary (truncate excess)
buffer.writeText("Very long text", maxWidth=5)  // Result: "Very "

// insertText: Wrap to next line
buffer.insertText("Very long text", maxWidth=5) // Result: "Very\nlong..."
```

**Rationale:**
- `writeText`: Direct placement, predictable behavior
- `insertText`: Logical text flow, respects line width

### 5. TextAttributes: Immutable vs. Mutable

**Selected: Mutable `MutableSet<Style>` ✓**

```kotlin
data class TextAttributes(
    val foreground: Color,
    val background: Color,
    val styles: MutableSet<Style>  // Mutable for in-place modifications
)
```

**Optimization Benefit:**
- Modify styles on individual cells without recreating entire attribute objects
- Reduced garbage collection pressure
- Per-cell style flexibility (not all-or-nothing)

### 6. Cell Implementation: Mutable Data Class

**Selected: Mutable `data class` ✓**

Cells are modified in-place during buffer operations rather than creating new instances. This reduces:
- Object allocation frequency
- Garbage collection pauses
- Memory pressure on large buffers

### 7. Size Limitations & Safety

**Challenge:** What if a user requests `Int.MAX_VALUE` dimensions?

**Solution: Companion Object Constraints**
```kotlin
companion object {
    const val MAX_WIDTH = 10_000
    const val MAX_HEIGHT = 100_000
}
```

---

## 🚀 Getting Started / Local Setup

### Prerequisites

- **JDK 21** installed and configured in your environment variables

### Installation

```bash
# Clone the repository
git clone <repository-url>
cd Terminal-Text-Buffer

# Verify JDK installation
java -version  # Should show Java 21
```

### Build Instructions

**On Linux/macOS:**
```bash
./gradlew build
```

**On Windows:**
```bash
gradlew.bat build
```

This will:
- Download dependencies
- Compile Kotlin source code
- Run all tests
- Package the application

### Running Tests

**Run all tests:**
```bash
./gradlew test
```

**Run specific test class:**
```bash
./gradlew test --tests TerminalBufferInitTest
```

**Run with detailed output:**
```bash
./gradlew test -i
```