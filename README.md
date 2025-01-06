# JShorelark 🦅

A Java/JavaFX port of the fascinating [Shorelark](https://github.com/patryk27/shorelark) project - a bird evolution simulator that demonstrates genetic algorithms and neural networks in action.

## Overview

JShorelark is a reimagining of the original Rust/TypeScript project in pure Java, bringing the excitement of artificial life and evolution to the JVM ecosystem. Watch as birds learn to navigate through obstacles using neural networks and genetic algorithms, evolving better strategies with each generation.

## Original Project

This project is based on the excellent work by [Patryk Wychowaniec](https://github.com/patryk27) and his detailed blog series:

1. [Learning to Fly - Part 1: Genetic Algorithms](https://pwy.io/posts/learning-to-fly-pt1/)
2. [Learning to Fly - Part 2: Neural Networks](https://pwy.io/posts/learning-to-fly-pt2/)
3. [Learning to Fly - Part 3: Backpropagation](https://pwy.io/posts/learning-to-fly-pt3/)
4. [Learning to Fly - Part 4: Putting it All Together](https://pwy.io/posts/learning-to-fly-pt4/)

## How It Works

JShorelark combines several key components:

### 🧬 Genetic Algorithm
- Manages a population of birds
- Uses natural selection to evolve better flying strategies
- Implements crossover and mutation operations to create new generations

### 🧠 Neural Network
- Each bird has its own neural network "brain"
- Processes inputs like position, velocity, and obstacle locations
- Determines flight adjustments in real-time

### 🎮 JavaFX Visualization
- Real-time visualization of the evolution process
- Watch birds learn and adapt over generations
- Interactive controls for simulation parameters

## Key Features

- Pure Java implementation
- Modern JavaFX user interface
- Real-time evolution visualization
- Configurable simulation parameters
- Educational tool for understanding AI concepts

## Requirements

- Java 17 or higher
- JavaFX runtime

## Building and Running

```bash
# Clone the repository
git clone https://github.com/yourusername/JShorelark.git

# Navigate to project directory
cd JShorelark

# Build with Maven
mvn clean install

# Run the application
java -jar target/jshorelark.jar
```

## Contributing

Contributions are welcome! Feel free to:
- Report bugs
- Suggest features
- Submit pull requests

## License

This project is licensed under the same terms as the original Shorelark project.

## Acknowledgments

Special thanks to Patryk Wychowaniec for creating the original Shorelark project and writing the excellent blog series that made this port possible.