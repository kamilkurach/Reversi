package com.yacotaco.ai_modules;

/**
 * MCTS AI module
 */

public class MCTS {
    private Selection selection;
    private Expansion expansion;
    private Simulation simulation;
    private Backpropagation backpropagation;

    public MCTS() {
        this.selection = new Selection();
        this.expansion = new Expansion();
        this.simulation = new Simulation();
        this.backpropagation = new Backpropagation();
    }

    // Selection
    public class Selection {

        public Selection() {
            
        }

    }

    // Expansion
    public class Expansion {

        public Expansion() {

        }

    }

    // Simulation
    public class Simulation {

        public Simulation() {

        }

    }

    // Backpropagation
    public class Backpropagation {

        public Backpropagation() {

        }
        
    }
    
}
