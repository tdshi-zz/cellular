<!-- PROJECT LOGO -->
<br />
<p align="center">
  <a href="https://github.com/tdshi/cellular">
    <img src="https://github.com/tdshi/cellular/blob/main/src/main/resources/img/logo/logo.gif" alt="Logo" width="450" height="450">
  </a>

<h3 align="center">Cellular Automaton</h3>

  <p align="center">
        This study project was developed during the winter term 2020/2021, and the exercise was to develop a simulator from scratch for cellular automata.
    <br />
    <br />
    <a href="#getting-started">Getting Started</a>
    ·
    <a href="#usage">Usage</a>
    ·
    <a href="#license">License</a>
  </p>

<!-- GETTING STARTED -->

## Getting Started

### Prerequisites

You need to have Java 11 or above installed, in order to run the simulator. Older versions should also work, but maybe something will be displayed weird.
Git and Maven are also required.


### Setup

1. Run the following commands (on terminal):
   ```sh
   $ git clone https://github.com/tdshi/cellular
   $ cd cellular
   $ mvn package
   $ cd target
   $ java -jar Cellular-1.0.jar

<!-- USAGE EXAMPLES -->

## Usage

Following you can see the simulator in action, with a cellular automata based on the rules from Conways Game of Life:
<p align="center">
  <a href="https://github.com/tdshi/cellular">
<img align="center" src="simulator-example.gif" alt="example gif simulator" width="740" height="600">
  </a>

_For more information, please go
to [Wikipedia - Conway's Game of Life](https://en.wikipedia.org/wiki/Conway%27s_Game_of_Life)_

<!-- LICENSE -->

## License

Distributed under the GNU General Public License. See `LICENSE` for more information.
