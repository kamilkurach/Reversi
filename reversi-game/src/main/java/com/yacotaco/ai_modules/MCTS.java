/**
 * ai_modules package provides different AI models.
 */
package com.yacotaco.ai_modules;

/** MCTS AI module. */
public class MCTS {
    /** Selection class object. */
    private Selection selection;
    /** Expansion class object. */
    private Expansion expansion;
    /** Simulation class object. */
    private Simulation simulation;
    /** Backpropagation class object. */
    private Backpropagation backpropagation;

    /** MCTS class constructor. */
    public MCTS() {
        this.selection = new Selection();
        this.expansion = new Expansion();
        this.simulation = new Simulation();
        this.backpropagation = new Backpropagation();
    }

    /** Selection.
     *
     */
    public class Selection {

        public Selection() {

        }

    }

    /** Expansion.
     *
     */
    public class Expansion {

        public Expansion() {

        }

    }

    /** Simulation.
     *
     */
    public class Simulation {

        public Simulation() {

        }

    }

    /** Backpropagation.
     *
     */
    public class Backpropagation {

        public Backpropagation() {

        }

    }

}
